package com.eams.attendance.infrastructure.persistence;

import com.eams.attendance.domain.AttendanceRecord;
import com.eams.attendance.domain.AttendanceRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador JPA del puerto de salida AttendanceRecordRepository (AD-03).
 */
@Repository
@RequiredArgsConstructor
public class JpaAttendanceRecordRepository implements AttendanceRecordRepository {

    private final SpringDataAttendanceRecordRepository spring;

    @Override
    public AttendanceRecord save(AttendanceRecord record) {
        return spring.save(record);
    }

    @Override
    public Optional<AttendanceRecord> findById(UUID recordId) {
        return spring.findById(recordId);
    }

    @Override
    public Optional<AttendanceRecord> findBySessionIdAndStudentId(UUID sessionId, UUID studentId) {
        return spring.findBySessionIdAndStudentId(sessionId, studentId);
    }

    @Override
    public List<AttendanceRecord> findBySessionId(UUID sessionId) {
        return spring.findBySessionId(sessionId);
    }

    @Override
    public List<AttendanceRecord> findByStudentId(UUID studentId) {
        return spring.findByStudentId(studentId);
    }
}
