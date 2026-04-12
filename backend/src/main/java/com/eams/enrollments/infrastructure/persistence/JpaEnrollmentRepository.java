package com.eams.enrollments.infrastructure.persistence;

import com.eams.enrollments.domain.Enrollment;
import com.eams.enrollments.domain.EnrollmentRepository;
import com.eams.enrollments.domain.EnrollmentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador JPA del puerto de salida EnrollmentRepository (AD-03).
 */
@Repository
@RequiredArgsConstructor
public class JpaEnrollmentRepository implements EnrollmentRepository {

    private final SpringDataEnrollmentRepository spring;

    @Override
    public Enrollment save(Enrollment enrollment) {
        return spring.save(enrollment);
    }

    @Override
    public Optional<Enrollment> findById(UUID enrollmentId) {
        return spring.findById(enrollmentId);
    }

    @Override
    public Optional<Enrollment> findActiveByStudentAndActivity(UUID studentId, UUID activityId) {
        return spring.findActiveByStudentAndActivity(studentId, activityId);
    }

    @Override
    public long countActiveByStudentId(UUID studentId) {
        return spring.countActiveByStudentId(studentId);
    }

    @Override
    public List<Enrollment> findByStudentId(UUID studentId, EnrollmentStatus status) {
        if (status == null) {
            return spring.findByStudentId(studentId);
        }
        return spring.findByStudentIdAndStatus(studentId, status);
    }

    @Override
    public List<Enrollment> findByActivityId(UUID activityId, EnrollmentStatus status) {
        if (status == null) {
            return spring.findByActivityId(activityId);
        }
        return spring.findByActivityIdAndStatus(activityId, status);
    }
}
