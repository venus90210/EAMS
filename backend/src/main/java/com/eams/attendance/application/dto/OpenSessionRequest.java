package com.eams.attendance.application.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

public record OpenSessionRequest(
        @NotNull UUID activityId,
        @NotNull LocalDate date,
        String topicsCovered
) {}
