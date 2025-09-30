package com.example.techdirectory.technician;

import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"}, allowCredentials = "true")
public class TechnicianController {
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

    // Enhanced search with location and advanced filters
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
            Pageable pageable = PageRequest.of(page, size);
            List<Technician> technicians;
            
            if (governorate != null && !governorate.isEmpty()) {
                technicians = technicianRepository.searchWithFilters(
                    governorate, city, field, minRating, maxPrice, availableNow, emergencyService);
            } else {
                technicians = technicianRepository.findAllActiveWithFilters(
                    minRating, maxPrice, availableNow, emergencyService);
            }
            
            // Filter out technicians with null values that could cause issues
            technicians = technicians.stream()
                .filter(tech -> tech != null && tech.getFullName() != null)
                .collect(Collectors.toList());
            
            if (userLat != null && userLng != null) {
                technicians = technicians.stream()
                    .map(tech -> new TechnicianWithDistance(tech, tech.calculateDistanceFrom(userLat, userLng)))
                    .filter(techDist -> maxDistance == null || techDist.getDistance() <= maxDistance)
                    .sorted((t1, t2) -> {
                        switch (sortBy) {
                            case "distance": return Double.compare(t1.getDistance(), t2.getDistance());
                            case "rating": 
                                double r1 = t1.getTechnician().getAverageRating() != null ? t1.getTechnician().getAverageRating() : 0.0;
                                double r2 = t2.getTechnician().getAverageRating() != null ? t2.getTechnician().getAverageRating() : 0.0;
                                return Double.compare(r2, r1);
                            case "price": return Double.compare(
                                t1.getTechnician().getBaseServiceFee() != null ? t1.getTechnician().getBaseServiceFee() : Double.MAX_VALUE,
                                t2.getTechnician().getBaseServiceFee() != null ? t2.getTechnician().getBaseServiceFee() : Double.MAX_VALUE
                            );
                            case "name": return t1.getTechnician().getFullName().compareToIgnoreCase(t2.getTechnician().getFullName());
                            default: return Double.compare(t1.getDistance(), t2.getDistance());
                        }
                    })
                    .map(TechnicianWithDistance::getTechnician)
                    .collect(Collectors.toList());
            } else {
                technicians = technicians.stream()
                    .sorted((t1, t2) -> {
                        switch (sortBy) {
                            case "rating": 
                                double r1 = t1.getAverageRating() != null ? t1.getAverageRating() : 0.0;
                                double r2 = t2.getAverageRating() != null ? t2.getAverageRating() : 0.0;
                                return Double.compare(r2, r1);
                            case "price": return Double.compare(
                                t1.getBaseServiceFee() != null ? t1.getBaseServiceFee() : Double.MAX_VALUE,
                                t2.getBaseServiceFee() != null ? t2.getBaseServiceFee() : Double.MAX_VALUE
                            );
                            case "name": return t1.getFullName().compareToIgnoreCase(t2.getFullName());
                            default: return t1.getFullName().compareToIgnoreCase(t2.getFullName());
                        }
                    })
                    .collect(Collectors.toList());
            }
            
            int start = page * size;
            int end = Math.min(start + size, technicians.size());
            List<Technician> pagedTechnicians = start < technicians.size() ? 
                technicians.subList(start, end) : List.of();
            
            return ResponseEntity.ok(new SearchResult(
                pagedTechnicians,
                technicians.size(),
                page,
                size,
                (int) Math.ceil((double) technicians.size() / size)
            ));
        } catch (Exception e) {
            e.printStackTrace(); // This will show the error in console
            return ResponseEntity.status(500).body(
                new ErrorResponse("Error searching technicians: " + e.getMessage())
            );
        }
    }

    @GetMapping("/public/technicians/{id}")
    public TechnicianProfile getTechnicianProfile(@PathVariable Long id) {
        Technician technician = technicianRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Technician not found"));
        List<Review> reviews = reviewRepository.findByTechnicianIdOrderByCreatedAtDesc(id);
        return new TechnicianProfile(technician, reviews);
    }

    @PostMapping("/user/technicians/{id}/contact")
    public ContactResponse contactTechnician(
            @PathVariable Long id,
            @RequestBody ContactRequest request
    ) {
        Technician technician = technicianRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Technician not found"));
        
        ContactResponse response = new ContactResponse();
        
        if (technician.getAllowDirectCalls() && "call".equals(request.getContactMethod())) {
            response.setPhoneNumber(technician.getPhone());
            response.setAllowed(true);
        } else if (technician.getAllowWhatsApp() && "whatsapp".equals(request.getContactMethod())) {
            response.setWhatsappNumber(technician.getWhatsappNumber());
            response.setWhatsappLink("https://wa.me/" + technician.getWhatsappNumber().replaceAll("[^0-9]", ""));
            response.setAllowed(true);
        } else if (technician.getAllowInAppMessages() && "message".equals(request.getContactMethod())) {
            response.setMessageThreadId(createMessageThread(technician.getId()));
            response.setAllowed(true);
        } else {
            response.setAllowed(false);
            response.setMessage("Contact method not available");
        }
        
        return response;
    }

    @PostMapping("/admin/technicians") 
    public Technician createTechnician(@RequestBody CreateTechnicianRequest request) { 
        Technician technician = Technician.builder()
            .fullName(request.getFullName())
            .phone(request.getPhone())
            .email(request.getEmail())
            .summary(request.getSummary())
            .description(request.getDescription())
            .region(regionRepository.findById(request.getRegionId())
                .orElseThrow(() -> new RuntimeException("Region not found")))
            .fields(request.getFieldIds().stream()
                .map(fieldId -> fieldRepository.findById(fieldId)
                    .orElseThrow(() -> new RuntimeException("Field not found: " + fieldId)))
                .collect(Collectors.toSet()))
            .experienceYears(request.getExperienceYears())
            .baseServiceFee(request.getBaseServiceFee())
            .currency(request.getCurrency())
            .priceType(request.getPriceType())
            .allowDirectCalls(request.getAllowDirectCalls())
            .allowWhatsApp(request.getAllowWhatsApp())
            .allowInAppMessages(request.getAllowInAppMessages())
            .whatsappNumber(request.getWhatsappNumber())
            .workingHoursStart(request.getWorkingHoursStart())
            .workingHoursEnd(request.getWorkingHoursEnd())
            .workingDays(request.getWorkingDays())
            .lat(request.getLat())
            .lng(request.getLng())
            .address(request.getAddress())
            .certification(request.getCertification())
            .isEmergencyService(request.getIsEmergencyService())
            .emergencyFeeMultiplier(request.getEmergencyFeeMultiplier())
            .active(true)
            .available(true)
            .availabilityStatus(Technician.AvailabilityStatus.AVAILABLE)
            .build();
            
        return technicianRepository.save(technician); 
    }
    
    @PutMapping("/admin/technicians/{id}") 
    public Technician updateTechnician(@PathVariable Long id, @RequestBody Technician t) { 
        t.setId(id); 
        return technicianRepository.save(t); 
    }
    
    @DeleteMapping("/admin/technicians/{id}") 
    public void deleteTechnician(@PathVariable Long id) { 
        technicianRepository.deleteById(id); 
    }

    private Long createMessageThread(Long technicianId) {
        return 1L;
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
        public String getMessage() { return message; }
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
}