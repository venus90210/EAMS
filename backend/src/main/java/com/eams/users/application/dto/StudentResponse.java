package com.eams.users.application.dto;

import com.eams.users.domain.Student;

import java.util.UUID;

/**
 * Representación pública de un estudiante.
 */
public record StudentResponse(
        UUID id,
        String firstName,
        String lastName,
        String grade,
        UUID institutionId,
        UUID guardianId
) {
    public static StudentResponse from(Student student) {
        return new StudentResponse(
                student.getId(),
                student.getFirstName(),
                student.getLastName(),
                student.getGrade(),
                student.getInstitutionId(),
                student.getGuardianId()
        );
    }
}
