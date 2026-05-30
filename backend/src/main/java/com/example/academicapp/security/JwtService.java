package com.example.academicapp.security;

import java.security.Key;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    private final String secretKey;
    private final long expirationTime;

    public JwtService(
            @Value("${academicapp.jwt.secret}") String secretKey,
            @Value("${academicapp.jwt.expiration-ms:86400000}") long expirationTime) {
        this.secretKey = secretKey;
        this.expirationTime = expirationTime;
    }

    public String generateToken(String correoEducativo) {
        return Jwts.builder()
                .setSubject(correoEducativo)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(getSignInKey())
                .compact();
    }

    public String extractCorreoEducativo(String token) {
        return extractAllClaims(token).getSubject();
    }

    public boolean isTokenValid(String token, String correoEducativo) {
        final String correoExtraido = extractCorreoEducativo(token);
        return correoExtraido.equals(correoEducativo) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSignInKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }
}