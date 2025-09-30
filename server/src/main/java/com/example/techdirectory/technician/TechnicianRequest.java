package com.example.techdirectory.technician;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalTime;
import java.util.Set;

@Data
public class TechnicianRequest {
    
    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    private String fullName;
    
    @NotBlank(message = "Phone is required")
    @Pattern(regexp = "^[+]?[0-9]{10,15}$", message = "Invalid phone number format")
    private String phone;
    
    @Email(message = "Invalid email format")
    private String email;
    
    @Size(max = 1000, message = "Summary too long")
    private String summary;
    
    @Size(max = 2000, message = "Description too long")
    private String description;
    
    @NotNull(message = "Region is required")
    private Long regionId;
    
    @NotEmpty(message = "At least one field is required")
    private Set<Long> fieldIds;
    
    @Min(value = 0, message = "Experience years cannot be negative")
    @Max(value = 50, message = "Experience years seems unrealistic")
    private Integer experienceYears;
    
    @DecimalMin(value = "0.0", message = "Service fee cannot be negative")
    private Double baseServiceFee;
    
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be a 3-letter ISO code")
    private String currency = "EGP";
    
    private Technician.PriceType priceType;
    
    private Boolean allowDirectCalls;
    
    private Boolean allowWhatsApp;
    
    private Boolean allowInAppMessages;
    
    @Pattern(regexp = "^[+]?[0-9]{10,15}$", message = "Invalid WhatsApp number format")
    private String whatsappNumber;
    
    private LocalTime workingHoursStart;
    
    private LocalTime workingHoursEnd;
    
    private Set<Technician.DayOfWeek> workingDays;
    
    @DecimalMin(value = "-90.0", message = "Latitude must be >= -90")
    @DecimalMax(value = "90.0", message = "Latitude must be <= 90")
    private Double lat;
    
    @DecimalMin(value = "-180.0", message = "Longitude must be >= -180")
    @DecimalMax(value = "180.0", message = "Longitude must be <= 180")
    private Double lng;
    
    @Size(max = 500, message = "Address too long")
    private String address;
    
    @Size(max = 500, message = "Certification description too long")
    private String certification;
    
    private Boolean isEmergencyService;
    
    @DecimalMin(value = "1.0", message = "Emergency fee multiplier must be >= 1.0")
    @DecimalMax(value = "5.0", message = "Emergency fee multiplier must be <= 5.0")
    private Double emergencyFeeMultiplier;
}