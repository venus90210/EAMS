package com.eams.enrollments.domain;

/**
 * Estados del enrollment (F1-inscripcion.feature).
 *
 * ACTIVE    → estudiante inscrito, participa en actividad
 * CANCELLED → inscripción cancelada, cupo liberado
 */
public enum EnrollmentStatus {
    ACTIVE,
    CANCELLED
}
