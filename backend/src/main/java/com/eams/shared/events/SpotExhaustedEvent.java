package com.eams.shared.events;

import java.util.UUID;

/**
 * Evento de dominio: cupos agotados en una actividad.
 *
 * Publicado por EnrollmentService cuando availableSpots llega a 0.
 * Dispara notificación a administradores.
 */
public record SpotExhaustedEvent(
        UUID activityId,
        String activityName,
        UUID institutionId
) {}
