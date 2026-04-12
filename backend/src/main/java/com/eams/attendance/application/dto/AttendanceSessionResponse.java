package com.eams.attendance.application.dto;

import com.eams.attendance.domain.AttendanceSession;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record AttendanceSessionResponse(
        UUID id,
        UUID activityId,
        LocalDate date,
        String topicsCovered,
        Instant recordedAt,
        Boolean isEditable
) {
    public static AttendanceSessionResponse from(AttendanceSession session) {
        return new AttendanceSessionResponse(
                session.getId(),
                session.getActivityId(),
                session.getDate(),
                session.getTopicsCovered(),
                session.getRecordedAt(),
                session.isEditable()
        );
    }
}
