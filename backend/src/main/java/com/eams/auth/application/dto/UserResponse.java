package com.eams.auth.application.dto;

import com.eams.auth.domain.User;
import com.eams.auth.domain.UserRole;

import java.time.Instant;
import java.util.UUID;

/**
 * Representación pública de un usuario (sin datos sensibles).
 */
public record UserResponse(
        UUID id,
        String email,
        UserRole role,
        UUID institutionId,
        String firstName,
        String lastName,
        Instant createdAt
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getRole(),
                user.getInstitutionId(),
                user.getFirstName(),
                user.getLastName(),
                user.getCreatedAt()
        );
    }
}
