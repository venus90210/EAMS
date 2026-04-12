package com.eams.shared.events;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Evento de dominio: observación publicada en registro de asistencia.
 *
 * Publicado por AttendanceService tras guardar/actualizar una observación.
 * Dispara envío de email al acudiente con observación del docente.
 */
public record ObservationPublishedEvent(
        UUID recordId,
        UUID studentId,
        String observation,
        LocalDate sessionDate,
        UUID institutionId
) {}
