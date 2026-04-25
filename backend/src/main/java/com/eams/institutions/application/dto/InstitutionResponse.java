package com.eams.institutions.application.dto;

import com.eams.institutions.domain.Institution;

import java.time.Instant;
import java.util.UUID;

/**
 * Proyección de Institution para respuestas HTTP.
 */
public record InstitutionResponse(
        UUID    id,
        String  name,
        String  emailDomain,
        Instant createdAt
) {
    public static InstitutionResponse from(Institution i) {
        return new InstitutionResponse(i.getId(), i.getName(), i.getEmailDomain(), i.getCreatedAt());
    }
}
