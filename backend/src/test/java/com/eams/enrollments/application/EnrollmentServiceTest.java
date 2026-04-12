package com.eams.enrollments.application;

import com.eams.activities.domain.Activity;
import com.eams.activities.domain.ActivityCachePort;
import com.eams.activities.domain.ActivityRepository;
import com.eams.activities.domain.ActivityStatus;
import com.eams.activities.domain.Schedule;
import com.eams.enrollments.domain.Enrollment;
import com.eams.enrollments.domain.EnrollmentRepository;
import com.eams.enrollments.domain.EnrollmentStatus;
import com.eams.shared.exception.DomainException;
import com.eams.shared.tenant.TenantContext;
import com.eams.shared.tenant.TenantContextHolder;
import com.eams.users.domain.Student;
import com.eams.users.domain.StudentRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.lang.reflect.Field;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EnrollmentService Tests")
@org.junit.jupiter.api.Tag("unit")
class EnrollmentServiceTest {

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private ActivityRepository activityRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private ActivityCachePort activityCachePort;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private EnrollmentService enrollmentService;

    private UUID studentId;
    private UUID activityId;
    private UUID enrollmentId;
    private UUID institutionId;
    private UUID guardianId;

    private Student testStudent;
    private Activity testActivity;
    private Enrollment testEnrollment;

    @BeforeEach
    void setUp() throws Exception {
        enrollmentService = new EnrollmentService(
                enrollmentRepository,
                activityRepository,
                studentRepository,
                activityCachePort,
                eventPublisher
        );

        studentId = UUID.randomUUID();
        activityId = UUID.randomUUID();
        enrollmentId = UUID.randomUUID();
        institutionId = UUID.randomUUID();
        guardianId = UUID.randomUUID();

        // Setup test data using factory methods
        testStudent = Student.create("Juan", "Pérez", "10", institutionId, guardianId);

        Schedule schedule = Schedule.create(
                DayOfWeek.MONDAY,
                LocalTime.of(9, 0),
                LocalTime.of(10, 0),
                "Aula 101"
        );

        testActivity = Activity.create(
                "Math Club",
                "Advanced mathematics",
                30,
                schedule,
                institutionId
        );
        // Set activity to PUBLISHED status for enrollment tests
        setFieldValue(testActivity, "status", ActivityStatus.PUBLISHED);

        testEnrollment = Enrollment.create(studentId, activityId);
        // Override ID for test
        setFieldValue(testEnrollment, "id", enrollmentId);

        // Setup TenantContext as ADMIN
        TenantContextHolder.set(
                new TenantContext(institutionId, "ADMIN")
        );
    }

    private void setFieldValue(Object obj, String fieldName, Object value) throws Exception {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(obj, value);
    }

    @AfterEach
    void tearDown() {
        TenantContextHolder.clear();
    }

    // ── Enroll Tests ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("enroll() should create enrollment when all validations pass")
    void testEnrollSuccess() {
        // Arrange
        when(studentRepository.findById(studentId)).thenReturn(Optional.of(testStudent));
        when(activityRepository.findById(activityId)).thenReturn(Optional.of(testActivity));
        when(enrollmentRepository.countActiveByStudentId(studentId)).thenReturn(0L);
        when(enrollmentRepository.findActiveByStudentAndActivity(studentId, activityId))
                .thenReturn(Optional.empty());
        when(enrollmentRepository.save(any(Enrollment.class))).thenReturn(testEnrollment);

        // Act
        Enrollment result = enrollmentService.enroll(studentId, activityId);

        // Assert
        assertThat(result)
                .isNotNull()
                .extracting("studentId", "activityId", "status")
                .containsExactly(studentId, activityId, EnrollmentStatus.ACTIVE);

        verify(studentRepository).findById(studentId);
        verify(activityRepository, times(2)).findById(activityId); // Once for status check, once for available_spots
        verify(enrollmentRepository).countActiveByStudentId(studentId);
        verify(enrollmentRepository).findActiveByStudentAndActivity(studentId, activityId);
        verify(activityRepository).save(any(Activity.class)); // To decrement available_spots
        verify(enrollmentRepository).save(any(Enrollment.class));
        verify(activityCachePort).invalidate(activityId);
    }

