package com.example.techdirectory.payment;

import com.example.techdirectory.payment.dto.PaymentRequest;
import com.example.techdirectory.payment.dto.PaymentResult;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class PaymentService {

    @Value("${stripe.secret.key}")
    private String stripeSecretKey;

    @Value("${stripe.publishable.key}")
    private String stripePublishableKey;

    @Value("${app.base.url}")
    private String baseUrl;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
    }

    public String createCheckoutSession(PaymentRequest request) throws StripeException {
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
                                                .setUnitAmount(Math.round(request.getAmount() * 100)) // cents
                                                .setProductData(
                                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                .setName(request.getServiceName())
                                                                .setDescription(request.getDescription())
                                                                .build()
                                                )
                                                .build()
                                )
                                .build()
                );

        // Add customer email if provided
        if (request.getCustomerEmail() != null && !request.getCustomerEmail().isBlank()) {
            paramsBuilder.setCustomerEmail(request.getCustomerEmail());
        }

        // Add metadata safely (Stripe builder has putMetadata)
        Map<String, String> metadata = Map.of(
                "booking_id", String.valueOf(request.getBookingId()),
                "technician_id", String.valueOf(request.getTechnicianId()),
                "user_id", String.valueOf(request.getUserId()),
                "service_type", request.getServiceType() == null ? "" : request.getServiceType()
        );
        metadata.forEach(paramsBuilder::putMetadata);

        SessionCreateParams params = paramsBuilder.build();
        Session session = Session.create(params);
        return session.getUrl();
    }

    public PaymentResult processWebhook(String payload, String sigHeader) {
        try {
            // TODO: Verify signature with Stripe's library and event parsing, then handle events
            return new PaymentResult(true, "Payment processed successfully");
        } catch (Exception e) {
            return new PaymentResult(false, "Webhook processing failed: " + e.getMessage());
        }
    }

    // PayMob placeholder
    public String createPayMobSession(PaymentRequest request) {
        try {
            return "https://accept.paymobsolutions.com/api/acceptance/iframes/..." + "?payment_token=" + generatePayMobToken(request);
        } catch (Exception e) {
            throw new RuntimeException("PayMob session creation failed", e);
        }
    }

    private String generatePayMobToken(PaymentRequest request) {
        return "paymob_token_" + System.currentTimeMillis();
    }
}