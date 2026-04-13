package com.eams.enrollments.application.dto;

import com.eams.enrollments.domain.Enrollment;
import com.eams.enrollments.domain.EnrollmentStatus;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO para seguimiento de inscripciones (guardián).
 * Incluye información del estudiante y actividad para el frontend.
 */
public record EnrollmentTrackingResponse(
        UUID id,
        UUID studentId,
        String studentName,
        UUID activityId,
        String activityName,
        EnrollmentStatus status,
        Instant enrolledAt,
        Instant cancelledAt
) {
    public static EnrollmentTrackingResponse from(
            Enrollment enrollment,
            String studentName,
            String activityName) {
        return new EnrollmentTrackingResponse(
                enrollment.getId(),
                enrollment.getStudentId(),
                studentName,
                enrollment.getActivityId(),
                activityName,
                enrollment.getStatus(),
                enrollment.getEnrolledAt(),
                enrollment.getCancelledAt()
        );
    }
}
