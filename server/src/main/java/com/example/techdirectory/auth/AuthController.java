package com.example.techdirectory.auth;

import com.example.techdirectory.user.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.util.Map;

@RestController 
@RequestMapping("/api/auth")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"}, allowCredentials = "true")
public class AuthController {
  
  private final UserRepository users;
  private final PasswordEncoder encoder;
  private final JwtService jwt;
  
  public AuthController(UserRepository u, PasswordEncoder e, JwtService j) { 
    this.users = u; 
    this.encoder = e; 
    this.jwt = j; 
  }

  public record RegisterRequest(
    @Email String email, 
    @NotBlank String password, 
    @NotBlank String fullName, 
    String phone, 
    String locale
  ) {}
  
  public record AuthResponse(
    String token, 
    String role, 
    String fullName, 
    String email
  ) {}

  @PostMapping("/register")
  public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest req) {
    try {
      if (users.existsByEmail(req.email())) {
        throw new RuntimeException("Email already exists");
      }
      
      var user = User.builder()
        .email(req.email())
        .password(encoder.encode(req.password()))
        .fullName(req.fullName())
        .phone(req.phone())
        .locale(req.locale() != null ? req.locale() : "en")
        .role(Role.ROLE_USER)
        .build();
      
      users.save(user);
      
      var token = jwt.generate(user.getEmail(), Map.of("role", user.getRole().name()));
      
      return ResponseEntity.ok(new AuthResponse(
        token, 
        user.getRole().name(), 
        user.getFullName(),
        user.getEmail()
      ));
    } catch (Exception e) {
      throw new RuntimeException("Registration failed: " + e.getMessage());
    }
  }

  public record LoginRequest(
    @Email String email, 
    @NotBlank String password
  ) {}

  @PostMapping("/login")
  public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest req) {
    try {
      var user = users.findByEmail(req.email())
        .orElseThrow(() -> new RuntimeException("Invalid credentials"));
      
      if (!encoder.matches(req.password(), user.getPassword())) {
        throw new RuntimeException("Invalid credentials");
      }
      
      var token = jwt.generate(user.getEmail(), Map.of("role", user.getRole().name()));
      
      return ResponseEntity.ok(new AuthResponse(
        token, 
        user.getRole().name(), 
        user.getFullName(),
        user.getEmail()
      ));
    } catch (Exception e) {
      throw new RuntimeException("Login failed: " + e.getMessage());
    }
  }
  
  // Test endpoint to verify authentication
  @GetMapping("/me")
  public ResponseEntity<Map<String, String>> getCurrentUser(
    @RequestHeader("Authorization") String authHeader
  ) {
    try {
      String token = authHeader.substring(7);
      var claims = jwt.parse(token).getBody();
      String email = claims.getSubject();
      String role = (String) claims.get("role");
      
      var user = users.findByEmail(email)
        .orElseThrow(() -> new RuntimeException("User not found"));
      
      return ResponseEntity.ok(Map.of(
        "email", email,
        "role", role,
        "fullName", user.getFullName()
      ));
    } catch (Exception e) {
      return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }
  }
}