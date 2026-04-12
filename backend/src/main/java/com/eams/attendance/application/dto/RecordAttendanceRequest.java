package com.eams.attendance.application.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record RecordAttendanceRequest(
        @NotNull UUID sessionId,
        @NotNull UUID studentId,
        @NotNull Boolean present,
        String observation
) {}
