package com.eams.auth.application;

import com.eams.auth.domain.User;
import com.eams.auth.domain.UserRole;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@Tag("unit")
class JwtTokenProviderTest {

    private static final String SECRET =
            "test-secret-key-minimum-256-bits-long-for-hs256-algorithm";
    private static final long EXPIRATION_MINUTES = 15;

    private JwtTokenProvider provider;
    private User guardian;
    private User admin;

    @BeforeEach
    void setUp() {
        provider = new JwtTokenProvider(SECRET, EXPIRATION_MINUTES);
        var encoder = new BCryptPasswordEncoder();

        guardian = User.create("guardian@test.com", "Password1!", UserRole.GUARDIAN,
                UUID.randomUUID(), encoder);
        admin = User.create("admin@test.com", "Password1!", UserRole.ADMIN,
                UUID.randomUUID(), encoder);
    }

    // ── generateAccessToken ──────────────────────────────────────────────────

    @Test
    void generateAccessToken_returnsValidToken() {
        String token = provider.generateAccessToken(guardian);
        assertThat(token).isNotBlank();
        assertThat(provider.validateToken(token)).isTrue();
    }

    @Test
    void generateAccessToken_containsRoleClaim() {
        String token = provider.generateAccessToken(guardian);
        assertThat(provider.extractRole(token)).isEqualTo("GUARDIAN");
    }

    @Test
    void generateAccessToken_containsInstitutionId_whenPresent() {
        String token = provider.generateAccessToken(guardian);
        UUID institutionId = provider.extractInstitutionId(token);
        assertThat(institutionId).isNotNull();
    }

    @Test
    void generateAccessToken_institutionIdNull_forSuperAdmin() {
        User superAdmin = User.create("sa@test.com", "Password1!", UserRole.SUPERADMIN,
                null, new BCryptPasswordEncoder());
        String token = provider.generateAccessToken(superAdmin);
        assertThat(provider.extractInstitutionId(token)).isNull();
    }

    @Test
    void extractUserId_returnsCorrectId() {
        String token = provider.generateAccessToken(guardian);
        UUID userId = provider.extractUserId(token);
        assertThat(userId).isEqualTo(guardian.getId());
    }

    // ── validateToken ────────────────────────────────────────────────────────

    @Test
    void validateToken_returnsFalse_forInvalidToken() {
        assertThat(provider.validateToken("invalid.token.here")).isFalse();
    }

    @Test
    void validateToken_returnsFalse_forTamperedToken() {
        String token = provider.generateAccessToken(guardian);
        String tampered = token.substring(0, token.length() - 5) + "XXXXX";
        assertThat(provider.validateToken(tampered)).isFalse();
    }

    @Test
    void validateToken_returnsFalse_forBlankToken() {
        assertThat(provider.validateToken("")).isFalse();
    }

    // ── MFA pending token ────────────────────────────────────────────────────

    @Test
    void generateMfaPendingToken_isValidAndNotAccessToken() {
        String sessionToken = provider.generateMfaPendingToken(admin);
        assertThat(sessionToken).isNotBlank();
        assertThat(provider.isMfaPendingToken(sessionToken)).isTrue();
    }

    @Test
    void isMfaPendingToken_returnsFalse_forAccessToken() {
        String accessToken = provider.generateAccessToken(admin);
        assertThat(provider.isMfaPendingToken(accessToken)).isFalse();
    }

    @Test
    void isMfaPendingToken_returnsFalse_forInvalidToken() {
        assertThat(provider.isMfaPendingToken("garbage")).isFalse();
    }

    @Test
    void extractUserId_worksForMfaPendingToken() {
        String sessionToken = provider.generateMfaPendingToken(admin);
        assertThat(provider.extractUserId(sessionToken)).isEqualTo(admin.getId());
    }

    // ── isTokenExpired ────────────────────────────────────────────────────────

    @Test
    void isTokenExpired_returnsFalse_forFreshToken() {
        String token = provider.generateAccessToken(guardian);
        assertThat(provider.isTokenExpired(token)).isFalse();
    }

    @Test
    void isTokenExpired_returnsTrue_forExpiredToken() {
        // TTL de 0 minutos → expira inmediatamente
        JwtTokenProvider shortLived = new JwtTokenProvider(SECRET, 0);
        String token = shortLived.generateAccessToken(guardian);
        assertThat(shortLived.isTokenExpired(token)).isTrue();
    }

    // ── parseClaims ──────────────────────────────────────────────────────────

    @Test
    void parseClaims_returnsExpectedPayload() {
        String token = provider.generateAccessToken(guardian);
        Claims claims = provider.parseClaims(token);

        assertThat(claims.getSubject()).isEqualTo(guardian.getId().toString());
        assertThat(claims.get("role", String.class)).isEqualTo("GUARDIAN");
    }
}
