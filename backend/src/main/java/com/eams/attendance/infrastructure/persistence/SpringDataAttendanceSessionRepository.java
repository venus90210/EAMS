package com.eams.attendance.infrastructure.persistence;

import com.eams.attendance.domain.AttendanceSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio Spring Data JPA — adaptador técnico interno del módulo Attendance.
 */
interface SpringDataAttendanceSessionRepository extends JpaRepository<AttendanceSession, UUID> {

    @Query("SELECT s FROM AttendanceSession s WHERE s.activityId = :activityId AND s.date = :date")
    Optional<AttendanceSession> findByActivityIdAndDate(
            @Param("activityId") UUID activityId,
            @Param("date") LocalDate date);
}
