package com.eams.attendance.application.dto;

import java.util.UUID;

public record AttendanceStudentDto(
        UUID enrollmentId,
        UUID studentId,
        String studentName,
        Boolean present,
        String observations
) {
}
