package com.eams.auth.application;

import com.eams.auth.application.dto.*;
import com.eams.auth.domain.SessionStore;
import com.eams.auth.domain.User;
import com.eams.auth.domain.UserRepository;
import com.eams.shared.exception.DomainException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Casos de uso del módulo Auth (AD-06):
 *   - login         : autenticación con contraseña + MFA opcional
 *   - mfaVerify     : segundo paso para roles con privilegios de escritura
 *   - refreshToken  : renovación de access token con refresh token válido
 *   - logout        : revocación del refresh token en Redis
 *
 * Sigue AD-03: solo interactúa con puertos (UserRepository, SessionStore),
 * nunca con implementaciones concretas.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final int ACCESS_EXPIRES_IN_SECONDS = 900;   // 15 min
    private static final long REFRESH_TTL_SECONDS       = 604_800L; // 7 días

    private final UserRepository    userRepository;
    private final SessionStore      sessionStore;
    private final JwtTokenProvider  jwtTokenProvider;
    private final MfaService        mfaService;
    private final PasswordEncoder   passwordEncoder;

    // ── Login ────────────────────────────────────────────────────────────────

    /**
     * Primer paso de autenticación.
     *
     * GUARDIAN → emite tokens directamente.
     * TEACHER / ADMIN / SUPERADMIN → emite sessionToken MFA pending (AD-06).
     *
     * @throws DomainException INVALID_CREDENTIALS si email/password no coinciden
     */
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> DomainException.unauthorized("INVALID_CREDENTIALS", "Credenciales inválidas"));

        if (!user.matchesPassword(request.password(), passwordEncoder)) {
            throw DomainException.unauthorized("INVALID_CREDENTIALS", "Credenciales inválidas");
        }

        if (user.requiresMfa()) {
            String sessionToken = jwtTokenProvider.generateMfaPendingToken(user);
            log.debug("MFA requerido para usuario {}", user.getId());
            return LoginResponse.withMfa(sessionToken);
        }

        return LoginResponse.withTokens(issueTokenPair(user));
    }

    // ── MFA verify ───────────────────────────────────────────────────────────

    /**
     * Segundo paso: verifica el código TOTP y emite tokens.
     *
     * @throws DomainException MFA_INVALID si el sessionToken no es válido
     *                         o el código TOTP es incorrecto
     */
    public TokenPair mfaVerify(MfaVerifyRequest request) {
        if (!jwtTokenProvider.isMfaPendingToken(request.sessionToken())) {
            throw DomainException.unauthorized("MFA_INVALID", "Session token inválido");
        }

        UUID userId = jwtTokenProvider.extractUserId(request.sessionToken());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> DomainException.unauthorized("MFA_INVALID", "Usuario no encontrado"));

        if (!user.hasMfaConfigured()) {
            throw DomainException.unauthorized("MFA_INVALID", "MFA no configurado para este usuario");
        }

        int code;
        try {
            code = Integer.parseInt(request.code());
        } catch (NumberFormatException e) {
            throw DomainException.unauthorized("MFA_INVALID", "Código MFA inválido");
        }

        if (!mfaService.verifyCode(user.getMfaSecret(), code)) {
            throw DomainException.unauthorized("MFA_INVALID", "Código MFA incorrecto");
        }

        return issueTokenPair(user);
    }

    // ── Refresh token ────────────────────────────────────────────────────────

    /**
     * Renueva el access token usando un refresh token válido en Redis.
     *
     * @throws DomainException TOKEN_REVOKED si el token no existe en Redis
     */
    public TokenPair refreshToken(RefreshRequest request) {
        UUID userId = sessionStore.findUserIdByToken(request.refreshToken())
                .orElseThrow(() -> DomainException.unauthorized("TOKEN_REVOKED",
                        "Refresh token revocado o expirado"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> DomainException.unauthorized("TOKEN_REVOKED",
                        "Usuario no encontrado"));

        // Rotación de refresh token: revoca el anterior, emite uno nuevo
        sessionStore.revoke(request.refreshToken());
        return issueTokenPair(user);
    }

    // ── Logout ───────────────────────────────────────────────────────────────

    /**
     * Revoca el refresh token en Redis (AD-06 — escenario logout).
     * El access token expirará de forma natural en sus 15 minutos.
     */
    public void logout(RefreshRequest request) {
        sessionStore.revoke(request.refreshToken());
        log.debug("Refresh token revocado por logout");
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private TokenPair issueTokenPair(User user) {
        String accessToken  = jwtTokenProvider.generateAccessToken(user);
        String refreshToken = UUID.randomUUID().toString(); // UUID opaco (AD-06)

        sessionStore.save(refreshToken, user.getId(), REFRESH_TTL_SECONDS);

        return new TokenPair(accessToken, refreshToken, ACCESS_EXPIRES_IN_SECONDS);
    }
}
