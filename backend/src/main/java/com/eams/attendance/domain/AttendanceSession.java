package com.eams.attendance.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Entidad de dominio: sesión de asistencia abierta por un docente (F2-asistencia.feature).
 *
 * Invariantes:
 *   - activity_id obligatorio y debe ser PUBLISHED
 *   - date es el día actual (validado en openSession)
 *   - recorded_at es immutable (set en creación)
 *   - Una sesión por (activity_id, date)
 */
@Entity
@Table(name = "attendance_sessions", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"activity_id", "date"}, name = "uc_activity_date")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id")
public class AttendanceSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "activity_id", nullable = false)
    private UUID activityId;

    @Column(nullable = false)
    private LocalDate date;

    @Column(columnDefinition = "TEXT")
    private String topicsCovered;

    @Column(name = "recorded_at", nullable = false, updatable = false)
    private Instant recordedAt;

    // ── Factory method ──────────────────────────────────────────────────────

    public static AttendanceSession create(UUID activityId, LocalDate date, String topicsCovered) {
        AttendanceSession session = new AttendanceSession();
        session.id = UUID.randomUUID();
        session.activityId = activityId;
        session.date = date;
        session.topicsCovered = topicsCovered != null ? topicsCovered.strip() : null;
        session.recordedAt = Instant.now();
        return session;
    }

    // ── Comportamiento de dominio ───────────────────────────────────────────

    /**
     * Verifica si la sesión está dentro de la ventana de 24h para edición.
     * RF13: máximo 24h después de abierta la sesión.
     */
    public boolean isEditable() {
        Instant limit = recordedAt.plusSeconds(24 * 3600);
        return Instant.now().isBefore(limit);
    }
}
