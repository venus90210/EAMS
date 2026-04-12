package com.eams.notification.infrastructure.email;

import com.eams.notification.domain.EmailSenderPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import jakarta.mail.internet.MimeMessage;

/**
 * Implementación SMTP del puerto EmailSenderPort.
 *
 * Usa `JavaMailSender` de Spring Mail para despachar emails vía SMTP.
 * Soporta HTML en el cuerpo del email.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SmtpEmailSender implements EmailSenderPort {

    private final JavaMailSender mailSender;

    @Value("${email.from:noreply@eams.edu.co}")
    private String fromEmail;

    @Override
    public void send(String to, String subject, String body) throws Exception {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true); // true = HTML

            mailSender.send(message);
            log.info("Email enviado a {} con asunto '{}'", to, subject);

        } catch (Exception e) {
            log.error("Error enviando email a {}: {}", to, e.getMessage());
            throw e;
        }
    }
}
