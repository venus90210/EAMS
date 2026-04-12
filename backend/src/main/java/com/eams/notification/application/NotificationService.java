package com.eams.notification.application;

import com.eams.notification.domain.NotificationJob;
import com.eams.notification.domain.NotificationQueuePort;
import com.eams.shared.events.*;
import com.eams.shared.user.UserEmailPort;
import com.eams.users.api.StudentRepositoryApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

/**
 * Servicio de aplicación: maneja conversión de eventos a jobs de notificación.
 *
 * Responsabilidades:
 * - Recibir eventos de dominio desde otros módulos
 * - Convertir eventos a NotificationJob con datos de email y asunto
 * - Verificar idempotencia (no encolar si ya fue procesado)
 * - Encolar en la cola para procesamiento asincrónico
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationQueuePort queuePort;
    private final UserEmailPort userEmailPort;
    private final StudentRepositoryApi studentRepository;

    /**
     * Maneja el evento de inscripción confirmada.
     *
     * Encola email de confirmación al acudiente del estudiante.
     */
    public void onEnrollmentConfirmed(EnrollmentConfirmedEvent event) {
        log.debug("Procesando EnrollmentConfirmedEvent para inscripción {}", event.enrollmentId());

        if (queuePort.isProcessed(event.enrollmentId().toString())) {
            log.warn("Inscripción {} ya notificada, ignorando evento duplicado", event.enrollmentId());
            return;
        }

        Optional<String> guardianEmail = studentRepository.findById(event.studentId())
                .flatMap(student -> userEmailPort.findEmailById(student.getGuardianId()));

        if (guardianEmail.isEmpty()) {
            log.error("No se encontró email del acudiente para estudiante {}", event.studentId());
            return;
        }

        NotificationJob job = new NotificationJob(
                UUID.randomUUID(),
                "ENROLLMENT_CONFIRMED",
                guardianEmail.get(),
                "Inscripción confirmada: " + event.activityName(),
                buildEnrollmentConfirmedBody(event),
                event.enrollmentId().toString(),
                0
        );

        queuePort.push(job);
        log.info("Job de inscripción {} encolado para {}", job.id(), guardianEmail.get());
    }

    /**
     * Maneja el evento de cupos agotados.
     *
     * Encola notificación a administradores.
     */
    public void onSpotExhausted(SpotExhaustedEvent event) {
        log.debug("Procesando SpotExhaustedEvent para actividad {}", event.activityId());

        // TODO: En Phase 1.8, obtener emails de ADMIN según institutionId
        // Por ahora, solo registramos el evento
        log.warn("Cupos agotados para actividad: {}", event.activityName());
    }

    /**
     * Maneja el evento de observación publicada.
     *
     * Encola email con la observación del docente al acudiente del estudiante.
     */
    public void onObservationPublished(ObservationPublishedEvent event) {
        log.debug("Procesando ObservationPublishedEvent para registro {}", event.recordId());

        if (queuePort.isProcessed(event.recordId().toString())) {
            log.warn("Registro {} ya notificado, ignorando evento duplicado", event.recordId());
            return;
        }

        Optional<String> guardianEmail = studentRepository.findById(event.studentId())
                .flatMap(student -> userEmailPort.findEmailById(student.getGuardianId()));

        if (guardianEmail.isEmpty()) {
            log.error("No se encontró email del acudiente para estudiante {}", event.studentId());
            return;
        }

        NotificationJob job = new NotificationJob(
                UUID.randomUUID(),
                "OBSERVATION_PUBLISHED",
                guardianEmail.get(),
                "Observación del " + event.sessionDate(),
                buildObservationBody(event),
                event.recordId().toString(),
                0
        );

        queuePort.push(job);
        log.info("Job de observación {} encolado para {}", job.id(), guardianEmail.get());
    }

    /**
     * Maneja el evento de cambio de estado de actividad.
     *
     * TODO: Notificar a acudientes de estudiantes inscritos.
     */
    public void onActivityStatusChanged(ActivityStatusChangedEvent event) {
        log.debug("Procesando ActivityStatusChangedEvent para actividad {}", event.activityId());

        // TODO: En Phase 1.8, obtener emails de acudientes inscritos a la actividad
        // Por ahora, solo registramos el evento
        log.info("Estado de actividad {} cambió a {}", event.activityName(), event.newStatus());
    }

    // ── Templates de email ────────────────────────────────────────────────

    private String buildEnrollmentConfirmedBody(EnrollmentConfirmedEvent event) {
        return String.format(
                "<h2>Inscripción Confirmada</h2>" +
                "<p>La inscripción a <strong>%s</strong> ha sido confirmada.</p>" +
                "<p>Consulta la plataforma para más detalles.</p>",
                event.activityName()
        );
    }

    private String buildObservationBody(ObservationPublishedEvent event) {
        return String.format(
                "<h2>Observación del %s</h2>" +
                "<p><strong>Observación:</strong> %s</p>" +
                "<p>Consulta la plataforma para más contexto.</p>",
                event.sessionDate(),
                event.observation()
        );
    }
}
