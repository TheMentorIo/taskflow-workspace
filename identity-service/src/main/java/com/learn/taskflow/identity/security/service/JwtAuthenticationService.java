package com.learn.taskflow.identity.security.service;
import java.util.Date;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
@Service
public class JwtAuthenticationService {

    private final SecretKey secretKey;

    @Value("${jwt.issuer:identity-service}")
    private  final String issuer = "identity-service";

    @Value("${jwt.audience:taskflow-api}")
    private final String audience = "taskflow-api";

    public JwtAuthenticationService(@Value("${jwt.secret-key}") String secretKey) {
        this.secretKey = Keys.hmacShaKeyFor(
            Decoders.BASE64.decode(secretKey)
        );
    }

    public String generateAccessToken(UUID userId, UUID sessionId, long expiresAt) {
        return Jwts.builder()
                .subject(userId.toString())
                .issuer(issuer)
                .audience().add(audience).and()
                .claim("sessionId", sessionId.toString())
                .claim("type", "access")
                .issuedAt(new Date())
                .expiration(new Date(expiresAt))
                .signWith(secretKey)
                .compact();
    }

    public String generateRefreshToken(UUID userId, UUID sessionId, long expiresAt) {
        return Jwts.builder()
                .subject(userId.toString())
                .issuer(issuer)
                .audience().add(audience).and()
                .claim("sessionId", sessionId.toString())
                .claim("type", "refresh")
                .issuedAt(new Date())
                .expiration(new Date(expiresAt))
                .signWith(secretKey)
                .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .requireIssuer(issuer)
                .requireAudience(audience)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public UUID getSessionId(Claims claims) {
        return UUID.fromString(claims.get("sessionId", String.class));
    }

    public boolean isAccessToken(Claims claims) {
        return "access".equals(claims.get("type", String.class));
    }

    public boolean isRefreshToken(Claims claims) {
        return "refresh".equals(claims.get("type", String.class));
    }

    public boolean verifyToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);

            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}