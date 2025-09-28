package com.example.techdirectory.auth;
import com.example.techdirectory.user.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController @RequestMapping("/api/auth")
public class AuthController {
  private final UserRepository users; private final PasswordEncoder encoder; private final JwtService jwt;
  public AuthController(UserRepository u, PasswordEncoder e, JwtService j){ this.users=u; this.encoder=e; this.jwt=j; }

  public record RegisterRequest(@Email String email, @NotBlank String password, @NotBlank String fullName, String phone, String locale){}
  public record AuthResponse(String token, String role, String fullName){}

  @PostMapping("/register")
  public AuthResponse register(@RequestBody RegisterRequest req){
    if(users.existsByEmail(req.email())) throw new RuntimeException("Email exists");
    var user = User.builder().email(req.email()).password(encoder.encode(req.password()))
      .fullName(req.fullName()).phone(req.phone()).locale(req.locale()!=null?req.locale():"en").role(Role.ROLE_USER).build();
    users.save(user);
    var token = jwt.generate(user.getEmail(), Map.of("role", user.getRole().name()));
    return new AuthResponse(token, user.getRole().name(), user.getFullName());
  }

  public record LoginRequest(@Email String email, @NotBlank String password){}

  @PostMapping("/login")
  public AuthResponse login(@RequestBody LoginRequest req){
    var user = users.findByEmail(req.email()).orElseThrow(() -> new RuntimeException("Invalid credentials"));
    if(!encoder.matches(req.password(), user.getPassword())) throw new RuntimeException("Invalid credentials");
    var token = jwt.generate(user.getEmail(), Map.of("role", user.getRole().name()));
    return new AuthResponse(token, user.getRole().name(), user.getFullName());
  }
}
