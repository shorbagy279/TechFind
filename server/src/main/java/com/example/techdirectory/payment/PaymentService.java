package com.example.techdirectory.payment;

import com.example.techdirectory.payment.dto.PaymentRequest;
import com.example.techdirectory.payment.dto.PaymentResult;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class PaymentService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);

    @Value("${stripe.secret.key}")
    private String stripeSecretKey;

    @Value("${stripe.webhook.secret:}")
    private String stripeWebhookSecret;

    @Value("${app.base.url}")
    private String baseUrl;

    @PostConstruct
    public void init() {
        if (stripeSecretKey == null || stripeSecretKey.isBlank() || stripeSecretKey.startsWith("sk_test_your")) {
            logger.warn("Stripe secret key not properly configured. Payment processing will fail.");
        } else {
            Stripe.apiKey = stripeSecretKey;
            logger.info("Stripe API initialized successfully");
        }
    }

    public String createCheckoutSession(PaymentRequest request) throws StripeException {
        if (stripeSecretKey == null || stripeSecretKey.startsWith("sk_test_your")) {
            throw new IllegalStateException("Stripe is not properly configured");
        }

        try {
            // Validate request
            validatePaymentRequest(request);

            // Build session parameters
            SessionCreateParams.Builder paramsBuilder = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl(baseUrl + "/payment/success?session_id={CHECKOUT_SESSION_ID}")
                    .setCancelUrl(baseUrl + "/payment/cancel")
                    .addLineItem(
                            SessionCreateParams.LineItem.builder()
                                    .setQuantity(1L)
                                    .setPriceData(
                                            SessionCreateParams.LineItem.PriceData.builder()
                                                    .setCurrency(request.getCurrency().toLowerCase())
                                                    .setUnitAmount(convertToSmallestUnit(request.getAmount(), request.getCurrency()))
                                                    .setProductData(
                                                            SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                    .setName(request.getServiceName())
                                                                    .setDescription(request.getDescription() != null ? 
                                                                        request.getDescription() : "Professional service")
                                                                    .build()
                                                    )
                                                    .build()
                                    )
                                    .build()
                    );

            // Add customer email
            if (request.getCustomerEmail() != null && !request.getCustomerEmail().isBlank()) {
                paramsBuilder.setCustomerEmail(request.getCustomerEmail());
            }

            // Add metadata
            Map<String, String> metadata = new HashMap<>();
            metadata.put("booking_id", String.valueOf(request.getBookingId()));
            metadata.put("technician_id", String.valueOf(request.getTechnicianId()));
            metadata.put("user_id", String.valueOf(request.getUserId()));
            if (request.getServiceType() != null) {
                metadata.put("service_type", request.getServiceType());
            }
            
            metadata.forEach(paramsBuilder::putMetadata);

            // Create session
            SessionCreateParams params = paramsBuilder.build();
            Session session = Session.create(params);
            
            logger.info("Stripe checkout session created: {} for booking: {}", 
                session.getId(), request.getBookingId());
            
            return session.getUrl();
            
        } catch (StripeException e) {
            logger.error("Stripe API error creating checkout session: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error creating checkout session", e);
            throw new RuntimeException("Failed to create payment session", e);
        }
    }

    public PaymentResult processWebhook(String payload, String sigHeader) {
        try {
            if (stripeWebhookSecret == null || stripeWebhookSecret.isBlank()) {
                logger.warn("Stripe webhook secret not configured. Skipping signature verification.");
                return new PaymentResult(true, "Webhook received (signature not verified)");
            }

            // Verify webhook signature
            Event event;
            try {
                event = Webhook.constructEvent(payload, sigHeader, stripeWebhookSecret);
            } catch (Exception e) {
                logger.error("Webhook signature verification failed: {}", e.getMessage());
                return new PaymentResult(false, "Invalid signature");
            }

            // Handle different event types
            switch (event.getType()) {
                case "checkout.session.completed":
                    logger.info("Checkout session completed: {}", event.getId());
                    // TODO: Update booking status to PAYMENT_CONFIRMED
                    break;
                    
                case "checkout.session.expired":
                    logger.info("Checkout session expired: {}", event.getId());
                    // TODO: Update booking status to CANCELLED
                    break;
                    
                case "payment_intent.succeeded":
                    logger.info("Payment intent succeeded: {}", event.getId());
                    break;
                    
                case "payment_intent.payment_failed":
                    logger.info("Payment intent failed: {}", event.getId());
                    // TODO: Update booking status to CANCELLED
                    break;
                    
                default:
                    logger.debug("Unhandled event type: {}", event.getType());
            }

            return new PaymentResult(true, "Webhook processed successfully");
            
        } catch (Exception e) {
            logger.error("Webhook processing error: {}", e.getMessage(), e);
            return new PaymentResult(false, "Webhook processing failed: " + e.getMessage());
        }
    }

    public String createPayMobSession(PaymentRequest request) {
        try {
            validatePaymentRequest(request);
            
            // TODO: Implement actual PayMob integration
            String token = generatePayMobToken(request);
            String iframeId = "YOUR_PAYMOB_IFRAME_ID"; // Should be configured
            
            logger.info("PayMob session created for booking: {}", request.getBookingId());
            
            return String.format("https://accept.paymob.com/api/acceptance/iframes/%s?payment_token=%s", 
                iframeId, token);
                
        } catch (Exception e) {
            logger.error("PayMob session creation failed", e);
            throw new RuntimeException("Failed to create PayMob session", e);
        }
    }

    private String generatePayMobToken(PaymentRequest request) {
        // TODO: Implement actual PayMob token generation
        // This should call PayMob API to get authentication token,
        // register order, and generate payment key
        return "paymob_token_" + System.currentTimeMillis();
    }

    private void validatePaymentRequest(PaymentRequest request) {
        if (request.getAmount() == null || request.getAmount() <= 0) {
            throw new IllegalArgumentException("Invalid amount");
        }
        
        if (request.getCurrency() == null || request.getCurrency().isBlank()) {
            throw new IllegalArgumentException("Currency is required");
        }
        
        if (request.getServiceName() == null || request.getServiceName().isBlank()) {
            throw new IllegalArgumentException("Service name is required");
        }
        
        if (request.getBookingId() == null) {
            throw new IllegalArgumentException("Booking ID is required");
        }
        
        if (request.getTechnicianId() == null) {
            throw new IllegalArgumentException("Technician ID is required");
        }
        
        if (request.getUserId() == null) {
            throw new IllegalArgumentException("User ID is required");
        }
    }

    /**
     * Convert amount to smallest currency unit (e.g., dollars to cents)
     */
    private Long convertToSmallestUnit(Double amount, String currency) {
        // Most currencies use 2 decimal places (cents)
        // Some exceptions: JPY, KRW use 0 decimal places
        String curr = currency.toUpperCase();
        
        if (curr.equals("JPY") || curr.equals("KRW")) {
            return Math.round(amount);
        }
        
        return Math.round(amount * 100);
    }
}