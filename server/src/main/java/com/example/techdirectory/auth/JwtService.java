package com.example.techdirectory.auth;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.security.Key;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {
  private final Key key;
  private final long expirationMs;
  public JwtService(@Value("${jwt.secret}") String secret, @Value("${jwt.expirationMinutes}") long expMin){
    this.key = Keys.hmacShaKeyFor(secret.getBytes());
    this.expirationMs = expMin * 60_000;
  }
  public String generate(String subject, Map<String,Object> claims){
    return Jwts.builder().setSubject(subject).addClaims(claims)
      .setIssuedAt(new Date()).setExpiration(new Date(System.currentTimeMillis()+expirationMs))
      .signWith(key, SignatureAlgorithm.HS256).compact();
  }
  public Jws<Claims> parse(String token){ return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token); }
}
