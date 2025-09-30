package com.example.techdirectory.payment.model;

import java.time.LocalDateTime;

public class Booking {
    private Long id;
    private Long technicianId;
    private Long userId;
    private String serviceType;
    private Double amount;
    private String currency;
    private BookingStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String transactionId;
    private String paymentMethod;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getTechnicianId() { return technicianId; }
    public void setTechnicianId(Long technicianId) { this.technicianId = technicianId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getServiceType() { return serviceType; }
    public void setServiceType(String serviceType) { this.serviceType = serviceType; }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public BookingStatus getStatus() { return status; }
    public void setStatus(BookingStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
} 
