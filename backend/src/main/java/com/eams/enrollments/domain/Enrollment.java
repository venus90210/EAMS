package com.eams.enrollments.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Entidad de dominio: inscripción de un estudiante en una actividad (F1-inscripcion.feature).
 *
 * Invariantes:
 *   - student_id + activity_id no puede repetirse con status=ACTIVE (constraint DB)
 *   - status regula el ciclo de vida: ACTIVE → CANCELLED (irreversible)
 *   - enrolled_at se set en creación; cancelled_at se set al cancelar
 */
@Entity
@Table(name = "enrollments", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"student_id", "activity_id"},
                          name = "uc_student_activity")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id")
public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "student_id", nullable = false)
    private UUID studentId;

    @Column(name = "activity_id", nullable = false)
    private UUID activityId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EnrollmentStatus status;

    @Column(name = "enrolled_at", nullable = false, updatable = false)
    private Instant enrolledAt;

    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    // ── Factory method ──────────────────────────────────────────────────────

    public static Enrollment create(UUID studentId, UUID activityId) {
        Enrollment e = new Enrollment();
        e.id         = UUID.randomUUID();
        e.studentId  = studentId;
        e.activityId = activityId;
        e.status     = EnrollmentStatus.ACTIVE;
        e.enrolledAt = Instant.now();
        return e;
    }

    // ── Comportamiento de dominio ───────────────────────────────────────────

    /**
     * Cancela la inscripción.
     *
     * @throws IllegalStateException si ya está cancelada
     */
    public void cancel() {
        if (status == EnrollmentStatus.CANCELLED) {
            throw new IllegalStateException("La inscripción ya está cancelada");
        }
        this.status = EnrollmentStatus.CANCELLED;
        this.cancelledAt = Instant.now();
    }

    public boolean isActive() {
        return status == EnrollmentStatus.ACTIVE;
    }
}
