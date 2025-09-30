
package com.example.techdirectory.technician;

import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class TechnicianController {
    private final TechnicianRepository techRepo;
    private final RegionRepository regionRepo;
    private final FieldRepository fieldRepo;
    private final ReviewRepository reviewRepo;

    public TechnicianController(TechnicianRepository t, RegionRepository r, FieldRepository f, ReviewRepository rev) {
        this.techRepo = t;
        this.regionRepo = r;
        this.fieldRepo = f;
        this.reviewRepo = rev;
    }

    // Enhanced search with location and advanced filters
    @GetMapping("/public/technicians/search")
    public SearchResult searchTechnicians(
            @RequestParam(required = false) String governorate,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String field,
            @RequestParam(required = false) Double userLat,
            @RequestParam(required = false) Double userLng,
            @RequestParam(required = false) Integer maxDistance, // in km
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Boolean availableNow,
            @RequestParam(required = false) Boolean emergencyService,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "distance") String sortBy // distance, rating, price, name
    ) {

        Pageable pageable = PageRequest.of(page, size);
        List<Technician> technicians;

        // Get technicians based on location and filters
        if (governorate != null && !governorate.isEmpty()) {
            technicians = techRepo.searchWithFilters(governorate, city, field, minRating, maxPrice, availableNow, emergencyService);
        } else {
            technicians = techRepo.findAllActiveWithFilters(minRating, maxPrice, availableNow, emergencyService);
        }

        // Apply location-based filtering and sorting
        if (userLat != null && userLng != null) {
            technicians = technicians.stream()
                    .map(tech -> {
                        double distance = tech.calculateDistanceFrom(userLat, userLng);
                        // Add distance as a transient field or create a wrapper
                        return new TechnicianWithDistance(tech, distance);
                    })
                    .filter(techDist -> maxDistance == null || techDist.getDistance() <= maxDistance)
                    .sorted((t1, t2) -> {
                        switch (sortBy) {
                            case "distance": return Double.compare(t1.getDistance(), t2.getDistance());
                            case "rating": return Double.compare(t2.getTechnician().getAverageRating(), t1.getTechnician().getAverageRating());
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
            // Sort without distance
            technicians = technicians.stream()
                    .sorted((t1, t2) -> {
                        switch (sortBy) {
                            case "rating": return Double.compare(t2.getAverageRating(), t1.getAverageRating());
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

        // Apply pagination manually since we did custom sorting
        int start = page * size;
        int end = Math.min(start + size, technicians.size());
        List<Technician> pagedTechnicians = technicians.subList(start, end);

        return new SearchResult(
                pagedTechnicians,
                technicians.size(),
                page,
                size,
                (int) Math.ceil((double) technicians.size() / size)
        );
    }

    // Get technician profile with reviews
    @GetMapping("/public/technicians/{id}")
    public TechnicianProfile getTechnicianProfile(@PathVariable Long id) {
        Technician technician = techRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Technician not found"));

        List<Review> reviews = reviewRepo.findByTechnicianIdOrderByCreatedAtDesc(id);

        return new TechnicianProfile(technician, reviews);
    }

    // Contact technician
    @PostMapping("/user/technicians/{id}/contact")
    public ContactResponse contactTechnician(
            @PathVariable Long id,
            @RequestBody ContactRequest request
    ) {
        Technician technician = techRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Technician not found"));

        // Log the contact attempt
        // Send notification to technician
        // Return contact information based on technician's preferences

        ContactResponse response = new ContactResponse();

        if (technician.getAllowDirectCalls() && "call".equals(request.getContactMethod())) {
            response.setPhoneNumber(technician.getPhone());
            response.setAllowed(true);
        } else if (technician.getAllowWhatsApp() && "whatsapp".equals(request.getContactMethod())) {
            response.setWhatsappNumber(technician.getWhatsappNumber());
            response.setWhatsappLink("https://wa.me/" + technician.getWhatsappNumber().replaceAll("[^0-9]", ""));
            response.setAllowed(true);
        } else if (technician.getAllowInAppMessages() && "message".equals(request.getContactMethod())) {
            // Create in-app message thread
            response.setMessageThreadId(createMessageThread(technician.getId()));
            response.setAllowed(true);
        } else {
            response.setAllowed(false);
            response.setMessage("Contact method not available");
        }

        return response;
    }

    // Fix: remove duplicate "/api" prefix and avoid mapping conflict with RegionController
    @GetMapping("/public/technician-fields")
    public List<Field> fields() {
        return fieldRepo.findAll();
    }

    // Fix: avoid duplicate mapping with RegionController by renaming this endpoint
    @GetMapping("/public/technicians/regions")
    public List<Region> regions() {
        return regionRepo.findAll();
    }

    // Admin CRUD operations
    @PostMapping("/admin/technicians")
    public Technician create(@RequestBody CreateTechnicianRequest request) {
        Technician technician = Technician.builder()
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .email(request.getEmail())
                .summary(request.getSummary())
                .description(request.getDescription())
                .region(regionRepo.findById(request.getRegionId()).orElse(null))
                .fields(request.getFieldIds().stream()
                        .map(fieldId -> fieldRepo.findById(fieldId).orElse(null))
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
                .active(true)
                .available(true)
                .availabilityStatus(Technician.AvailabilityStatus.AVAILABLE)
                .build();

        return techRepo.save(technician);
    }

    @PutMapping("/admin/technicians/{id}")
    public Technician update(@PathVariable Long id, @RequestBody Technician t) {
        t.setId(id);
        return techRepo.save(t);
    }

    @DeleteMapping("/admin/technicians/{id}")
    public void delete(@PathVariable Long id) {
        techRepo.deleteById(id);
    }

    private Long createMessageThread(Long technicianId) {
        // Implementation for creating message thread
        return 1L; // Placeholder
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

        // Getters and setters
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
        private String contactMethod; // call, whatsapp, message
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

        // Getters and setters
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
}
