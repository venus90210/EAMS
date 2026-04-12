package com.eams.auth.application;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import com.warrenstrange.googleauth.GoogleAuthenticatorQRGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Servicio TOTP para MFA obligatorio (AD-06, RNF04).
 * Usa googleauth que implementa RFC 6238 (TOTP).
 *
 * Roles que requieren MFA: TEACHER, ADMIN, SUPERADMIN.
 */
@Slf4j
@Service
public class MfaService {

    private static final String ISSUER = "EAMS";
    private static final int WINDOW = 1; // ±1 paso de 30s = ventana de 90s

    private final GoogleAuthenticator gauth = new GoogleAuthenticator();

    // ── Configuración inicial ────────────────────────────────────────────────

    /**
     * Genera un nuevo secreto TOTP para un usuario.
     * Debe persistirse en user.mfaSecret (cifrado en la BD).
     */
    public String generateSecret() {
        GoogleAuthenticatorKey credentials = gauth.createCredentials();
        return credentials.getKey();
    }

    /**
     * Genera la URL otpauth:// para que el usuario escanee el QR
     * con Google Authenticator, Authy u otro cliente TOTP.
     */
    public String getOtpAuthUrl(String secret, String email) {
        return GoogleAuthenticatorQRGenerator.getOtpAuthTotpURL(ISSUER, email, buildKey(secret));
    }

    // ── Verificación ─────────────────────────────────────────────────────────

    /**
     * Verifica un código TOTP de 6 dígitos contra el secreto del usuario.
     * Admite una ventana de ±1 intervalo (30 s) para compensar desfases de reloj.
     *
     * @param secret  secreto TOTP almacenado en el usuario
     * @param code    código de 6 dígitos ingresado por el usuario
     * @return true si el código es válido en la ventana actual
     */
    public boolean verifyCode(String secret, int code) {
        try {
            return gauth.authorize(secret, code, WINDOW);
        } catch (Exception e) {
            log.debug("Error verificando código MFA: {}", e.getMessage());
            return false;
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private GoogleAuthenticatorKey buildKey(String secret) {
        return new GoogleAuthenticatorKey.Builder(secret).build();
    }
}
