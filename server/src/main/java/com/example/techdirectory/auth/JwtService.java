package com.example.techdirectory.auth;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {
    
    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);
    
    private final Key key;
    private final long expirationMs;

    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expirationMinutes}") long expMin) {
        
        if (secret == null || secret.isBlank()) {
            throw new IllegalArgumentException("JWT secret cannot be null or empty");
        }
        
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        
        if (keyBytes.length < 32) {
            logger.warn("JWT secret is less than 256 bits. Consider using a stronger secret.");
        }
        
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.expirationMs = expMin * 60_000;
    }

    /**
     * Generate a JWT token for the given subject with custom claims
     */
    public String generate(String subject, Map<String, Object> claims) {
        if (subject == null || subject.isBlank()) {
            throw new IllegalArgumentException("Subject cannot be null or empty");
        }
        
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs);
        
        try {
            return Jwts.builder()
                .setSubject(subject)
                .addClaims(claims != null ? claims : Map.of())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
        } catch (Exception e) {
            logger.error("Error generating JWT token for subject: {}", subject, e);
            throw new RuntimeException("Failed to generate JWT token", e);
        }
    }

    /**
     * Parse and validate a JWT token
     */
    public Jws<Claims> parse(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Token cannot be null or empty");
        }
        
        try {
            return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token);
        } catch (ExpiredJwtException e) {
            logger.debug("JWT token is expired: {}", e.getMessage());
            throw new RuntimeException("Token has expired", e);
        } catch (UnsupportedJwtException e) {
            logger.error("Unsupported JWT token: {}", e.getMessage());
            throw new RuntimeException("Unsupported token", e);
        } catch (MalformedJwtException e) {
            logger.error("Malformed JWT token: {}", e.getMessage());
            throw new RuntimeException("Malformed token", e);
        } catch (SignatureException e) {
            logger.error("Invalid JWT signature: {}", e.getMessage());
            throw new RuntimeException("Invalid token signature", e);
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
            throw new RuntimeException("Invalid token", e);
        }
    }

    /**
     * Validate token and extract subject
     */
    public String getSubjectFromToken(String token) {
        try {
            Claims claims = parse(token).getBody();
            return claims.getSubject();
        } catch (Exception e) {
            logger.error("Failed to extract subject from token: {}", e.getMessage());
            throw new RuntimeException("Invalid token", e);
        }
    }

    /**
     * Check if token is expired
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = parse(token).getBody();
            return claims.getExpiration().before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        } catch (Exception e) {
            logger.error("Error checking token expiration: {}", e.getMessage());
            return true;
        }
    }
}