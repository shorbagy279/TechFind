package com.example.techdirectory.technician;

import lombok.Data;
import java.time.LocalTime;
import java.util.Set;

@Data
public class CreateTechnicianRequest {
    private String fullName;
    private String phone;
    private String email;
    private String summary;
    private String description;
    private Long regionId;
    private Set<Long> fieldIds;
    private Integer experienceYears;
    private Double baseServiceFee;
    private String currency;
    private Technician.PriceType priceType;
    private Boolean allowDirectCalls;
    private Boolean allowWhatsApp;
    private Boolean allowInAppMessages;
    private String whatsappNumber;
    private LocalTime workingHoursStart;
    private LocalTime workingHoursEnd;
    private Set<Technician.DayOfWeek> workingDays;
    private Double lat;
    private Double lng;
    private String address;
    private String certification;
    private Boolean isEmergencyService;
    private Double emergencyFeeMultiplier;
}