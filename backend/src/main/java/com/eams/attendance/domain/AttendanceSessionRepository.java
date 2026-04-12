package com.eams.attendance.domain;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de salida del módulo Attendance — abstracción de persistencia de sesiones.
 */
public interface AttendanceSessionRepository {

    AttendanceSession save(AttendanceSession session);

    Optional<AttendanceSession> findById(UUID sessionId);

    Optional<AttendanceSession> findByActivityIdAndDate(UUID activityId, LocalDate date);
}
