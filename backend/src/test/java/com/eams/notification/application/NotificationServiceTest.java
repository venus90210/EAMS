package com.eams.notification.application;

import com.eams.notification.domain.NotificationJob;
import com.eams.notification.domain.NotificationQueuePort;
import com.eams.shared.events.*;
import com.eams.shared.user.UserEmailPort;
import com.eams.users.api.StudentRepositoryApi;
import com.eams.users.domain.Student;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationService Tests")
@org.junit.jupiter.api.Tag("unit")
class NotificationServiceTest {

    @Mock
    private NotificationQueuePort queuePort;

    @Mock
    private UserEmailPort userEmailPort;

    @Mock
    private StudentRepositoryApi studentRepository;

    private NotificationService notificationService;

    private UUID enrollmentId;
    private UUID studentId;
    private UUID activityId;
    private UUID guardianId;
    private UUID institutionId;
    private UUID recordId;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationService(queuePort, userEmailPort, studentRepository);

        enrollmentId = UUID.randomUUID();
        studentId = UUID.randomUUID();
        activityId = UUID.randomUUID();
        guardianId = UUID.randomUUID();
        institutionId = UUID.randomUUID();
        recordId = UUID.randomUUID();
    }

    // ── onEnrollmentConfirmed() Tests ─────────────────────────────────────────

    @Test
    @DisplayName("onEnrollmentConfirmed() should enqueue job when student found")
    void testEnrollmentConfirmedSuccess() {
        // Arrange
        Student mockStudent = Student.create("Juan", "Pérez", "7A", institutionId, guardianId);
        EnrollmentConfirmedEvent event = new EnrollmentConfirmedEvent(
                enrollmentId, studentId, activityId, "Fútbol", institutionId
        );
        when(studentRepository.findById(studentId)).thenReturn(Optional.of(mockStudent));
        when(userEmailPort.findEmailById(guardianId)).thenReturn(Optional.of("guardian@example.com"));
        when(queuePort.isProcessed(enrollmentId.toString())).thenReturn(false);

        // Act
        notificationService.onEnrollmentConfirmed(event);

        // Assert
        ArgumentCaptor<NotificationJob> jobCaptor = ArgumentCaptor.forClass(NotificationJob.class);
        verify(queuePort).push(jobCaptor.capture());

        NotificationJob job = jobCaptor.getValue();
        assertThat(job)
                .isNotNull()
                .extracting("type", "to", "idempotencyKey", "attempts")
                .containsExactly("ENROLLMENT_CONFIRMED", "guardian@example.com", enrollmentId.toString(), 0);
        assertThat(job.subject()).contains("Fútbol");
    }

    @Test
    @DisplayName("onEnrollmentConfirmed() should not enqueue if already processed")
    void testEnrollmentConfirmedIdempotent() {
        // Arrange
        EnrollmentConfirmedEvent event = new EnrollmentConfirmedEvent(
                enrollmentId, studentId, activityId, "Fútbol", institutionId
        );
        when(queuePort.isProcessed(enrollmentId.toString())).thenReturn(true);

        // Act
        notificationService.onEnrollmentConfirmed(event);

        // Assert
        verify(queuePort, never()).push(any(NotificationJob.class));
        verify(studentRepository, never()).findById(any());
    }

    @Test
    @DisplayName("onEnrollmentConfirmed() should not enqueue if guardian email not found")
    void testEnrollmentConfirmedGuardianEmailNotFound() {
        // Arrange
        Student mockStudent = Student.create("Juan", "Pérez", "7A", institutionId, guardianId);
        EnrollmentConfirmedEvent event = new EnrollmentConfirmedEvent(
                enrollmentId, studentId, activityId, "Fútbol", institutionId
        );
        when(studentRepository.findById(studentId)).thenReturn(Optional.of(mockStudent));
        when(userEmailPort.findEmailById(guardianId)).thenReturn(Optional.empty());
        when(queuePort.isProcessed(enrollmentId.toString())).thenReturn(false);

        // Act
        notificationService.onEnrollmentConfirmed(event);

        // Assert
        verify(queuePort, never()).push(any(NotificationJob.class));
    }

    // ── onObservationPublished() Tests ────────────────────────────────────────

    @Test
    @DisplayName("onObservationPublished() should enqueue job with observation text")
    void testObservationPublishedSuccess() {
        // Arrange
        Student mockStudent = Student.create("Juan", "Pérez", "7A", institutionId, guardianId);
        LocalDate sessionDate = LocalDate.now().minusDays(1);
        ObservationPublishedEvent event = new ObservationPublishedEvent(
                recordId, studentId, "Excelente comportamiento", sessionDate, institutionId
        );
        when(studentRepository.findById(studentId)).thenReturn(Optional.of(mockStudent));
        when(userEmailPort.findEmailById(guardianId)).thenReturn(Optional.of("guardian@example.com"));
        when(queuePort.isProcessed(recordId.toString())).thenReturn(false);

        // Act
        notificationService.onObservationPublished(event);

        // Assert
        ArgumentCaptor<NotificationJob> jobCaptor = ArgumentCaptor.forClass(NotificationJob.class);
        verify(queuePort).push(jobCaptor.capture());

        NotificationJob job = jobCaptor.getValue();
        assertThat(job)
                .isNotNull()
                .extracting("type", "to", "idempotencyKey")
                .containsExactly("OBSERVATION_PUBLISHED", "guardian@example.com", recordId.toString());
        assertThat(job.body()).contains("Excelente comportamiento");
    }

    @Test
    @DisplayName("onObservationPublished() should not enqueue if already processed")
    void testObservationPublishedIdempotent() {
        // Arrange
        LocalDate sessionDate = LocalDate.now();
        ObservationPublishedEvent event = new ObservationPublishedEvent(
                recordId, studentId, "Observación", sessionDate, institutionId
        );
        when(queuePort.isProcessed(recordId.toString())).thenReturn(true);

        // Act
        notificationService.onObservationPublished(event);

        // Assert
        verify(queuePort, never()).push(any(NotificationJob.class));
        verify(studentRepository, never()).findById(any());
    }

    // ── onSpotExhausted() Tests ──────────────────────────────────────────────

    @Test
    @DisplayName("onSpotExhausted() should log event (Phase 1.8 enhancement)")
    void testSpotExhaustedLogged() {
        // Arrange
        SpotExhaustedEvent event = new SpotExhaustedEvent(activityId, "Fútbol", institutionId);

        // Act
        notificationService.onSpotExhausted(event);

        // Assert - Just verify no exceptions; full implementation deferred to Phase 1.8
        verify(queuePort, never()).push(any());
    }

    // ── onActivityStatusChanged() Tests ──────────────────────────────────────

    @Test
    @DisplayName("onActivityStatusChanged() should log event (Phase 1.8 enhancement)")
    void testActivityStatusChangedLogged() {
        // Arrange
        ActivityStatusChangedEvent event = new ActivityStatusChangedEvent(
                activityId, "Fútbol", "PUBLISHED", institutionId
        );

        // Act
        notificationService.onActivityStatusChanged(event);

        // Assert - Just verify no exceptions; full implementation deferred to Phase 1.8
        verify(queuePort, never()).push(any());
    }
}
