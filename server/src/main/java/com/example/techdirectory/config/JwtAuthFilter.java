package com.example.techdirectory.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Key;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthFilter.class);

    private final UserDetailsService userDetailsService;

    @Value("${jwt.secret}")
    private String secret;

    private Key key;

    public JwtAuthFilter(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @PostConstruct
    public void init() {
        try {
            if (secret == null || secret.isBlank()) {
                throw new IllegalStateException("JWT secret is not configured");
            }
            
            byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
            
            if (keyBytes.length < 32) {
                logger.warn("JWT secret is shorter than recommended (32+ bytes). Consider using a stronger secret.");
            }
            
            this.key = Keys.hmacShaKeyFor(keyBytes);
            logger.info("JWT signing key initialized successfully");
        } catch (Exception ex) {
            logger.error("Failed to initialize JWT signing key: {}", ex.getMessage(), ex);
            throw new IllegalStateException("JWT initialization failed", ex);
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        String method = request.getMethod();
        
        if ("OPTIONS".equalsIgnoreCase(method)) {
            return true;
        }
        
        return path.startsWith("/api/public/") 
            || path.startsWith("/api/auth/")
            || path.equals("/api/regions")
            || path.startsWith("/api/payments/webhook/")
            || path.equals("/api/payments/success")
            || path.equals("/api/payments/cancel")
            || path.startsWith("/actuator/");
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        
        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7).trim();
        
        if (token.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String username = claims.getSubject();
            
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                try {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    
                    UsernamePasswordAuthenticationToken authToken = 
                        new UsernamePasswordAuthenticationToken(
                            userDetails, 
                            null, 
                            userDetails.getAuthorities()
                        );
                    
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    
                    logger.debug("Successfully authenticated user: {}", username);
                } catch (Exception e) {
                    logger.error("Failed to load user details for: {}", username, e);
                }
            }
        } catch (ExpiredJwtException ex) {
            logger.debug("Expired JWT token for {}: {}", request.getServletPath(), ex.getMessage());
            response.setHeader("X-Token-Expired", "true");
        } catch (JwtException ex) {
            logger.debug("Invalid JWT token for {}: {}", request.getServletPath(), ex.getMessage());
        } catch (Exception ex) {
            logger.warn("JWT processing error for {}: {}", request.getServletPath(), ex.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}