package com.eams.notification.application;

import com.eams.notification.domain.EmailSenderPort;
import com.eams.notification.domain.NotificationJob;
import com.eams.notification.domain.NotificationQueuePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationWorker Tests")
@org.junit.jupiter.api.Tag("unit")
class NotificationWorkerTest {

    @Mock
    private NotificationQueuePort queuePort;

    @Mock
    private EmailSenderPort emailSenderPort;

    private NotificationWorker notificationWorker;

    private UUID jobId;
    private String idempotencyKey;

    @BeforeEach
    void setUp() {
        notificationWorker = new NotificationWorker(queuePort, emailSenderPort);
        jobId = UUID.randomUUID();
        idempotencyKey = UUID.randomUUID().toString();
    }

    // ── processQueue() Tests ──────────────────────────────────────────────────

    @Test
    @DisplayName("processQueue() should do nothing when queue is empty")
    void testProcessQueueEmpty() throws Exception {
        // Arrange
        when(queuePort.pollNext()).thenReturn(Optional.empty());

        // Act
        notificationWorker.processQueue();

        // Assert
        verify(emailSenderPort, never()).send(anyString(), anyString(), anyString());
        verify(queuePort, never()).markProcessed(anyString());
        verify(queuePort, never()).push(any(NotificationJob.class));
    }

    @Test
    @DisplayName("processQueue() should send email and mark processed on success")
    void testProcessQueueSuccess() throws Exception {
        // Arrange
        NotificationJob job = new NotificationJob(
                jobId,
                "ENROLLMENT_CONFIRMED",
                "guardian@example.com",
                "Inscripción Confirmada",
                "<h1>Confirmada</h1>",
                idempotencyKey,
                0
        );
        when(queuePort.pollNext()).thenReturn(Optional.of(job));
        when(queuePort.isProcessed(idempotencyKey)).thenReturn(false);

        // Act
        notificationWorker.processQueue();

        // Assert
        verify(emailSenderPort).send("guardian@example.com", "Inscripción Confirmada", "<h1>Confirmada</h1>");
        verify(queuePort).markProcessed(idempotencyKey);
        verify(queuePort, never()).push(any()); // No requeue on success
    }

    @Test
    @DisplayName("processQueue() should skip if job already processed")
    void testProcessQueueIdempotencyGuard() throws Exception {
        // Arrange
        NotificationJob job = new NotificationJob(
                jobId,
                "ENROLLMENT_CONFIRMED",
                "guardian@example.com",
                "Inscripción Confirmada",
                "<h1>Confirmada</h1>",
                idempotencyKey,
                0
        );
        when(queuePort.pollNext()).thenReturn(Optional.of(job));
        when(queuePort.isProcessed(idempotencyKey)).thenReturn(true);

        // Act
        notificationWorker.processQueue();

        // Assert
        verify(emailSenderPort, never()).send(anyString(), anyString(), anyString());
        verify(queuePort, never()).markProcessed(anyString());
        verify(queuePort, never()).push(any());
    }

    @Test
    @DisplayName("processQueue() should requeue on send failure (attempt < 3)")
    void testProcessQueueSendFailureRequeue() throws Exception {
        // Arrange
        NotificationJob job = new NotificationJob(
                jobId,
                "ENROLLMENT_CONFIRMED",
                "guardian@example.com",
                "Inscripción Confirmada",
                "<h1>Confirmada</h1>",
                idempotencyKey,
                1 // Already 1 attempt
        );
        when(queuePort.pollNext()).thenReturn(Optional.of(job));
        when(queuePort.isProcessed(idempotencyKey)).thenReturn(false);
        doThrow(new Exception("SMTP error"))
                .when(emailSenderPort).send(anyString(), anyString(), anyString());

        // Act
        notificationWorker.processQueue();

        // Assert
        verify(emailSenderPort).send("guardian@example.com", "Inscripción Confirmada", "<h1>Confirmada</h1>");
        verify(queuePort, never()).markProcessed(anyString()); // Not marked yet
        ArgumentCaptor<NotificationJob> retryCaptor = ArgumentCaptor.forClass(NotificationJob.class);
        verify(queuePort).push(retryCaptor.capture());

        NotificationJob retryJob = retryCaptor.getValue();
        assertThat(retryJob)
                .extracting("attempts", "idempotencyKey")
                .containsExactly(2, idempotencyKey); // Attempts incremented
    }

    @Test
    @DisplayName("processQueue() should discard job after 3 failed attempts")
    void testProcessQueueMaxAttemptsExceeded() throws Exception {
        // Arrange
        NotificationJob job = new NotificationJob(
                jobId,
                "ENROLLMENT_CONFIRMED",
                "guardian@example.com",
                "Inscripción Confirmada",
                "<h1>Confirmada</h1>",
                idempotencyKey,
                3 // Already at max
        );
        when(queuePort.pollNext()).thenReturn(Optional.of(job));
        when(queuePort.isProcessed(idempotencyKey)).thenReturn(false);

        // Act
        notificationWorker.processQueue();

        // Assert
        verify(emailSenderPort, never()).send(anyString(), anyString(), anyString());
        verify(queuePort).markProcessed(idempotencyKey); // Marked to prevent re-processing
        verify(queuePort, never()).push(any()); // Not requeued
    }

    @Test
    @DisplayName("processQueue() should handle multiple retries sequentially")
    void testProcessQueueMultipleRetries() throws Exception {
        // Arrange: First call returns job with 0 attempts
        NotificationJob firstJob = new NotificationJob(
                jobId, "TEST", "test@example.com", "Subject", "Body", idempotencyKey, 0
        );
        when(queuePort.pollNext())
                .thenReturn(Optional.of(firstJob))
                .thenReturn(Optional.empty());
        when(queuePort.isProcessed(idempotencyKey)).thenReturn(false);
        doThrow(new Exception("Network error"))
                .when(emailSenderPort).send(anyString(), anyString(), anyString());

        // Act
        notificationWorker.processQueue();

        // Assert: Should fail and requeue once
        verify(queuePort).push(argThat(job -> job.attempts() == 1));
    }
}
