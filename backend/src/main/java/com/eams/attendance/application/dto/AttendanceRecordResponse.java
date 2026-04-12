package com.eams.attendance.application.dto;

import com.eams.attendance.domain.AttendanceRecord;
import java.time.Instant;
import java.util.UUID;

public record AttendanceRecordResponse(
        UUID id,
        UUID sessionId,
        UUID studentId,
        Boolean present,
        String observation,
        Instant recordedAt
) {
    public static AttendanceRecordResponse from(AttendanceRecord record) {
        return new AttendanceRecordResponse(
                record.getId(),
                record.getSessionId(),
                record.getStudentId(),
                record.getPresent(),
                record.getObservation(),
                record.getRecordedAt()
        );
    }
}
