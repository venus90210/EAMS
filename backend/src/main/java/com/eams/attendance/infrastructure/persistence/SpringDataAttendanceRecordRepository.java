package com.eams.attendance.infrastructure.persistence;

import com.eams.attendance.domain.AttendanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio Spring Data JPA — adaptador técnico interno del módulo Attendance.
 */
interface SpringDataAttendanceRecordRepository extends JpaRepository<AttendanceRecord, UUID> {

    @Query("SELECT r FROM AttendanceRecord r WHERE r.sessionId = :sessionId AND r.studentId = :studentId")
    Optional<AttendanceRecord> findBySessionIdAndStudentId(
            @Param("sessionId") UUID sessionId,
            @Param("studentId") UUID studentId);

    @Query("SELECT r FROM AttendanceRecord r WHERE r.sessionId = :sessionId")
    List<AttendanceRecord> findBySessionId(@Param("sessionId") UUID sessionId);

    @Query("SELECT r FROM AttendanceRecord r WHERE r.studentId = :studentId")
    List<AttendanceRecord> findByStudentId(@Param("studentId") UUID studentId);
}
