package com.eams.enrollments.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de salida del módulo Enrollments — abstracción de persistencia.
 */
public interface EnrollmentRepository {

    Enrollment save(Enrollment enrollment);

    Optional<Enrollment> findById(UUID enrollmentId);

    /**
     * Busca si existe un enrollment ACTIVE para el par (studentId, activityId).
     * Usado para validar que no hay duplicados.
     */
    Optional<Enrollment> findActiveByStudentAndActivity(UUID studentId, UUID activityId);

    /**
     * Cuenta cuántos enrollments ACTIVE tiene un estudiante.
     * Validación: máximo 1 enrollment ACTIVE simultáneamente.
     */
    long countActiveByStudentId(UUID studentId);

    /**
     * Lista enrollments de un estudiante, opcionalmente filtrados por estado.
     */
    List<Enrollment> findByStudentId(UUID studentId, EnrollmentStatus status);

    /**
     * Lista enrollments de una actividad, opcionalmente filtrados por estado.
     */
    List<Enrollment> findByActivityId(UUID activityId, EnrollmentStatus status);
}
