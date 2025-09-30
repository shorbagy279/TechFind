package com.example.techdirectory.auth;

import com.example.techdirectory.user.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController 
@RequestMapping("/api/auth")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"}, allowCredentials = "true")
public class AuthController {
  
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
  
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
  
    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) { 
        this.userRepository = userRepository; 
        this.passwordEncoder = passwordEncoder; 
        this.jwtService = jwtService; 
    }

    public record RegisterRequest(
        @Email(message = "Invalid email format") 
        @NotBlank(message = "Email is required") 
        String email,
        
        @NotBlank(message = "Password is required") 
        @Size(min = 8, message = "Password must be at least 8 characters") 
        String password,
        
        @NotBlank(message = "Full name is required") 
        @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters") 
        String fullName,
        
        @Size(max = 20, message = "Phone number too long") 
        String phone,
        
        String locale
    ) {}
  
    public record AuthResponse(
        String token, 
        String role, 
        String fullName, 
        String email
    ) {}
    
    public record ErrorResponse(
        String error,
        String message
    ) {}

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest req) {
        try {
            // Check if email already exists
            if (userRepository.existsByEmail(req.email().toLowerCase())) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ErrorResponse("EMAIL_EXISTS", "Email already registered"));
            }
            
            // Validate locale
            String locale = (req.locale() != null && req.locale().matches("^(en|ar)$")) 
                ? req.locale() 
                : "en";
            
            // Create new user
            User user = User.builder()
                .email(req.email().toLowerCase().trim())
                .password(passwordEncoder.encode(req.password()))
                .fullName(req.fullName().trim())
                .phone(req.phone() != null ? req.phone().trim() : null)
                .locale(locale)
                .role(Role.ROLE_USER)
                .build();
            
            userRepository.save(user);
            
            // Generate JWT token
            String token = jwtService.generate(
                user.getEmail(), 
                Map.of(
                    "role", user.getRole().name(),
                    "userId", user.getId().toString()
                )
            );
            
            logger.info("User registered successfully: {}", user.getEmail());
            
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(new AuthResponse(
                    token, 
                    user.getRole().name(), 
                    user.getFullName(),
                    user.getEmail()
                ));
                
        } catch (Exception e) {
            logger.error("Registration failed for email: {}", req.email(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("REGISTRATION_FAILED", "Registration failed. Please try again."));
        }
    }

    public record LoginRequest(
        @Email(message = "Invalid email format") 
        @NotBlank(message = "Email is required") 
        String email,
        
        @NotBlank(message = "Password is required") 
        String password
    ) {}

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req) {
        try {
            // Find user by email
            User user = userRepository.findByEmail(req.email().toLowerCase())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));
            
            // Verify password
            if (!passwordEncoder.matches(req.password(), user.getPassword())) {
                logger.warn("Failed login attempt for email: {}", req.email());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("INVALID_CREDENTIALS", "Invalid email or password"));
            }
            
            // Generate JWT token
            String token = jwtService.generate(
                user.getEmail(), 
                Map.of(
                    "role", user.getRole().name(),
                    "userId", user.getId().toString()
                )
            );
            
            logger.info("User logged in successfully: {}", user.getEmail());
            
            return ResponseEntity.ok(new AuthResponse(
                token, 
                user.getRole().name(), 
                user.getFullName(),
                user.getEmail()
            ));
            
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse("INVALID_CREDENTIALS", "Invalid email or password"));
        } catch (Exception e) {
            logger.error("Login failed for email: {}", req.email(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("LOGIN_FAILED", "Login failed. Please try again."));
        }
    }
  
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("UNAUTHORIZED", "Not authenticated"));
            }
            
            String email = authentication.getName();
            
            User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            return ResponseEntity.ok(Map.of(
                "id", user.getId(),
                "email", user.getEmail(),
                "role", user.getRole().name(),
                "fullName", user.getFullName(),
                "phone", user.getPhone() != null ? user.getPhone() : "",
                "locale", user.getLocale()
            ));
            
        } catch (Exception e) {
            logger.error("Failed to get current user", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("ERROR", "Failed to retrieve user information"));
        }
    }
    
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        // For stateless JWT, logout is handled client-side by removing the token
        // You can implement token blacklisting here if needed
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }
}