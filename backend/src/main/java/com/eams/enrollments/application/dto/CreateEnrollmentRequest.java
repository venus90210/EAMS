package com.eams.enrollments.application.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateEnrollmentRequest(
        @NotNull UUID studentId,
        @NotNull UUID activityId
) {}
