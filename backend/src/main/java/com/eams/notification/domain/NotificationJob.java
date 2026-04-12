package com.eams.notification.domain;

import java.util.UUID;

/**
 * Objeto de valor: un trabajo de notificación por enviar.
 *
 * Representa un email en cola, con soporte para idempotencia y reintentos.
 *
 * @param id Identificador único del job (UUID)
 * @param type Tipo de notificación (ENROLLMENT_CONFIRMED, OBSERVATION_PUBLISHED, etc.)
 * @param to Email de destino
 * @param subject Asunto del email
 * @param body Cuerpo del email (HTML)
 * @param idempotencyKey Clave de idempotencia para evitar duplicados (ej. enrollmentId)
 * @param attempts Número de intentos realizados (0 en la primera)
 */
public record NotificationJob(
        UUID id,
        String type,
        String to,
        String subject,
        String body,
        String idempotencyKey,
        int attempts
) {
    public NotificationJob {
        if (id == null) throw new IllegalArgumentException("id requerido");
        if (type == null || type.isBlank()) throw new IllegalArgumentException("type requerido");
        if (to == null || to.isBlank()) throw new IllegalArgumentException("to requerido");
        if (subject == null || subject.isBlank()) throw new IllegalArgumentException("subject requerido");
        if (body == null || body.isBlank()) throw new IllegalArgumentException("body requerido");
        if (idempotencyKey == null || idempotencyKey.isBlank()) throw new IllegalArgumentException("idempotencyKey requerido");
        if (attempts < 0) throw new IllegalArgumentException("attempts no puede ser negativo");
    }

    /**
     * Retorna un nuevo job con el número de intentos incrementado.
     */
    public NotificationJob withNextAttempt() {
        return new NotificationJob(id, type, to, subject, body, idempotencyKey, attempts + 1);
    }
}
