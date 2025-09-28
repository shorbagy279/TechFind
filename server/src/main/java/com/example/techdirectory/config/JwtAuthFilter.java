package com.example.techdirectory.config;
import com.example.techdirectory.auth.JwtService;
import com.example.techdirectory.user.UserService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {
  private final JwtService jwtService;
  private final UserService userService;
  public JwtAuthFilter(JwtService jwtService, UserService userService){ this.jwtService=jwtService; this.userService=userService; }
  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
    String authHeader = request.getHeader("Authorization");
    if(authHeader!=null && authHeader.startsWith("Bearer ")){
      String token = authHeader.substring(7);
      try{
        Claims claims = jwtService.parse(token).getBody();
        var userDetails = userService.loadUserByUsername(claims.getSubject());
        var authToken = new UsernamePasswordAuthenticationToken(userDetails,null,userDetails.getAuthorities());
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);
      } catch(Exception ignored){}
    }
    chain.doFilter(request, response);
  }
}
