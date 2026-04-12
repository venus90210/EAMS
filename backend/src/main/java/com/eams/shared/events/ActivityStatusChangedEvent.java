package com.eams.shared.events;

import java.util.UUID;

/**
 * Evento de dominio: cambio de estado de actividad.
 *
 * Publicado por ActivityService tras cambiar el estado (DRAFT → PUBLISHED, etc.).
 * Dispara notificaciones a acudientes de estudiantes inscritos.
 */
public record ActivityStatusChangedEvent(
        UUID activityId,
        String activityName,
        String newStatus,
        UUID institutionId
) {}
