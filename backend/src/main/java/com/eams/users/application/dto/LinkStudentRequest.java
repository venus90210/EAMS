package com.eams.users.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * Payload para vincular un estudiante a un acudiente (POST /users/students/link).
 * Validaciones contra OWASP A06 (input validation).
 */
public record LinkStudentRequest(
        @NotNull
        UUID guardianId,

        @NotBlank @Size(max = 100)
        String studentFirstName,

        @NotBlank @Size(max = 100)
        String studentLastName,

        @Size(max = 50)
        String grade
) {}
