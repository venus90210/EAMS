package com.eams.auth.application.dto;

/**
 * Par de tokens emitidos tras autenticación exitosa (OpenAPI auth.yaml — TokenPair).
 * accessToken: JWT válido 15 min.
 * refreshToken: UUID opaco almacenado en Redis, válido 7 días (AD-06).
 */
public record TokenPair(
        String accessToken,
        String refreshToken,
        int expiresIn          // segundos (900 para 15 min)
) {}
