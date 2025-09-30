package com.example.techdirectory.config; // <-- replace with your package

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Key;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthFilter.class);

    private final UserDetailsService userDetailsService;

    @Value("${security.jwt.base64-secret:}")
    private String base64Secret;

    @Value("${security.jwt.secret:}")
    private String secret;

    private Key key;

    public JwtAuthFilter(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @PostConstruct
    public void init() {
        try {
            byte[] keyBytes = null;
            if (base64Secret != null && !base64Secret.isBlank()) {
                keyBytes = Decoders.BASE64.decode(base64Secret);
            } else if (secret != null && !secret.isBlank()) {
                keyBytes = secret.getBytes(StandardCharsets.UTF_8);
                // optional warning for short secret, but do not throw
                if (keyBytes.length < 32) {
                    logger.warn("security.jwt.secret is shorter than recommended (32+ bytes). Use base64-secret for production.");
                }
            } else {
                // Do not throw — allow app to start; treat key==null as "no JWT configured".
                logger.warn("No JWT secret configured (security.jwt.base64-secret or security.jwt.secret). JWT validation will be disabled.");
                key = null;
                return;
            }
            this.key = Keys.hmacShaKeyFor(keyBytes);
        } catch (Exception ex) {
            // Fail safe: log and leave key=null so server still starts.
            logger.error("Failed to initialize JWT signing key: {}", ex.getMessage(), ex);
            key = null;
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        String method = request.getMethod();
        if ("OPTIONS".equalsIgnoreCase(method)) {
            return true;
        }
        return path.startsWith("/api/public/") || path.startsWith("/api/auth/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // If key is null => JWT support disabled; continue without authentication
        if (key == null) {
            filterChain.doFilter(request, response);
            return;
        }

        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String username = claims.getSubject();
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        } catch (JwtException ex) {
            logger.debug("Invalid JWT for {} : {}", request.getServletPath(), ex.getMessage());
        } catch (Exception ex) {
            logger.warn("JWT processing error for {} : {}", request.getServletPath(), ex.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