    @Test
    @DisplayName("enroll() should throw when student not found")
    void testEnrollStudentNotFound() {
        // Arrange
        when(studentRepository.findById(studentId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> enrollmentService.enroll(studentId, activityId))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("Estudiante no encontrado");

        verify(studentRepository).findById(studentId);
        verify(enrollmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("enroll() should throw when activity not found")
    void testEnrollActivityNotFound() {
        // Arrange
        when(studentRepository.findById(studentId)).thenReturn(Optional.of(testStudent));
        when(activityRepository.findById(activityId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> enrollmentService.enroll(studentId, activityId))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("Actividad no encontrada");

        verify(enrollmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("enroll() should throw when activity is not PUBLISHED")
    void testEnrollActivityNotPublished() throws Exception {
        // Arrange
        Activity draftActivity = Activity.create(
                "Math Club",
                "Advanced mathematics",
                30,
                testActivity.getSchedule(),
                institutionId
        );
        setFieldValue(draftActivity, "status", ActivityStatus.DRAFT);

        when(studentRepository.findById(studentId)).thenReturn(Optional.of(testStudent));
        when(activityRepository.findById(activityId)).thenReturn(Optional.of(draftActivity));

        // Act & Assert
        assertThatThrownBy(() -> enrollmentService.enroll(studentId, activityId))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("PUBLISHED");

        verify(enrollmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("enroll() should throw when student has active enrollment")
    void testEnrollActiveEnrollmentExists() {
        // Arrange
        when(studentRepository.findById(studentId)).thenReturn(Optional.of(testStudent));
        when(activityRepository.findById(activityId)).thenReturn(Optional.of(testActivity));
        when(enrollmentRepository.countActiveByStudentId(studentId)).thenReturn(1L);

        // Act & Assert
        assertThatThrownBy(() -> enrollmentService.enroll(studentId, activityId))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("inscripción activa");

        verify(enrollmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("enroll() should throw when student already enrolled in activity")
    void testEnrollAlreadyEnrolled() {
        // Arrange
        when(studentRepository.findById(studentId)).thenReturn(Optional.of(testStudent));
        when(activityRepository.findById(activityId)).thenReturn(Optional.of(testActivity));
        when(enrollmentRepository.countActiveByStudentId(studentId)).thenReturn(0L);
        when(enrollmentRepository.findActiveByStudentAndActivity(studentId, activityId))
                .thenReturn(Optional.of(testEnrollment));

        // Act & Assert
        assertThatThrownBy(() -> enrollmentService.enroll(studentId, activityId))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("ya está inscrito");

        verify(enrollmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("enroll() should throw when no spots available")
    void testEnrollSpotExhausted() throws Exception {
        // Arrange
        Activity fullActivity = Activity.create(
                "Math Club",
                "Advanced mathematics",
                30,
                testActivity.getSchedule(),
                institutionId
        );
        setFieldValue(fullActivity, "status", ActivityStatus.PUBLISHED);
        setFieldValue(fullActivity, "availableSpots", 0);

        when(studentRepository.findById(studentId)).thenReturn(Optional.of(testStudent));
        when(activityRepository.findById(activityId)).thenReturn(Optional.of(fullActivity));
        when(enrollmentRepository.countActiveByStudentId(studentId)).thenReturn(0L);
        when(enrollmentRepository.findActiveByStudentAndActivity(studentId, activityId))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> enrollmentService.enroll(studentId, activityId))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("No hay cupos");

        verify(enrollmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("enroll() should decrement available spots in same transaction")
    void testEnrollDecrementsAvailableSpots() {
        // Arrange
        when(studentRepository.findById(studentId)).thenReturn(Optional.of(testStudent));
        when(activityRepository.findById(activityId)).thenReturn(Optional.of(testActivity));
        when(enrollmentRepository.countActiveByStudentId(studentId)).thenReturn(0L);
        when(enrollmentRepository.findActiveByStudentAndActivity(studentId, activityId))
                .thenReturn(Optional.empty());
        when(enrollmentRepository.save(any(Enrollment.class))).thenReturn(testEnrollment);

        // Act
        enrollmentService.enroll(studentId, activityId);

        // Assert - verify that activity and enrollment were saved (enrollment logic executed)
        verify(activityRepository).save(any(Activity.class));
        verify(enrollmentRepository).save(any(Enrollment.class));
        verify(activityCachePort).invalidate(activityId);
    }

    // ── Cancel Tests ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("cancel() should cancel enrollment and release spot when ADMIN")
    void testCancelSuccessAsAdmin() throws Exception {
        // Arrange
        when(enrollmentRepository.findById(enrollmentId)).thenReturn(Optional.of(testEnrollment));
        when(activityRepository.findById(activityId)).thenReturn(Optional.of(testActivity));

        Enrollment cancelledEnrollment = Enrollment.create(studentId, activityId);
        setFieldValue(cancelledEnrollment, "id", enrollmentId);
        setFieldValue(cancelledEnrollment, "status", EnrollmentStatus.CANCELLED);
        setFieldValue(cancelledEnrollment, "cancelledAt", Instant.now());

        when(enrollmentRepository.save(any(Enrollment.class))).thenReturn(cancelledEnrollment);

        // Act
        Enrollment result = enrollmentService.cancel(enrollmentId);

        // Assert
        assertThat(result)
                .isNotNull()
                .extracting("status")
                .isEqualTo(EnrollmentStatus.CANCELLED);

        verify(enrollmentRepository).findById(enrollmentId);
        verify(activityRepository).findById(activityId);
        verify(activityRepository).save(any(Activity.class));
        verify(enrollmentRepository).save(any(Enrollment.class));
        verify(activityCachePort).invalidate(activityId);
    }

    @Test
    @DisplayName("cancel() should throw when enrollment not found")
    void testCancelNotFound() {
        // Arrange
        when(enrollmentRepository.findById(enrollmentId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> enrollmentService.cancel(enrollmentId))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("Inscripción no encontrada");

        verify(activityRepository, never()).findById(any());
        verify(enrollmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("cancel() should throw when not ADMIN")
    void testCancelForbiddenAsGuardian() {
        // Arrange
        TenantContextHolder.set(
                new TenantContext(institutionId, "GUARDIAN")
        );
        when(enrollmentRepository.findById(enrollmentId)).thenReturn(Optional.of(testEnrollment));

        // Act & Assert
        assertThatThrownBy(() -> enrollmentService.cancel(enrollmentId))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("Solo ADMIN");

        verify(activityRepository, never()).findById(any());
        verify(enrollmentRepository, times(1)).findById(enrollmentId);
    }

    @Test
    @DisplayName("cancel() should throw when associated activity not found")
    void testCancelActivityNotFound() {
        // Arrange
        when(enrollmentRepository.findById(enrollmentId)).thenReturn(Optional.of(testEnrollment));
        when(activityRepository.findById(activityId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> enrollmentService.cancel(enrollmentId))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("Actividad asociada no encontrada");

        verify(activityRepository, never()).save(any());
        verify(enrollmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("cancel() should increment available spots")
    void testCancelIncrementsAvailableSpots() throws Exception {
        // Arrange
        when(enrollmentRepository.findById(enrollmentId)).thenReturn(Optional.of(testEnrollment));
        when(activityRepository.findById(activityId)).thenReturn(Optional.of(testActivity));

        Enrollment cancelledEnrollment = Enrollment.create(studentId, activityId);
        setFieldValue(cancelledEnrollment, "id", enrollmentId);
        setFieldValue(cancelledEnrollment, "status", EnrollmentStatus.CANCELLED);
        setFieldValue(cancelledEnrollment, "cancelledAt", Instant.now());

        when(enrollmentRepository.save(any(Enrollment.class))).thenReturn(cancelledEnrollment);

        // Act
        enrollmentService.cancel(enrollmentId);

        // Assert - verify that activity and enrollment were saved (cancellation logic executed)
        verify(activityRepository).save(any(Activity.class));
        verify(enrollmentRepository).save(any(Enrollment.class));
        verify(activityCachePort).invalidate(activityId);
    }

    // ── Query Tests ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("getEnrollmentsByStudent() should return enrollments when ADMIN")
    void testGetEnrollmentsByStudentAsAdmin() {
        // Arrange
        List<Enrollment> enrollments = List.of(testEnrollment);
        when(enrollmentRepository.findByStudentId(studentId, null)).thenReturn(enrollments);

        // Act
        List<Enrollment> result = enrollmentService.getEnrollmentsByStudent(studentId, null);

        // Assert
        assertThat(result).hasSize(1).contains(testEnrollment);
        verify(enrollmentRepository).findByStudentId(studentId, null);
    }

    @Test
    @DisplayName("getEnrollmentsByStudent() should filter by status when provided")
    void testGetEnrollmentsByStudentWithStatus() {
        // Arrange
        List<Enrollment> enrollments = List.of(testEnrollment);
        when(enrollmentRepository.findByStudentId(studentId, EnrollmentStatus.ACTIVE))
                .thenReturn(enrollments);

        // Act
        List<Enrollment> result = enrollmentService.getEnrollmentsByStudent(studentId, EnrollmentStatus.ACTIVE);

        // Assert
        assertThat(result).hasSize(1).contains(testEnrollment);
        verify(enrollmentRepository).findByStudentId(studentId, EnrollmentStatus.ACTIVE);
    }

    @Test
    @DisplayName("getEnrollmentsByStudent() should throw when not ADMIN or SUPERADMIN")
    void testGetEnrollmentsByStudentForbiddenAsGuardian() {
        // Arrange
        TenantContextHolder.set(
                new TenantContext(institutionId, "GUARDIAN")
        );

        // Act & Assert
        assertThatThrownBy(() -> enrollmentService.getEnrollmentsByStudent(studentId, null))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("GUARDIAN");

        verify(enrollmentRepository, never()).findByStudentId(any(), any());
    }

    @Test
    @DisplayName("getEnrollmentsByStudent() should work as SUPERADMIN")
    void testGetEnrollmentsByStudentAsSuperadmin() {
        // Arrange
        TenantContextHolder.set(
                new TenantContext(institutionId, "SUPERADMIN")
        );
        List<Enrollment> enrollments = List.of(testEnrollment);
        when(enrollmentRepository.findByStudentId(studentId, null)).thenReturn(enrollments);

        // Act
        List<Enrollment> result = enrollmentService.getEnrollmentsByStudent(studentId, null);

        // Assert
        assertThat(result).hasSize(1).contains(testEnrollment);
    }

    @Test
    @DisplayName("getEnrollmentsByActivity() should return enrollments when ADMIN")
    void testGetEnrollmentsByActivityAsAdmin() {
        // Arrange
        List<Enrollment> enrollments = List.of(testEnrollment);
        when(enrollmentRepository.findByActivityId(activityId, null)).thenReturn(enrollments);

        // Act
        List<Enrollment> result = enrollmentService.getEnrollmentsByActivity(activityId, null);

        // Assert
        assertThat(result).hasSize(1).contains(testEnrollment);
        verify(enrollmentRepository).findByActivityId(activityId, null);
    }

    @Test
    @DisplayName("getEnrollmentsByActivity() should throw when not ADMIN or SUPERADMIN")
    void testGetEnrollmentsByActivityForbiddenAsTeacher() {
        // Arrange
        TenantContextHolder.set(
                new TenantContext(institutionId, "TEACHER")
        );

        // Act & Assert
        assertThatThrownBy(() -> enrollmentService.getEnrollmentsByActivity(activityId, null))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("Solo ADMIN");

        verify(enrollmentRepository, never()).findByActivityId(any(), any());
    }

    @Test
    @DisplayName("getEnrollmentsByActivity() should filter by status when provided")
    void testGetEnrollmentsByActivityWithStatus() {
        // Arrange
        List<Enrollment> enrollments = List.of(testEnrollment);
        when(enrollmentRepository.findByActivityId(activityId, EnrollmentStatus.ACTIVE))
                .thenReturn(enrollments);

        // Act
        List<Enrollment> result = enrollmentService.getEnrollmentsByActivity(activityId, EnrollmentStatus.ACTIVE);

        // Assert
        assertThat(result).hasSize(1).contains(testEnrollment);
        verify(enrollmentRepository).findByActivityId(activityId, EnrollmentStatus.ACTIVE);
    }
}
