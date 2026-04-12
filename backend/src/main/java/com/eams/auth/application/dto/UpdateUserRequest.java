package com.eams.auth.application.dto;

import jakarta.validation.constraints.Size;

/**
 * Payload para actualizar el perfil de un usuario (PATCH /users/{userId}).
 * Todos los campos son opcionales (actualización parcial).
 * Validaciones contra OWASP A06 (input validation).
 */
public record UpdateUserRequest(
        @Size(max = 100)
        String firstName,

        @Size(max = 100)
        String lastName,

        @Size(max = 20)
        String phone
) {}
