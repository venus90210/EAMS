package com.eams.auth.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Cuerpo de POST /auth/mfa/verify (OpenAPI auth.yaml — MfaVerifyRequest).
 */
public record MfaVerifyRequest(
        @NotBlank
        String sessionToken,

        @NotBlank @Pattern(regexp = "\\d{6}")
        String code
) {}
