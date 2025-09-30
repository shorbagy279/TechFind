package com.example.techdirectory.technician;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"}, allowCredentials = "true")
public class TechnicianController {
    
    private static final Logger logger = LoggerFactory.getLogger(TechnicianController.class);
    
    private final TechnicianRepository technicianRepository;
    private final FieldRepository fieldRepository;
    private final RegionRepository regionRepository;
    private final ReviewRepository reviewRepository;

    public TechnicianController(
            TechnicianRepository technicianRepository,
            FieldRepository fieldRepository,
            RegionRepository regionRepository,
            ReviewRepository reviewRepository) {
        this.technicianRepository = technicianRepository;
        this.fieldRepository = fieldRepository;
        this.regionRepository = regionRepository;
        this.reviewRepository = reviewRepository;
    }

    @GetMapping("/public/technicians/search")
    public ResponseEntity<?> searchTechnicians(
            @RequestParam(required = false) String governorate,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String field,
            @RequestParam(required = false) Double userLat,
            @RequestParam(required = false) Double userLng,
            @RequestParam(required = false) Integer maxDistance,
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Boolean availableNow,
            @RequestParam(required = false) Boolean emergencyService,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "distance") String sortBy
    ) {
        try {
            // Validate pagination parameters
            if (page < 0) page = 0;
            if (size < 1 || size > 100) size = 20;
            
            List<Technician> technicians;
            
            // Search with or without location filters
            if (governorate != null && !governorate.isBlank()) {
                technicians = technicianRepository.searchWithFilters(
                    governorate.trim(), 
                    city != null ? city.trim() : null, 
                    field != null ? field.trim() : null, 
                    minRating, 
                    maxPrice, 
                    availableNow, 
                    emergencyService
                );
            } else {
                technicians = technicianRepository.findAllActiveWithFilters(
                    minRating, 
                    maxPrice, 
                    availableNow, 
                    emergencyService
                );
            }
            
            // Handle null list from repository
            if (technicians == null) {
                technicians = List.of();
            }
            
            // Filter out null technicians and validate data
            technicians = technicians.stream()
                .filter(tech -> tech != null && 
                               tech.getFullName() != null && 
                               !tech.getFullName().isBlank())
                .collect(Collectors.toList());
            
            // Apply distance filtering and sorting if location provided
            if (userLat != null && userLng != null) {
                technicians = technicians.stream()
                    .map(tech -> new TechnicianWithDistance(tech, tech.calculateDistanceFrom(userLat, userLng)))
                    .filter(techDist -> maxDistance == null || techDist.getDistance() <= maxDistance)
                    .sorted((t1, t2) -> sortTechnicians(t1, t2, sortBy))
                    .map(TechnicianWithDistance::getTechnician)
                    .collect(Collectors.toList());
            } else {
                // Sort without distance
                technicians = technicians.stream()
                    .sorted((t1, t2) -> sortTechniciansWithoutDistance(t1, t2, sortBy))
                    .collect(Collectors.toList());
            }
            
            // Apply pagination
            int start = page * size;
            int end = Math.min(start + size, technicians.size());
            List<Technician> pagedTechnicians = start < technicians.size() ? 
                technicians.subList(start, end) : List.of();
            
            int totalPages = technicians.size() > 0 ? (int) Math.ceil((double) technicians.size() / size) : 0;
            
            logger.debug("Search returned {} technicians (page {}/{})", 
                pagedTechnicians.size(), page, totalPages);
            
            return ResponseEntity.ok(new SearchResult(
                pagedTechnicians,
                technicians.size(),
                page,
                size,
                totalPages
            ));
            
        } catch (Exception e) {
            logger.error("Error searching technicians", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Failed to search technicians: " + e.getMessage()));
        }
    }

    @GetMapping("/public/technicians/{id}")
    public ResponseEntity<?> getTechnicianProfile(@PathVariable Long id) {
        try {
            Technician technician = technicianRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Technician not found"));
            
            List<Review> reviews = reviewRepository.findByTechnicianIdOrderByCreatedAtDesc(id);
            
            return ResponseEntity.ok(new TechnicianProfile(technician, reviews));
            
        } catch (RuntimeException e) {
            logger.warn("Technician not found: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("Technician not found"));
        } catch (Exception e) {
            logger.error("Error getting technician profile: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Failed to retrieve technician profile"));
        }
    }

    @PostMapping("/user/technicians/{id}/contact")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> contactTechnician(
            @PathVariable Long id,
            @Valid @RequestBody ContactRequest request
    ) {
        try {
            Technician technician = technicianRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Technician not found"));
            
            ContactResponse response = new ContactResponse();
            String method = request.getContactMethod();
            
            if ("call".equals(method) && Boolean.TRUE.equals(technician.getAllowDirectCalls())) {
                response.setPhoneNumber(technician.getPhone());
                response.setAllowed(true);
                response.setMessage("You can call the technician");
            } else if ("whatsapp".equals(method) && Boolean.TRUE.equals(technician.getAllowWhatsApp())) {
                String whatsappNum = technician.getWhatsappNumber();
                if (whatsappNum != null && !whatsappNum.isBlank()) {
                    response.setWhatsappNumber(whatsappNum);
                    response.setWhatsappLink("https://wa.me/" + whatsappNum.replaceAll("[^0-9]", ""));
                    response.setAllowed(true);
                    response.setMessage("WhatsApp available");
                } else {
                    response.setAllowed(false);
                    response.setMessage("WhatsApp number not configured");
                }
            } else if ("message".equals(method) && Boolean.TRUE.equals(technician.getAllowInAppMessages())) {
                response.setMessageThreadId(createMessageThread(id));
                response.setAllowed(true);
                response.setMessage("In-app messaging available");
            } else {
                response.setAllowed(false);
                response.setMessage("This contact method is not available for this technician");
            }
            
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("Technician not found"));
        } catch (Exception e) {
            logger.error("Error contacting technician: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Failed to process contact request"));
        }
    }

    @PostMapping("/admin/technicians")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createTechnician(@Valid @RequestBody CreateTechnicianRequest request) {
        try {
            // Validate region
            Region region = regionRepository.findById(request.getRegionId())
                .orElseThrow(() -> new IllegalArgumentException("Region not found"));
            
            // Validate fields
            if (request.getFieldIds() == null || request.getFieldIds().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(new ErrorResponse("At least one field is required"));
            }
            
            var fields = request.getFieldIds().stream()
                .map(fieldId -> fieldRepository.findById(fieldId)
                    .orElseThrow(() -> new IllegalArgumentException("Field not found: " + fieldId)))
                .collect(Collectors.toSet());
            
            // Create technician
            Technician technician = Technician.builder()
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .email(request.getEmail())
                .summary(request.getSummary())
                .description(request.getDescription())
                .region(region)
                .fields(fields)
                .experienceYears(request.getExperienceYears())
                .baseServiceFee(request.getBaseServiceFee())
                .currency(request.getCurrency() != null ? request.getCurrency() : "EGP")
                .priceType(request.getPriceType())
                .allowDirectCalls(request.getAllowDirectCalls() != null ? request.getAllowDirectCalls() : false)
                .allowWhatsApp(request.getAllowWhatsApp() != null ? request.getAllowWhatsApp() : false)
                .allowInAppMessages(request.getAllowInAppMessages() != null ? request.getAllowInAppMessages() : false)
                .whatsappNumber(request.getWhatsappNumber())
                .workingHoursStart(request.getWorkingHoursStart())
                .workingHoursEnd(request.getWorkingHoursEnd())
                .workingDays(request.getWorkingDays())
                .lat(request.getLat())
                .lng(request.getLng())
                .address(request.getAddress())
                .certification(request.getCertification())
                .isEmergencyService(request.getIsEmergencyService() != null ? request.getIsEmergencyService() : false)
                .emergencyFeeMultiplier(request.getEmergencyFeeMultiplier())
                .active(true)
                .available(true)
                .availabilityStatus(Technician.AvailabilityStatus.AVAILABLE)
                .build();
            
            technician = technicianRepository.save(technician);
            
            logger.info("Technician created: ID={}, Name={}", technician.getId(), technician.getFullName());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(technician);
            
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid technician creation request: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error creating technician", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Failed to create technician"));
        }
    }
    
    @PutMapping("/admin/technicians/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateTechnician(
            @PathVariable Long id, 
            @Valid @RequestBody CreateTechnicianRequest request) {
        try {
            Technician technician = technicianRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Technician not found"));
            
            // Update fields
            if (request.getFullName() != null) technician.setFullName(request.getFullName());
            if (request.getPhone() != null) technician.setPhone(request.getPhone());
            if (request.getEmail() != null) technician.setEmail(request.getEmail());
            if (request.getSummary() != null) technician.setSummary(request.getSummary());
            if (request.getDescription() != null) technician.setDescription(request.getDescription());
            
            if (request.getRegionId() != null) {
                Region region = regionRepository.findById(request.getRegionId())
                    .orElseThrow(() -> new IllegalArgumentException("Region not found"));
                technician.setRegion(region);
            }
            
            if (request.getFieldIds() != null && !request.getFieldIds().isEmpty()) {
                var fields = request.getFieldIds().stream()
                    .map(fieldId -> fieldRepository.findById(fieldId)
                        .orElseThrow(() -> new IllegalArgumentException("Field not found: " + fieldId)))
                    .collect(Collectors.toSet());
                technician.setFields(fields);
            }
            
            if (request.getExperienceYears() != null) technician.setExperienceYears(request.getExperienceYears());
            if (request.getBaseServiceFee() != null) technician.setBaseServiceFee(request.getBaseServiceFee());
            if (request.getCurrency() != null) technician.setCurrency(request.getCurrency());
            if (request.getPriceType() != null) technician.setPriceType(request.getPriceType());
            if (request.getAllowDirectCalls() != null) technician.setAllowDirectCalls(request.getAllowDirectCalls());
            if (request.getAllowWhatsApp() != null) technician.setAllowWhatsApp(request.getAllowWhatsApp());
            if (request.getAllowInAppMessages() != null) technician.setAllowInAppMessages(request.getAllowInAppMessages());
            if (request.getWhatsappNumber() != null) technician.setWhatsappNumber(request.getWhatsappNumber());
            if (request.getWorkingHoursStart() != null) technician.setWorkingHoursStart(request.getWorkingHoursStart());
            if (request.getWorkingHoursEnd() != null) technician.setWorkingHoursEnd(request.getWorkingHoursEnd());
            if (request.getWorkingDays() != null) technician.setWorkingDays(request.getWorkingDays());
            if (request.getLat() != null) technician.setLat(request.getLat());
            if (request.getLng() != null) technician.setLng(request.getLng());
            if (request.getAddress() != null) technician.setAddress(request.getAddress());
            if (request.getCertification() != null) technician.setCertification(request.getCertification());
            if (request.getIsEmergencyService() != null) technician.setIsEmergencyService(request.getIsEmergencyService());
            if (request.getEmergencyFeeMultiplier() != null) technician.setEmergencyFeeMultiplier(request.getEmergencyFeeMultiplier());
            
            technician = technicianRepository.save(technician);
            
            logger.info("Technician updated: ID={}", id);
            
            return ResponseEntity.ok(technician);
            
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("Technician not found"));
        } catch (Exception e) {
            logger.error("Error updating technician: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Failed to update technician"));
        }
    }
    
    @DeleteMapping("/admin/technicians/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteTechnician(@PathVariable Long id) {
        try {
            if (!technicianRepository.existsById(id)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Technician not found"));
            }
            
            technicianRepository.deleteById(id);
            
            logger.info("Technician deleted: ID={}", id);
            
            return ResponseEntity.ok(new SuccessResponse("Technician deleted successfully"));
            
        } catch (Exception e) {
            logger.error("Error deleting technician: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Failed to delete technician"));
        }
    }

    // Helper methods
    private Long createMessageThread(Long technicianId) {
        // TODO: Implement actual message thread creation
        return System.currentTimeMillis();
    }
    
    private int sortTechnicians(TechnicianWithDistance t1, TechnicianWithDistance t2, String sortBy) {
        if (t1 == null || t2 == null) return 0;
        
        switch (sortBy) {
            case "distance":
                return Double.compare(t1.getDistance(), t2.getDistance());
            case "rating":
                double r1 = t1.getTechnician().getAverageRating() != null ? t1.getTechnician().getAverageRating() : 0.0;
                double r2 = t2.getTechnician().getAverageRating() != null ? t2.getTechnician().getAverageRating() : 0.0;
                return Double.compare(r2, r1); // Descending
            case "price":
                double p1 = t1.getTechnician().getBaseServiceFee() != null ? t1.getTechnician().getBaseServiceFee() : Double.MAX_VALUE;
                double p2 = t2.getTechnician().getBaseServiceFee() != null ? t2.getTechnician().getBaseServiceFee() : Double.MAX_VALUE;
                return Double.compare(p1, p2); // Ascending
            case "name":
                String name1 = t1.getTechnician().getFullName();
                String name2 = t2.getTechnician().getFullName();
                if (name1 == null && name2 == null) return 0;
                if (name1 == null) return 1;
                if (name2 == null) return -1;
                return name1.compareToIgnoreCase(name2);
            default:
                return Double.compare(t1.getDistance(), t2.getDistance());
        }
    }
    
    private int sortTechniciansWithoutDistance(Technician t1, Technician t2, String sortBy) {
        if (t1 == null || t2 == null) return 0;
        
        switch (sortBy) {
            case "rating":
                double r1 = t1.getAverageRating() != null ? t1.getAverageRating() : 0.0;
                double r2 = t2.getAverageRating() != null ? t2.getAverageRating() : 0.0;
                return Double.compare(r2, r1); // Descending
            case "price":
                double p1 = t1.getBaseServiceFee() != null ? t1.getBaseServiceFee() : Double.MAX_VALUE;
                double p2 = t2.getBaseServiceFee() != null ? t2.getBaseServiceFee() : Double.MAX_VALUE;
                return Double.compare(p1, p2); // Ascending
            case "name":
            case "distance": // Handle distance as name when no location provided
            default:
                String name1 = t1.getFullName();
                String name2 = t2.getFullName();
                if (name1 == null && name2 == null) return 0;
                if (name1 == null) return 1;
                if (name2 == null) return -1;
                return name1.compareToIgnoreCase(name2);
        }
    }
    
    // DTOs
    public static class SearchResult {
        private List<Technician> technicians;
        private int totalElements;
        private int currentPage;
        private int pageSize;
        private int totalPages;
        
        public SearchResult(List<Technician> technicians, int totalElements, int currentPage, int pageSize, int totalPages) {
            this.technicians = technicians;
            this.totalElements = totalElements;
            this.currentPage = currentPage;
            this.pageSize = pageSize;
            this.totalPages = totalPages;
        }
        
        public List<Technician> getTechnicians() { return technicians; }
        public int getTotalElements() { return totalElements; }
        public int getCurrentPage() { return currentPage; }
        public int getPageSize() { return pageSize; }
        public int getTotalPages() { return totalPages; }
    }
    
    public static class TechnicianProfile {
        private Technician technician;
        private List<Review> reviews;
        
        public TechnicianProfile(Technician technician, List<Review> reviews) {
            this.technician = technician;
            this.reviews = reviews;
        }
        
        public Technician getTechnician() { return technician; }
        public List<Review> getReviews() { return reviews; }
    }
    
    public static class TechnicianWithDistance {
        private Technician technician;
        private double distance;
        
        public TechnicianWithDistance(Technician technician, double distance) {
            this.technician = technician;
            this.distance = distance;
        }
        
        public Technician getTechnician() { return technician; }
        public double getDistance() { return distance; }
    }
    
    public static class ContactRequest {
        private String contactMethod;
        private String message;
        
        public String getContactMethod() { return contactMethod; }
        public void setContactMethod(String contactMethod) { this.contactMethod = contactMethod; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
    
    public static class ContactResponse {
        private boolean allowed;
        private String phoneNumber;
        private String whatsappNumber;
        private String whatsappLink;
        private Long messageThreadId;
        private String message;
        
        public boolean isAllowed() { return allowed; }
        public void setAllowed(boolean allowed) { this.allowed = allowed; }
        public String getPhoneNumber() { return phoneNumber; }
        public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
        public String getWhatsappNumber() { return whatsappNumber; }
        public void setWhatsappNumber(String whatsappNumber) { this.whatsappNumber = whatsappNumber; }
        public String getWhatsappLink() { return whatsappLink; }
        public void setWhatsappLink(String whatsappLink) { this.whatsappLink = whatsappLink; }
        public Long getMessageThreadId() { return messageThreadId; }
        public void setMessageThreadId(Long messageThreadId) { this.messageThreadId = messageThreadId; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
    
    public static class ErrorResponse {
        private String error;
        
        public ErrorResponse(String error) {
            this.error = error;
        }
        
        public String getError() { return error; }
    }
    
    public static class SuccessResponse {
        private String message;
        
        public SuccessResponse(String message) {
            this.message = message;
        }
        
        public String getMessage() { return message; }
    }
}