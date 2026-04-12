package com.eams.institutions.application.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Cuerpo de PATCH /institutions/{id} — campos opcionales, solo SUPERADMIN (AD-08).
 */
public record UpdateInstitutionRequest(

        @Size(max = 200)
        String name,

        @Size(max = 100)
        @Pattern(regexp = "^[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$",
                 message = "Debe ser un dominio de correo válido (ej: colegio.edu.co)")
        String emailDomain
) {}
