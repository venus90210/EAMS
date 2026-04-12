package com.eams.auth.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Cuerpo de POST /auth/login (OpenAPI auth.yaml — LoginRequest).
 */
public record LoginRequest(
        @NotBlank @Email
        String email,

        @NotBlank @Size(min = 8)
        String password
) {}
