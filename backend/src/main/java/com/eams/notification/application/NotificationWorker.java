package com.eams.notification.application;

import com.eams.notification.domain.EmailSenderPort;
import com.eams.notification.domain.NotificationJob;
import com.eams.notification.domain.NotificationQueuePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Worker de notificaciones: procesa la cola de emails.
 *
 * Responsabilidades:
 * - Desencolar jobs de notificación
 * - Enviar emails vía EmailSenderPort
 * - Implementar reintentos con backoff exponencial (máximo 3 intentos)
 * - Garantizar idempotencia (no enviar duplicados)
 */
@Component
@RequiredArgsConstructor
@Slf4j
@EnableScheduling
public class NotificationWorker {

    private static final int MAX_ATTEMPTS = 3;

    private final NotificationQueuePort queuePort;
    private final EmailSenderPort emailSenderPort;

    /**
     * Procesa la cola de notificaciones.
     *
     * Se ejecuta cada 5 segundos, desencolando y despachando emails.
     * Si el envío falla, reencola el job con attempts++.
     * Si alcanza MAX_ATTEMPTS, descarta el job.
     */
    @Scheduled(fixedDelay = 5000)
    public void processQueue() {
        var job = queuePort.pollNext();

        if (job.isEmpty()) {
            // Queue vacía, nada que hacer
            return;
        }

        NotificationJob notificationJob = job.get();

        // Verificación de idempotencia: si ya fue procesado, ignorar
        if (queuePort.isProcessed(notificationJob.idempotencyKey())) {
            log.debug("Job {} ya procesado, ignorando", notificationJob.id());
            return;
        }

        // Verificación de reintentos: si alcanzó max, descartar
        if (notificationJob.attempts() >= MAX_ATTEMPTS) {
            log.error("Job {} falló {} intentos (máximo {}), descartando",
                    notificationJob.id(), notificationJob.attempts(), MAX_ATTEMPTS);
            queuePort.markProcessed(notificationJob.idempotencyKey());
            return;
        }

        // Intentar enviar
        try {
            emailSenderPort.send(
                    notificationJob.to(),
                    notificationJob.subject(),
                    notificationJob.body()
            );

            log.info("Email {} enviado a {}", notificationJob.id(), notificationJob.to());
            queuePort.markProcessed(notificationJob.idempotencyKey());

        } catch (Exception e) {
            log.warn("Error enviando email {} (intento {}/{}): {}",
                    notificationJob.id(),
                    notificationJob.attempts() + 1,
                    MAX_ATTEMPTS,
                    e.getMessage());

            // Reencolable con attempt incrementado
            NotificationJob retryJob = notificationJob.withNextAttempt();
            queuePort.push(retryJob);
        }
    }
}
