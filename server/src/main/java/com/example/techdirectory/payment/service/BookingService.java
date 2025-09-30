package com.example.techdirectory.payment.service;

import com.example.techdirectory.payment.dto.PaymentRequest;
import com.example.techdirectory.payment.model.Booking;
import com.example.techdirectory.payment.model.BookingStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class BookingService {

    private static final Logger logger = LoggerFactory.getLogger(BookingService.class);

    // In-memory storage until you implement BookingRepository
    private final Map<Long, Booking> bookingStore = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    // TODO: Inject BookingRepository when you create it
    // private final BookingRepository bookingRepository;
    // private final TechnicianRepository technicianRepository;
    
    // public BookingService(BookingRepository bookingRepository, TechnicianRepository technicianRepository) {
    //     this.bookingRepository = bookingRepository;
    //     this.technicianRepository = technicianRepository;
    // }

    @Transactional
    public Booking createBooking(PaymentRequest request) {
        try {
            // Validate request
            validateBookingRequest(request);
            
            // TODO: Verify technician exists
            // Technician technician = technicianRepository.findById(request.getTechnicianId())
            //     .orElseThrow(() -> new RuntimeException("Technician not found"));
            
            // Create booking
            Booking booking = new Booking();
            booking.setId(idGenerator.getAndIncrement());
            booking.setTechnicianId(request.getTechnicianId());
            booking.setUserId(request.getUserId());
            booking.setServiceType(request.getServiceType());
            booking.setAmount(request.getAmount());
            booking.setCurrency(request.getCurrency());
            booking.setStatus(BookingStatus.PENDING_PAYMENT);
            booking.setCreatedAt(LocalDateTime.now());
            booking.setUpdatedAt(LocalDateTime.now());
            booking.setPaymentMethod(request.getPaymentProvider());

            // Save to in-memory store (replace with repository save)
            bookingStore.put(booking.getId(), booking);
            
            // TODO: Persist booking
            // booking = bookingRepository.save(booking);

            logger.info("Booking created: ID={}, TechnicianID={}, Amount={}", 
                booking.getId(), booking.getTechnicianId(), booking.getAmount());

            return booking;
            
        } catch (Exception e) {
            logger.error("Failed to create booking", e);
            throw new RuntimeException("Failed to create booking: " + e.getMessage(), e);
        }
    }

    @Transactional
    public void updatePaymentStatus(String transactionId, String status) {
        try {
            // TODO: Find booking by transaction ID and update
            // Booking booking = bookingRepository.findByTransactionId(transactionId)
            //     .orElseThrow(() -> new RuntimeException("Booking not found"));
            
            BookingStatus newStatus;
            switch (status.toLowerCase()) {
                case "success":
                case "completed":
                case "true":
                    newStatus = BookingStatus.PAYMENT_CONFIRMED;
                    break;
                case "pending":
                case "false":
                    newStatus = BookingStatus.PENDING_PAYMENT;
                    break;
                case "failed":
                case "cancelled":
                    newStatus = BookingStatus.CANCELLED;
                    break;
                default:
                    logger.warn("Unknown payment status: {}", status);
                    return;
            }
            
            // booking.setStatus(newStatus);
            // booking.setUpdatedAt(LocalDateTime.now());
            // bookingRepository.save(booking);
            
            logger.info("Payment status updated for transaction: {} to {}", transactionId, newStatus);
            
        } catch (Exception e) {
            logger.error("Failed to update payment status for transaction: {}", transactionId, e);
            throw new RuntimeException("Failed to update payment status", e);
        }
    }

    public Booking getBookingById(Long id) {
        // TODO: Replace with repository
        // return bookingRepository.findById(id)
        //     .orElseThrow(() -> new RuntimeException("Booking not found"));
        
        Booking booking = bookingStore.get(id);
        if (booking == null) {
            throw new RuntimeException("Booking not found with ID: " + id);
        }
        return booking;
    }

    @Transactional
    public Booking updateBookingStatus(Long bookingId, BookingStatus newStatus) {
        try {
            Booking booking = getBookingById(bookingId);
            
            // Validate status transition
            if (!isValidStatusTransition(booking.getStatus(), newStatus)) {
                throw new IllegalStateException(
                    String.format("Invalid status transition from %s to %s", 
                        booking.getStatus(), newStatus));
            }
            
            booking.setStatus(newStatus);
            booking.setUpdatedAt(LocalDateTime.now());
            
            // TODO: Save to repository
            // booking = bookingRepository.save(booking);
            bookingStore.put(booking.getId(), booking);
            
            logger.info("Booking status updated: ID={}, Status={}", bookingId, newStatus);
            
            return booking;
            
        } catch (Exception e) {
            logger.error("Failed to update booking status: {}", bookingId, e);
            throw new RuntimeException("Failed to update booking status", e);
        }
    }

    private void validateBookingRequest(PaymentRequest request) {
        if (request.getTechnicianId() == null) {
            throw new IllegalArgumentException("Technician ID is required");
        }
        
        if (request.getUserId() == null) {
            throw new IllegalArgumentException("User ID is required");
        }
        
        if (request.getAmount() == null || request.getAmount() <= 0) {
            throw new IllegalArgumentException("Invalid amount");
        }
        
        if (request.getCurrency() == null || request.getCurrency().isBlank()) {
            throw new IllegalArgumentException("Currency is required");
        }
    }

    private boolean isValidStatusTransition(BookingStatus current, BookingStatus next) {
        if (current == null || next == null) {
            return false;
        }
        
        // Define valid status transitions
        switch (current) {
            case PENDING_PAYMENT:
                return next == BookingStatus.PAYMENT_CONFIRMED || 
                       next == BookingStatus.CANCELLED;
                       
            case PAYMENT_CONFIRMED:
                return next == BookingStatus.SCHEDULED || 
                       next == BookingStatus.CANCELLED;
                       
            case SCHEDULED:
                return next == BookingStatus.IN_PROGRESS || 
                       next == BookingStatus.CANCELLED;
                       
            case IN_PROGRESS:
                return next == BookingStatus.COMPLETED || 
                       next == BookingStatus.CANCELLED;
                       
            case COMPLETED:
                return next == BookingStatus.REFUNDED;
                       
            case CANCELLED:
            case REFUNDED:
                return false; // Terminal states
                
            default:
                return false;
        }
    }
}