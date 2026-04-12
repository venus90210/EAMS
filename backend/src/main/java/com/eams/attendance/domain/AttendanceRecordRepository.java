package com.eams.attendance.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de salida del módulo Attendance — abstracción de persistencia de registros de asistencia.
 */
public interface AttendanceRecordRepository {

    AttendanceRecord save(AttendanceRecord record);

    Optional<AttendanceRecord> findById(UUID recordId);

    Optional<AttendanceRecord> findBySessionIdAndStudentId(UUID sessionId, UUID studentId);

    List<AttendanceRecord> findBySessionId(UUID sessionId);

    List<AttendanceRecord> findByStudentId(UUID studentId);
}
