package com.example.techdirectory.payment;
import com.example.techdirectory.payment.dto.PaymentRequest;
import com.example.techdirectory.payment.dto.PaymentResult;
import com.example.techdirectory.payment.model.Booking;
import com.example.techdirectory.payment.service.BookingService;
import com.stripe.exception.SignatureVerificationException;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin
public class PaymentController {

    private final PaymentService paymentService;
    private final BookingService bookingService;

    public PaymentController(PaymentService paymentService, BookingService bookingService) {
        this.paymentService = paymentService;
        this.bookingService = bookingService;
    }

    @PostMapping("/create-session")
    public ResponseEntity<Map<String, String>> createPaymentSession(@RequestBody PaymentRequest request) {
        try {
            // create booking record first
            Booking booking = bookingService.createBooking(request);
            request.setBookingId(booking.getId());

            String sessionUrl;
            if ("stripe".equalsIgnoreCase(request.getPaymentProvider())) {
                sessionUrl = paymentService.createCheckoutSession(request);
            } else if ("paymob".equalsIgnoreCase(request.getPaymentProvider())) {
                sessionUrl = paymentService.createPayMobSession(request);
            } else {
                throw new IllegalArgumentException("Unsupported payment provider");
            }

            Map<String, String> response = new HashMap<>();
            response.put("sessionUrl", sessionUrl);
            response.put("bookingId", booking.getId().toString());
            response.put("status", "created");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            throw new RuntimeException("Payment session creation failed: " + e.getMessage(), e);
        }
    }

    @PostMapping("/webhook/stripe")
    public ResponseEntity<Map<String, String>> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {

        PaymentResult result = paymentService.processWebhook(payload, sigHeader);
        Map<String, String> response = new HashMap<>();
        response.put("status", result.isSuccess() ? "success" : "failed");
        response.put("message", result.getMessage());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/webhook/paymob")
    public ResponseEntity<Map<String, String>> handlePayMobWebhook(@RequestBody Map<String, Object> payload) {
        try {
            String transactionId = String.valueOf(payload.get("id"));
            String status = String.valueOf(payload.get("pending")); // adapt mapping
            bookingService.updatePaymentStatus(transactionId, status);
            return ResponseEntity.ok(Map.of("status", "success"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("status", "error", "message", e.getMessage()));
        }
    }

    @GetMapping("/success")
    public ResponseEntity<String> paymentSuccess(@RequestParam("session_id") String sessionId) {
        // Redirect page handling usually done in frontend; here return a simple message or redirect
        return ResponseEntity.ok("Payment success, session: " + sessionId);
    }

    @GetMapping("/cancel")
    public ResponseEntity<String> paymentCancel() {
        return ResponseEntity.ok("Payment cancelled");
    }
}
