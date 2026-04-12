package com.eams.notification.infrastructure.listener;

import com.eams.notification.application.NotificationService;
import com.eams.shared.events.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Listener de eventos de dominio para notificaciones.
 *
 * Escucha eventos publicados por otros módulos usando @TransactionalEventListener,
 * que garantiza ejecución DESPUÉS de que la transacción se haya commiteado.
 *
 * Delega la lógica de conversión evento → job al NotificationService.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventListener {

    private final NotificationService notificationService;

    @TransactionalEventListener
    public void onEnrollmentConfirmed(EnrollmentConfirmedEvent event) {
        log.debug("Escuchador: recibió EnrollmentConfirmedEvent");
        notificationService.onEnrollmentConfirmed(event);
    }

    @TransactionalEventListener
    public void onSpotExhausted(SpotExhaustedEvent event) {
        log.debug("Escuchador: recibió SpotExhaustedEvent");
        notificationService.onSpotExhausted(event);
    }

    @TransactionalEventListener
    public void onObservationPublished(ObservationPublishedEvent event) {
        log.debug("Escuchador: recibió ObservationPublishedEvent");
        notificationService.onObservationPublished(event);
    }

    @TransactionalEventListener
    public void onActivityStatusChanged(ActivityStatusChangedEvent event) {
        log.debug("Escuchador: recibió ActivityStatusChangedEvent");
        notificationService.onActivityStatusChanged(event);
    }
}
