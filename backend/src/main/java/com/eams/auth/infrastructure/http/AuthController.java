package com.eams.auth.infrastructure.http;

import com.eams.auth.application.AuthService;
import com.eams.auth.application.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Adaptador HTTP del módulo Auth (AD-03 — adaptador de entrada).
 *
 * Implementa los endpoints definidos en OpenAPI auth.yaml:
 *   POST /auth/login
 *   POST /auth/mfa/verify
 *   POST /auth/refresh
 *   POST /auth/logout
 *
 * La validación de JWT/RBAC la hace el API Gateway (AD-04).
 * Este controlador solo expone los endpoints de autenticación (sin seguridad propia).
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * POST /auth/login
     * Inicia sesión. Para roles con MFA retorna mfaRequired=true + sessionToken.
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    /**
     * POST /auth/mfa/verify
     * Segundo paso del flujo MFA: verifica TOTP y emite tokens definitivos.
     */
    @PostMapping("/mfa/verify")
    public ResponseEntity<TokenPair> verifyMfa(@Valid @RequestBody MfaVerifyRequest request) {
        return ResponseEntity.ok(authService.mfaVerify(request));
    }

    /**
     * POST /auth/refresh
     * Renueva el access token usando el refresh token almacenado en Redis.
     */
    @PostMapping("/refresh")
    public ResponseEntity<TokenPair> refresh(@Valid @RequestBody RefreshRequest request) {
        return ResponseEntity.ok(authService.refreshToken(request));
    }

    /**
     * POST /auth/logout
     * Revoca el refresh token en Redis. El access token expira en sus 15 min.
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshRequest request) {
        authService.logout(request);
        return ResponseEntity.ok().build();
    }
}
