package com.eams.notification.domain;

import java.util.Optional;

/**
 * Puerto de salida: cola de notificaciones.
 *
 * Abstrae la implementación de la cola (Redis LPUSH/RPOP, BullMQ, etc.)
 * permitiendo que el servicio de notificaciones sea agnóstico de la transport.
 *
 * La implementación concreta vive en infrastructure.queue (AD-03).
 */
public interface NotificationQueuePort {

    /**
     * Encola un job de notificación.
     *
     * @param job Job a encolar
     */
    void push(NotificationJob job);

    /**
     * Desencola el siguiente job pendiente.
     *
     * Llamadas sucesivas retornan diferentes jobs en FIFO.
     *
     * @return Optional con el siguiente job, o empty si la cola está vacía
     */
    Optional<NotificationJob> pollNext();

    /**
     * Marca un job como procesado (idempotencia).
     *
     * Previene que el mismo job se procese dos veces si el worker falla
     * después de enviar el email pero antes de desencolar.
     *
     * @param idempotencyKey Clave de idempotencia del job (típicamente enrollmentId)
     */
    void markProcessed(String idempotencyKey);

    /**
     * Verifica si un job ya fue procesado.
     *
     * @param idempotencyKey Clave de idempotencia del job
     * @return true si ya fue procesado, false en caso contrario
     */
    boolean isProcessed(String idempotencyKey);
}
