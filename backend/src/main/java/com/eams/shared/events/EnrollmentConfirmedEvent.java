package com.eams.shared.events;

import java.util.UUID;

/**
 * Evento de dominio: confirmación de inscripción exitosa.
 *
 * Publicado por EnrollmentService tras guardar una inscripción activa.
 * Dispara envío de email de confirmación al acudiente del estudiante.
 */
public record EnrollmentConfirmedEvent(
        UUID enrollmentId,
        UUID studentId,
        UUID activityId,
        String activityName,
        UUID institutionId
) {}
