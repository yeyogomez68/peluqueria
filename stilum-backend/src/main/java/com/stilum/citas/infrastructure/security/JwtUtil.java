package com.stilum.citas.infrastructure.security;

import com.stilum.citas.domain.user.UserRol;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

/**
 * Genera y valida JWT con claims: sub (email), tenantId, rol.
 * SK-B-11: Configuración via application.yml, no hardcodeada.
 */
@Component
public class JwtUtil {

    private static final String CLAIM_TENANT_ID = "tenantId";
    private static final String CLAIM_ROL = "rol";
    private static final String CLAIM_USER_ID = "userId";

    private final SecretKey secretKey;
    private final long expirationMs;

    public JwtUtil(
            @Value("${stilum.jwt.secret}") String secret,
            @Value("${stilum.jwt.expiration-ms}") long expirationMs) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    public String generarToken(UUID userId, String email, UUID tenantId, UserRol rol) {
        return Jwts.builder()
                .subject(email)
                .claim(CLAIM_USER_ID, userId.toString())
                .claim(CLAIM_TENANT_ID, tenantId != null ? tenantId.toString() : null)
                .claim(CLAIM_ROL, rol.name())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(secretKey)
                .compact();
    }

    public Claims parsearClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean esValido(String token) {
        try {
            parsearClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public String extraerEmail(String token) {
        return parsearClaims(token).getSubject();
    }

    public UUID extraerTenantId(String token) {
        String tenantId = parsearClaims(token).get(CLAIM_TENANT_ID, String.class);
        return tenantId != null ? UUID.fromString(tenantId) : null;
    }

    public UUID extraerUserId(String token) {
        return UUID.fromString(parsearClaims(token).get(CLAIM_USER_ID, String.class));
    }

    public UserRol extraerRol(String token) {
        return UserRol.valueOf(parsearClaims(token).get(CLAIM_ROL, String.class));
    }
}
