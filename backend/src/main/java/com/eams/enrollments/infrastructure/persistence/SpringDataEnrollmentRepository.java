package com.eams.enrollments.infrastructure.persistence;

import com.eams.enrollments.domain.Enrollment;
import com.eams.enrollments.domain.EnrollmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio Spring Data JPA — adaptador técnico interno del módulo Enrollments.
 */
interface SpringDataEnrollmentRepository extends JpaRepository<Enrollment, UUID> {

    @Query("SELECT e FROM Enrollment e WHERE e.studentId = :studentId AND e.activityId = :activityId AND e.status = 'ACTIVE'")
    Optional<Enrollment> findActiveByStudentAndActivity(
            @Param("studentId") UUID studentId,
            @Param("activityId") UUID activityId);

    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.studentId = :studentId AND e.status = 'ACTIVE'")
    long countActiveByStudentId(@Param("studentId") UUID studentId);

    @Query("SELECT e FROM Enrollment e WHERE e.studentId = :studentId AND e.status = :status")
    List<Enrollment> findByStudentIdAndStatus(
            @Param("studentId") UUID studentId,
            @Param("status") EnrollmentStatus status);

    @Query("SELECT e FROM Enrollment e WHERE e.studentId = :studentId")
    List<Enrollment> findByStudentId(@Param("studentId") UUID studentId);

    @Query("SELECT e FROM Enrollment e WHERE e.activityId = :activityId AND e.status = :status")
    List<Enrollment> findByActivityIdAndStatus(
            @Param("activityId") UUID activityId,
            @Param("status") EnrollmentStatus status);

    @Query("SELECT e FROM Enrollment e WHERE e.activityId = :activityId")
    List<Enrollment> findByActivityId(@Param("activityId") UUID activityId);
}
