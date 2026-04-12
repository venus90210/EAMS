package com.eams.auth.application.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Cuerpo de POST /auth/refresh y POST /auth/logout (OpenAPI auth.yaml — RefreshRequest).
 */
public record RefreshRequest(
        @NotBlank
        String refreshToken
) {}
