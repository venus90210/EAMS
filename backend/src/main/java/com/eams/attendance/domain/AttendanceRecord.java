package com.eams.attendance.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Entidad de dominio: registro de asistencia de un estudiante en una sesión (F2-asistencia.feature).
 *
 * Invariantes:
 *   - session_id + student_id formam clave única (un registro por estudiante por sesión)
 *   - present: true (asistió) | false (falta)
 *   - observation mutable: puede ser editada dentro de ventana 24h
 *   - recorded_at es immutable (set en creación)
 */
@Entity
@Table(name = "attendance_records", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"session_id", "student_id"}, name = "uc_session_student")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id")
public class AttendanceRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "session_id", nullable = false)
    private UUID sessionId;

    @Column(name = "student_id", nullable = false)
    private UUID studentId;

    @Column(nullable = false)
    private Boolean present;

    @Column(columnDefinition = "TEXT")
    private String observation;

    @Column(name = "recorded_at", nullable = false, updatable = false)
    private Instant recordedAt;

    // ── Factory method ──────────────────────────────────────────────────────

    public static AttendanceRecord create(UUID sessionId, UUID studentId, Boolean present, String observation) {
        AttendanceRecord record = new AttendanceRecord();
        record.id = UUID.randomUUID();
        record.sessionId = sessionId;
        record.studentId = studentId;
        record.present = present;
        record.observation = observation != null ? observation.strip() : null;
        record.recordedAt = Instant.now();
        return record;
    }

    // ── Comportamiento de dominio ───────────────────────────────────────────

    /**
     * Actualiza la observación del registro (dentro de ventana 24h).
     *
     * @param newObservation nueva observación
     */
    public void updateObservation(String newObservation) {
        this.observation = newObservation != null ? newObservation.strip() : null;
    }

    /**
     * Marca la asistencia del estudiante.
     *
     * @param present true si asistió, false si falta
     */
    public void markAttendance(Boolean present) {
        this.present = present;
    }
}
