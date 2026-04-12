package com.eams.notification.domain;

/**
 * Puerto de salida: despacho de email.
 *
 * Abstrae la implementación del transporte SMTP (JavaMailSender, SendGrid, Resend, etc.)
 *
 * La implementación concreta vive en infrastructure.email (AD-03).
 */
public interface EmailSenderPort {

    /**
     * Envía un email.
     *
     * @param to Email de destino
     * @param subject Asunto
     * @param body Cuerpo del email (puede ser HTML)
     * @throws Exception si falla el envío (reintentable en el worker)
     */
    void send(String to, String subject, String body) throws Exception;
}
