package com.example.demo;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService{
    private final SecretKey key;
    private final long expirationMs;
    public JwtService(@Value("${jwt.secret}") String secret,@Value("${jwt.expiration-ms}") long expirationMs){
            this.key=Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
            this.expirationMs=expirationMs;

    }
    public String createToken(String username,String role){
        Date now=new Date();
        Date expiry= new Date(now.getTime()+expirationMs);
        return Jwts.builder()
                .subject(username)
                .claim("role",role)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();

    }
    public String extractUsername(String token){
        return Jwts.parser()
        .verifyWith(key)
        .build()
        .parseSignedClaims(token)
        .getPayload()
        .getSubject();
    }
    
    public String extractRole(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("role", String.class);
    }
}