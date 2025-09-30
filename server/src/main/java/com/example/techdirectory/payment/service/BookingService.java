package com.example.techdirectory.payment.service;

import com.example.techdirectory.payment.dto.PaymentRequest;
import com.example.techdirectory.payment.model.Booking;
import com.example.techdirectory.payment.model.BookingStatus;
import org.springframework.stereotype.Service;

@Service
public class BookingService {

    // TODO: inject BookingRepository when you persist bookings
    public Booking createBooking(PaymentRequest request) {
        Booking booking = new Booking();
        booking.setTechnicianId(request.getTechnicianId());
        booking.setUserId(request.getUserId());
        booking.setServiceType(request.getServiceType());
        booking.setAmount(request.getAmount());
        booking.setCurrency(request.getCurrency());
        booking.setStatus(BookingStatus.PENDING_PAYMENT);
        booking.setCreatedAt(java.time.LocalDateTime.now());

        // Persist booking (uncomment when repository is available)
        // return bookingRepository.save(booking);

        booking.setId(System.currentTimeMillis()); // mock id for now
        return booking;
    }

    public void updatePaymentStatus(String transactionId, String status) {
        // TODO: find booking by transactionId and update status accordingly
    }
}