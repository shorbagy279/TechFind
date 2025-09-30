package com.example.techdirectory.payment.dto;

import jakarta.validation.constraints.*;

public class PaymentRequest {
    
    private Long bookingId;
    
    @NotNull(message = "Technician ID is required")
    private Long technicianId;
    
    private Long userId;
    
    @Email(message = "Invalid email format")
    private String customerEmail;
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private Double amount;
    
    @NotBlank(message = "Currency is required")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be a 3-letter ISO code")
    private String currency = "EGP";
    
    @NotBlank(message = "Service name is required")
    @Size(max = 200, message = "Service name too long")
    private String serviceName;
    
    @Size(max = 500, message = "Description too long")
    private String description;
    
    @Size(max = 100, message = "Service type too long")
    private String serviceType;
    
    @Pattern(regexp = "^(stripe|paymob)$", message = "Payment provider must be 'stripe' or 'paymob'")
    private String paymentProvider = "stripe";

    public PaymentRequest() {}

    // Getters
    public Long getBookingId() { return bookingId; }
    public Long getTechnicianId() { return technicianId; }
    public Long getUserId() { return userId; }
    public String getCustomerEmail() { return customerEmail; }
    public Double getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public String getServiceName() { return serviceName; }
    public String getDescription() { return description; }
    public String getServiceType() { return serviceType; }
    public String getPaymentProvider() { return paymentProvider; }

    // Setters
    public void setBookingId(Long bookingId) { this.bookingId = bookingId; }
    public void setTechnicianId(Long technicianId) { this.technicianId = technicianId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }
    public void setAmount(Double amount) { this.amount = amount; }
    public void setCurrency(String currency) { 
        this.currency = currency != null ? currency.toUpperCase() : "EGP"; 
    }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }
    public void setDescription(String description) { this.description = description; }
    public void setServiceType(String serviceType) { this.serviceType = serviceType; }
    public void setPaymentProvider(String paymentProvider) { 
        this.paymentProvider = paymentProvider != null ? paymentProvider.toLowerCase() : "stripe"; 
    }

    @Override
    public String toString() {
        return "PaymentRequest{" +
                "bookingId=" + bookingId +
                ", technicianId=" + technicianId +
                ", userId=" + userId +
                ", amount=" + amount +
                ", currency='" + currency + '\'' +
                ", serviceName='" + serviceName + '\'' +
                ", paymentProvider='" + paymentProvider + '\'' +
                '}';
    }
}