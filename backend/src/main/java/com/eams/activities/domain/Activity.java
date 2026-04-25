package com.eams.activities.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Entidad de dominio: actividad extracurricular de una institución (F5-estado-actividad.feature).
 *
 * Invariantes:
 *   - institution_id obligatorio (multi-tenancy AD-08)
 *   - status regula el ciclo de vida (DRAFT → PUBLISHED → DISABLED ↔ PUBLISHED)
 *   - available_spots <= total_spots
 *   - total_spots inmutable operativamente (solo ADMIN via audit log)
 */
@Entity
@Table(name = "activities")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id")
public class Activity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActivityStatus status;

    @Column(name = "total_spots", nullable = false)
    private Integer totalSpots;

    @Column(name = "available_spots", nullable = false)
    private Integer availableSpots;

    @Embedded
    private Schedule schedule;

    @Column(name = "institution_id", nullable = false)
    private UUID institutionId;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    // ── Factory method ──────────────────────────────────────────────────────

    public static Activity create(String name,
                                   String description,
                                   Integer totalSpots,
                                   Schedule schedule,
                                   UUID institutionId) {
        return create(name, description, totalSpots, schedule, institutionId, null);
    }

    public static Activity create(String name,
                                   String description,
                                   Integer totalSpots,
                                   Schedule schedule,
                                   UUID institutionId,
                                   UUID createdBy) {
        Activity a = new Activity();
        a.id                = UUID.randomUUID();
        a.name              = name.strip();
        a.description       = description != null ? description.strip() : null;
        a.status            = ActivityStatus.DRAFT;
        a.totalSpots        = totalSpots;
        a.availableSpots    = totalSpots;  // Al crear, todos los cupos están disponibles
        a.schedule          = schedule;
        a.institutionId     = institutionId;
        a.createdBy         = createdBy;
        return a;
    }

    // ── Comportamiento de dominio ───────────────────────────────────────────

    /**
     * Transiciona el estado de la actividad.
     *
     * @throws IllegalArgumentException si la transición no es válida
     */
    public void transitionTo(ActivityStatus newStatus) {
        status.validateTransitionTo(newStatus);
        this.status = newStatus;
    }

    /**
     * Actualiza nombre y descripción (TEACHER y ADMIN).
     */
    public void updateBasicInfo(String name, String description) {
        if (name != null && !name.isBlank()) this.name = name.strip();
        if (description != null && !description.isBlank()) this.description = description.strip();
    }

    /**
     * Actualiza el horario (TEACHER y ADMIN).
     */
    public void updateSchedule(Schedule schedule) {
        if (schedule != null) this.schedule = schedule;
    }

    /**
     * Actualiza total_spots (solo ADMIN, genera audit log).
     * Ajusta available_spots proportionalmente si es necesario.
     */
    public void updateTotalSpots(Integer newTotalSpots) {
        if (newTotalSpots == null || newTotalSpots < 1) {
            throw new IllegalArgumentException("totalSpots debe ser >= 1");
        }

        // Si se reduce total_spots y available_spots es mayor, ajustar disponibilidad
        int enrolled = this.totalSpots - this.availableSpots;
        int newAvailable = newTotalSpots - enrolled;
        if (newAvailable < 0) {
            newAvailable = 0;  // Nunca puede ser negativo
        }

        this.totalSpots = newTotalSpots;
        this.availableSpots = newAvailable;
    }

    /**
     * Decrementa cupos disponibles (usado en inscripción).
     */
    public void decrementAvailableSpots() {
        if (availableSpots > 0) {
            availableSpots--;
        }
    }

    /**
     * Incrementa cupos disponibles (usado en cancelación de inscripción).
     */
    public void incrementAvailableSpots() {
        if (availableSpots < totalSpots) {
            availableSpots++;
        }
    }

    /**
     * Retorna true si la actividad está aceptando inscripciones.
     */
    public boolean isAcceptingEnrollments() {
        return status == ActivityStatus.PUBLISHED && availableSpots > 0;
    }
}
