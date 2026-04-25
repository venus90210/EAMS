package com.eams.auth.application;

import com.eams.auth.domain.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

/**
 * Genera y valida Access Tokens JWT (AD-06).
 *
 * Payload del token:
 *   sub          : userId (UUID)
 *   role         : UserRole
 *   institutionId: UUID | null (SUPERADMIN)
 *   iat / exp    : tiempos de emisión y expiración
 */
@Slf4j
@Component
public class JwtTokenProvider {

    private final SecretKey signingKey;
    private final long accessExpirationMinutes;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-expiration-minutes:15}") long accessExpirationMinutes) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessExpirationMinutes = accessExpirationMinutes;
    }

    // ── Generación ──────────────────────────────────────────────────────────

    public String generateAccessToken(User user) {
        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(accessExpirationMinutes * 60);

        JwtBuilder builder = Jwts.builder()
                .subject(user.getId().toString())
                .claim("role", user.getRole().name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(signingKey);

        if (user.getInstitutionId() != null) {
            builder.claim("institutionId", user.getInstitutionId().toString());
        }

        return builder.compact();
    }

    // ── Token MFA pending ────────────────────────────────────────────────────

    private static final long MFA_PENDING_MINUTES = 5;

    /**
     * Genera un token temporal (5 min) para completar el paso MFA.
     * Contiene el claim {@code mfaPending=true} para distinguirlo
     * de un access token regular (AD-06).
     */
    public String generateMfaPendingToken(User user) {
        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(MFA_PENDING_MINUTES * 60);

        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("mfaPending", true)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(signingKey)
                .compact();
    }

    /**
     * Valida que el token sea un sessionToken MFA pendiente legítimo.
     * Rechaza tokens sin el claim {@code mfaPending=true}.
     */
    public boolean isMfaPendingToken(String token) {
        try {
            Claims claims = parseClaims(token);
            return Boolean.TRUE.equals(claims.get("mfaPending", Boolean.class));
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // ── Validación ──────────────────────────────────────────────────────────

    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("Token JWT inválido: {}", e.getMessage());
            return false;
        }
    }

    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public UUID extractUserId(String token) {
        return UUID.fromString(parseClaims(token).getSubject());
    }

    public String extractRole(String token) {
        return parseClaims(token).get("role", String.class);
    }

    public UUID extractInstitutionId(String token) {
        String id = parseClaims(token).get("institutionId", String.class);
        return id != null ? UUID.fromString(id) : null;
    }

    public boolean isTokenExpired(String token) {
        try {
            return parseClaims(token).getExpiration().before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        }
    }
}
