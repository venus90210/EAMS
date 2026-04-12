package com.eams.auth.application;

import com.eams.auth.application.dto.*;
import com.eams.auth.domain.SessionStore;
import com.eams.auth.domain.User;
import com.eams.auth.domain.UserRepository;
import com.eams.auth.domain.UserRole;
import com.eams.shared.exception.DomainException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private static final String SECRET =
            "test-secret-key-minimum-256-bits-long-for-hs256-algorithm";

    @Mock private UserRepository    userRepository;
    @Mock private SessionStore      sessionStore;
    @Mock private MfaService        mfaService;

    private JwtTokenProvider jwtTokenProvider;
    private PasswordEncoder  passwordEncoder;
    private AuthService      authService;

    private User guardian;
    private User adminWithMfa;

    @BeforeEach
    void setUp() {
        passwordEncoder  = new BCryptPasswordEncoder();
        jwtTokenProvider = new JwtTokenProvider(SECRET, 15);

        authService = new AuthService(
                userRepository, sessionStore,
                jwtTokenProvider, mfaService, passwordEncoder);

        guardian = User.create("guardian@test.com", "Password1!",
                UserRole.GUARDIAN, UUID.randomUUID(), passwordEncoder);

        adminWithMfa = User.create("admin@test.com", "Password1!",
                UserRole.ADMIN, UUID.randomUUID(), passwordEncoder);
        adminWithMfa.configureMfa("JBSWY3DPEHPK3PXP"); // secreto TOTP de prueba
    }

    // ── login: GUARDIAN sin MFA ───────────────────────────────────────────────

    @Test
    void login_guardian_returnsTokensWithoutMfa() {
        when(userRepository.findByEmail("guardian@test.com"))
                .thenReturn(Optional.of(guardian));

        LoginResponse response = authService.login(new LoginRequest("guardian@test.com", "Password1!"));

        assertThat(response.mfaRequired()).isFalse();
        assertThat(response.tokens()).isNotNull();
        assertThat(response.sessionToken()).isNull();
        verify(sessionStore).save(anyString(), eq(guardian.getId()), eq(604_800L));
    }

    @Test
    void login_returnsAccessTokenWithCorrectRole() {
        when(userRepository.findByEmail("guardian@test.com"))
                .thenReturn(Optional.of(guardian));

        LoginResponse response = authService.login(new LoginRequest("guardian@test.com", "Password1!"));

        String role = jwtTokenProvider.extractRole(response.tokens().accessToken());
        assertThat(role).isEqualTo("GUARDIAN");
    }

    // ── login: ADMIN con MFA ──────────────────────────────────────────────────

    @Test
    void login_admin_returnsMfaRequired() {
        when(userRepository.findByEmail("admin@test.com"))
                .thenReturn(Optional.of(adminWithMfa));

        LoginResponse response = authService.login(new LoginRequest("admin@test.com", "Password1!"));

        assertThat(response.mfaRequired()).isTrue();
        assertThat(response.sessionToken()).isNotBlank();
        assertThat(response.tokens()).isNull();
        verifyNoInteractions(sessionStore);
    }

    // ── login: credenciales inválidas ─────────────────────────────────────────

    @Test
    void login_unknownEmail_throwsInvalidCredentials() {
        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(new LoginRequest("nope@test.com", "any")))
                .isInstanceOf(DomainException.class)
                .hasFieldOrPropertyWithValue("errorCode", "INVALID_CREDENTIALS");
    }

    @Test
    void login_wrongPassword_throwsInvalidCredentials() {
        when(userRepository.findByEmail("guardian@test.com"))
                .thenReturn(Optional.of(guardian));

        assertThatThrownBy(() -> authService.login(new LoginRequest("guardian@test.com", "WrongPass!")))
                .isInstanceOf(DomainException.class)
                .hasFieldOrPropertyWithValue("errorCode", "INVALID_CREDENTIALS");
    }

    // ── mfaVerify ─────────────────────────────────────────────────────────────

    @Test
    void mfaVerify_validCode_returnsTokenPair() {
        String sessionToken = jwtTokenProvider.generateMfaPendingToken(adminWithMfa);
        when(userRepository.findById(adminWithMfa.getId()))
                .thenReturn(Optional.of(adminWithMfa));
        when(mfaService.verifyCode(eq("JBSWY3DPEHPK3PXP"), anyInt())).thenReturn(true);

        TokenPair result = authService.mfaVerify(new MfaVerifyRequest(sessionToken, "123456"));

        assertThat(result.accessToken()).isNotBlank();
        assertThat(result.refreshToken()).isNotBlank();
        assertThat(result.expiresIn()).isEqualTo(900);
        verify(sessionStore).save(anyString(), eq(adminWithMfa.getId()), eq(604_800L));
    }

    @Test
    void mfaVerify_invalidCode_throwsMfaInvalid() {
        String sessionToken = jwtTokenProvider.generateMfaPendingToken(adminWithMfa);
        when(userRepository.findById(adminWithMfa.getId()))
                .thenReturn(Optional.of(adminWithMfa));
        when(mfaService.verifyCode(anyString(), anyInt())).thenReturn(false);

        assertThatThrownBy(() ->
                authService.mfaVerify(new MfaVerifyRequest(sessionToken, "000000")))
                .isInstanceOf(DomainException.class)
                .hasFieldOrPropertyWithValue("errorCode", "MFA_INVALID");
    }

    @Test
    void mfaVerify_invalidSessionToken_throwsMfaInvalid() {
        // Usa un access token en lugar de un sessionToken
        String accessToken = jwtTokenProvider.generateAccessToken(adminWithMfa);

        assertThatThrownBy(() ->
                authService.mfaVerify(new MfaVerifyRequest(accessToken, "123456")))
                .isInstanceOf(DomainException.class)
                .hasFieldOrPropertyWithValue("errorCode", "MFA_INVALID");
    }

    @Test
    void mfaVerify_nonNumericCode_throwsMfaInvalid() {
        String sessionToken = jwtTokenProvider.generateMfaPendingToken(adminWithMfa);
        when(userRepository.findById(adminWithMfa.getId()))
                .thenReturn(Optional.of(adminWithMfa));

        assertThatThrownBy(() ->
                authService.mfaVerify(new MfaVerifyRequest(sessionToken, "ABCDEF")))
                .isInstanceOf(DomainException.class)
                .hasFieldOrPropertyWithValue("errorCode", "MFA_INVALID");
    }

    // ── refreshToken ──────────────────────────────────────────────────────────

    @Test
    void refreshToken_valid_returnsNewTokenPair() {
        String refreshToken = UUID.randomUUID().toString();
        when(sessionStore.findUserIdByToken(refreshToken))
                .thenReturn(Optional.of(guardian.getId()));
        when(userRepository.findById(guardian.getId()))
                .thenReturn(Optional.of(guardian));

        TokenPair result = authService.refreshToken(new RefreshRequest(refreshToken));

        assertThat(result.accessToken()).isNotBlank();
        verify(sessionStore).revoke(refreshToken);                          // rotación
        verify(sessionStore).save(anyString(), eq(guardian.getId()), anyLong()); // nuevo token
    }

    @Test
    void refreshToken_revoked_throwsTokenRevoked() {
        String refreshToken = UUID.randomUUID().toString();
        when(sessionStore.findUserIdByToken(refreshToken)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.refreshToken(new RefreshRequest(refreshToken)))
                .isInstanceOf(DomainException.class)
                .hasFieldOrPropertyWithValue("errorCode", "TOKEN_REVOKED");
    }

    // ── logout ────────────────────────────────────────────────────────────────

    @Test
    void logout_revokesRefreshToken() {
        String refreshToken = UUID.randomUUID().toString();

        authService.logout(new RefreshRequest(refreshToken));

        verify(sessionStore).revoke(refreshToken);
    }
}
