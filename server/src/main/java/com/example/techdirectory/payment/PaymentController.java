package com.example.techdirectory.payment;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class PaymentController {
  // Placeholder: integrate Stripe checkout/session creation
  @PostMapping("/user/payments/create-session")
  public Map<String,String> createSession(@RequestBody Map<String,Object> payload){
    // TODO: call Stripe SDK, return sessionId/url
    return Map.of("status","ok","sessionUrl","https://example.com/checkout-session");
  }
}
