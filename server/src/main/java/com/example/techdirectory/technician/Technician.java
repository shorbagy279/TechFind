package com.example.techdirectory.technician;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Set;
import java.util.List;

@Entity 
@Getter 
@Setter 
@NoArgsConstructor 
@AllArgsConstructor 
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Technician {
    @Id 
    @GeneratedValue(strategy=GenerationType.IDENTITY) 
    private Long id;
    
    // Basic Information
    private String fullName;
    private String phone;
    private String email;
    
    @Column(length = 1000)
    private String summary;
    
    @Column(length = 2000)
    private String description;
    
    // Location Information
    @ManyToOne(fetch = FetchType.EAGER)
    @JsonIgnoreProperties({"technicians"})
    private Region region;
    
    private Double lat;
    private Double lng;
    private String address;
    
    // Professional Information
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name="technician_fields",
        joinColumns=@JoinColumn(name="technician_id"),
        inverseJoinColumns=@JoinColumn(name="field_id"))
    @JsonIgnoreProperties({"technicians"})
    private Set<Field> fields;
    
    private Integer experienceYears;
    private String certification;
    
    // Availability
    @Builder.Default
    private Boolean active = true;
    
    @Builder.Default
    private Boolean available = true;
    
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private AvailabilityStatus availabilityStatus = AvailabilityStatus.AVAILABLE;
    
    // Working Hours
    private LocalTime workingHoursStart;
    private LocalTime workingHoursEnd;
    
    @ElementCollection
    @Enumerated(EnumType.STRING)
    private Set<DayOfWeek> workingDays;
    
    // Profile Media
    private String profilePhotoUrl;
    
    @ElementCollection
    private List<String> portfolioImageUrls;
    
    // Ratings and Reviews
    @Builder.Default
    private Double averageRating = 0.0;
    
    @Builder.Default
    private Integer totalReviews = 0;
    
    // Pricing
    private Double baseServiceFee;
    private String currency;
    
    @Enumerated(EnumType.STRING)
    private PriceType priceType;
    
    // Contact Preferences
    @Builder.Default
    private Boolean allowDirectCalls = false;
    
    @Builder.Default
    private Boolean allowWhatsApp = false;
    
    @Builder.Default
    private Boolean allowInAppMessages = false;
    
    private String whatsappNumber;
    
    // Metadata
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    private LocalDateTime lastActiveAt;
    
    @Builder.Default
    private Boolean isVerified = false;
    
    private String verificationDocumentUrl;
    
    // Emergency Contact
    @Builder.Default
    private Boolean isEmergencyService = false;
    
    private Double emergencyFeeMultiplier;
    
    public enum AvailabilityStatus {
        AVAILABLE, BUSY, OFFLINE
    }
    
    public enum PriceType {
        FIXED, HOURLY, NEGOTIABLE
    }
    
    public enum DayOfWeek {
        MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY
    }
    
    // Helper method to calculate distance from user
    public double calculateDistanceFrom(Double userLat, Double userLng) {
        if (lat == null || lng == null || userLat == null || userLng == null) {
            return Double.MAX_VALUE;
        }
        
        final int R = 6371;
        
        double latDistance = Math.toRadians(userLat - lat);
        double lonDistance = Math.toRadians(userLng - lng);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat)) * Math.cos(Math.toRadians(userLat))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
    
    public boolean isCurrentlyAvailable() {
        if (active == null || !active || available == null || !available || 
            availabilityStatus != AvailabilityStatus.AVAILABLE) {
            return false;
        }
        
        LocalTime now = LocalTime.now();
        java.time.DayOfWeek currentDay = java.time.LocalDate.now().getDayOfWeek();
        
        DayOfWeek today = DayOfWeek.valueOf(currentDay.name());
        
        return workingDays != null && workingDays.contains(today) &&
               workingHoursStart != null && workingHoursEnd != null &&
               now.isAfter(workingHoursStart) && now.isBefore(workingHoursEnd);
    }
}