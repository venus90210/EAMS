package com.eams.auth.application.dto;

import com.eams.auth.domain.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * Payload para registrar un nuevo usuario (POST /users).
 * Validaciones contra OWASP A06 (input validation).
 */
public record RegisterUserRequest(
        @NotBlank @Email
        String email,

        @NotBlank @Size(min = 8, max = 128)
        String password,

        @NotNull
        UserRole role,

        UUID institutionId,

        @Size(max = 100)
        String firstName,

        @Size(max = 100)
        String lastName,

        @Size(max = 20)
        String phone
) {}
