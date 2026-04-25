package com.eams.enrollments.application.dto;

import com.eams.enrollments.domain.Enrollment;
import com.eams.enrollments.domain.EnrollmentStatus;

import java.time.Instant;
import java.util.UUID;

public record EnrollmentResponse(
        UUID id,
        UUID studentId,
        UUID activityId,
        EnrollmentStatus status,
        Instant enrolledAt,
        Instant cancelledAt
) {
    public static EnrollmentResponse from(Enrollment enrollment) {
        return new EnrollmentResponse(
                enrollment.getId(),
                enrollment.getStudentId(),
                enrollment.getActivityId(),
                enrollment.getStatus(),
                enrollment.getEnrolledAt(),
                enrollment.getCancelledAt()
        );
    }
}
