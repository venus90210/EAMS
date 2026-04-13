package com.eams.attendance.application.dto;

import com.eams.attendance.domain.AttendanceSession;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record AttendanceSessionResponse(
        UUID id,
        UUID activityId,
        LocalDate date,
        String topicsCovered,
        Instant recordedAt,
        Boolean isEditable,
        List<AttendanceStudentDto> students
) {
    public static AttendanceSessionResponse from(AttendanceSession session, List<AttendanceStudentDto> students) {
        return new AttendanceSessionResponse(
                session.getId(),
                session.getActivityId(),
                session.getDate(),
                session.getTopicsCovered(),
                session.getRecordedAt(),
                session.isEditable(),
                students
        );
    }
}
