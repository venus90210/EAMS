package com.eams.auth.application.dto;

/**
 * Respuesta de POST /auth/login (OpenAPI auth.yaml — LoginResponse).
 *
 * Si mfaRequired=true  → sessionToken presente, tokens=null.
 * Si mfaRequired=false → tokens presente, sessionToken=null.
 *
 * AD-06: MFA obligatorio para TEACHER, ADMIN, SUPERADMIN.
 */
public record LoginResponse(
        boolean mfaRequired,
        String sessionToken,   // JWT temporal 5 min, solo para flujo MFA
        TokenPair tokens       // null si mfaRequired=true
) {

    public static LoginResponse withMfa(String sessionToken) {
        return new LoginResponse(true, sessionToken, null);
    }

    public static LoginResponse withTokens(TokenPair tokens) {
        return new LoginResponse(false, null, tokens);
    }
}
