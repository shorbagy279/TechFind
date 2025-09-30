package com.example.techdirectory.payment;

import com.example.techdirectory.payment.dto.PaymentRequest;
import com.example.techdirectory.payment.dto.PaymentResult;
import com.example.techdirectory.payment.model.Booking;
import com.example.techdirectory.payment.service.BookingService;
import com.example.techdirectory.user.User;
import com.example.techdirectory.user.UserRepository;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"}, allowCredentials = "true")
public class PaymentController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);

    private final PaymentService paymentService;
    private final BookingService bookingService;
    private final UserRepository userRepository;

    public PaymentController(
            PaymentService paymentService, 
            BookingService bookingService,
            UserRepository userRepository) {
        this.paymentService = paymentService;
        this.bookingService = bookingService;
        this.userRepository = userRepository;
    }

    @PostMapping("/create-session")
    public ResponseEntity<?> createPaymentSession(@Valid @RequestBody PaymentRequest request) {
        try {
            // Get authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName();
            
            User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Set user ID from authenticated user
            request.setUserId(user.getId());
            request.setCustomerEmail(user.getEmail());
            
            // Validate payment request
            if (request.getTechnicianId() == null) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Technician ID is required"));
            }
            
            if (request.getAmount() == null || request.getAmount() <= 0) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid amount"));
            }
            
            if (request.getServiceName() == null || request.getServiceName().isBlank()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Service name is required"));
            }
            
            // Validate payment provider
            String provider = request.getPaymentProvider();
            if (provider == null || (!provider.equalsIgnoreCase("stripe") && !provider.equalsIgnoreCase("paymob"))) {
                request.setPaymentProvider("stripe"); // Default to stripe
            }
            
            // Create booking
            Booking booking = bookingService.createBooking(request);
            request.setBookingId(booking.getId());

            // Create payment session
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
            
            logger.info("Payment session created for user: {}, booking: {}", user.getEmail(), booking.getId());
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            logger.error("Invalid payment request: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(Map.of("status", "error", "message", e.getMessage()));
        } catch (Exception e) {
            logger.error("Failed to create payment session", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("status", "error", "message", "Failed to create payment session"));
        }
    }

    @PostMapping("/webhook/stripe")
    public ResponseEntity<Map<String, String>> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader(value = "Stripe-Signature", required = false) String sigHeader) {
        
        try {
            if (sigHeader == null || sigHeader.isBlank()) {
                logger.warn("Stripe webhook received without signature");
                return ResponseEntity.badRequest()
                    .body(Map.of("status", "error", "message", "Missing signature"));
            }
            
            PaymentResult result = paymentService.processWebhook(payload, sigHeader);
            
            Map<String, String> response = new HashMap<>();
            response.put("status", result.isSuccess() ? "success" : "failed");
            response.put("message", result.getMessage());
            
            HttpStatus status = result.isSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status).body(response);
            
        } catch (Exception e) {
            logger.error("Stripe webhook processing failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("status", "error", "message", "Webhook processing failed"));
        }
    }

    @PostMapping("/webhook/paymob")
    public ResponseEntity<Map<String, String>> handlePayMobWebhook(@RequestBody Map<String, Object> payload) {
        try {
            if (payload == null || payload.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("status", "error", "message", "Empty payload"));
            }
            
            String transactionId = payload.get("id") != null ? String.valueOf(payload.get("id")) : null;
            String status = payload.get("pending") != null ? String.valueOf(payload.get("pending")) : null;
            
            if (transactionId == null || status == null) {
                return ResponseEntity.badRequest()
                    .body(Map.of("status", "error", "message", "Invalid payload"));
            }
            
            bookingService.updatePaymentStatus(transactionId, status);
            
            logger.info("PayMob webhook processed for transaction: {}", transactionId);
            
            return ResponseEntity.ok(Map.of("status", "success"));
            
        } catch (Exception e) {
            logger.error("PayMob webhook processing failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("status", "error", "message", "Webhook processing failed"));
        }
    }

    @GetMapping("/success")
    public ResponseEntity<?> paymentSuccess(
            @RequestParam(value = "session_id", required = false) String sessionId) {
        
        if (sessionId == null || sessionId.isBlank()) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Missing session ID"));
        }
        
        logger.info("Payment success callback for session: {}", sessionId);
        
        return ResponseEntity.ok(Map.of(
            "status", "success",
            "message", "Payment completed successfully",
            "sessionId", sessionId
        ));
    }

    @GetMapping("/cancel")
    public ResponseEntity<?> paymentCancel() {
        logger.info("Payment cancelled by user");
        
        return ResponseEntity.ok(Map.of(
            "status", "cancelled",
            "message", "Payment was cancelled"
        ));
    }
}