package com.eams.attendance.infrastructure.persistence;

import com.eams.attendance.domain.AttendanceSession;
import com.eams.attendance.domain.AttendanceSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador JPA del puerto de salida AttendanceSessionRepository (AD-03).
 */
@Repository
@RequiredArgsConstructor
public class JpaAttendanceSessionRepository implements AttendanceSessionRepository {

    private final SpringDataAttendanceSessionRepository spring;

    @Override
    public AttendanceSession save(AttendanceSession session) {
        return spring.save(session);
    }

    @Override
    public Optional<AttendanceSession> findById(UUID sessionId) {
        return spring.findById(sessionId);
    }

    @Override
    public Optional<AttendanceSession> findByActivityIdAndDate(UUID activityId, LocalDate date) {
        return spring.findByActivityIdAndDate(activityId, date);
    }
}
