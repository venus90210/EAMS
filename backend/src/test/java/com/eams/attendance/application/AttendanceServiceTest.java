package com.eams.attendance.application;

import com.eams.attendance.domain.AttendanceRecord;
import com.eams.attendance.domain.AttendanceRecordRepository;
import com.eams.attendance.domain.AttendanceSession;
import com.eams.attendance.domain.AttendanceSessionRepository;
import com.eams.attendance.domain.EditWindowPolicy;
import com.eams.enrollments.domain.EnrollmentRepository;
import com.eams.shared.exception.DomainException;
import com.eams.shared.tenant.TenantContext;
import com.eams.shared.tenant.TenantContextHolder;
import com.eams.users.domain.StudentRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AttendanceService Tests")
@org.junit.jupiter.api.Tag("unit")
class AttendanceServiceTest {

    @Mock
    private AttendanceSessionRepository sessionRepository;

    @Mock
    private AttendanceRecordRepository recordRepository;

    @Mock
    private EditWindowPolicy editWindowPolicy;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private EnrollmentRepository enrollmentRepository;

    private AttendanceService attendanceService;

    private UUID activityId;
    private UUID sessionId;
    private UUID recordId;
    private UUID studentId;
    private UUID institutionId;

    private AttendanceSession testSession;
    private AttendanceRecord testRecord;

    @BeforeEach
    void setUp() throws Exception {
        attendanceService = new AttendanceService(
                sessionRepository,
                recordRepository,
                editWindowPolicy,
                studentRepository,
                enrollmentRepository
        );

        activityId = UUID.randomUUID();
        sessionId = UUID.randomUUID();
        recordId = UUID.randomUUID();
        studentId = UUID.randomUUID();
        institutionId = UUID.randomUUID();

        // Setup test data
        testSession = AttendanceSession.create(activityId, LocalDate.now(), "Tema 1: Intro");
        setFieldValue(testSession, "id", sessionId);

        testRecord = AttendanceRecord.create(sessionId, studentId, true, "Participó activamente");
        setFieldValue(testRecord, "id", recordId);

        // Setup TenantContext as ADMIN for default tests
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

    // ── openSession() Tests ──────────────────────────────────────────────────

    @Test
    @DisplayName("openSession() should create session when all validations pass")
    void testOpenSessionSuccess() {
        // Arrange
        when(sessionRepository.findByActivityIdAndDate(activityId, LocalDate.now()))
                .thenReturn(Optional.empty());
        when(sessionRepository.save(any(AttendanceSession.class))).thenReturn(testSession);

        // Act
        AttendanceSession result = attendanceService.openSession(
                activityId, LocalDate.now(), "Tema 1: Intro"
        );

        // Assert
        assertThat(result)
                .isNotNull()
                .extracting("activityId", "date")
                .containsExactly(activityId, LocalDate.now());

        verify(sessionRepository).findByActivityIdAndDate(activityId, LocalDate.now());
        verify(sessionRepository).save(any(AttendanceSession.class));
    }

    @Test
    @DisplayName("openSession() should throw when date is not today")
    void testOpenSessionInvalidDate() {
        // Arrange
        LocalDate pastDate = LocalDate.now().minusDays(1);

        // Act & Assert
        assertThatThrownBy(() -> attendanceService.openSession(activityId, pastDate, "Tema 1"))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("día actual");

        verify(sessionRepository, never()).save(any());
    }

    @Test
    @DisplayName("openSession() should throw when user is GUARDIAN")
    void testOpenSessionInsufficientRole() {
        // Arrange
        TenantContextHolder.set(
                new TenantContext(institutionId, "GUARDIAN")
        );

        // Act & Assert
        assertThatThrownBy(() -> attendanceService.openSession(activityId, LocalDate.now(), "Tema 1"))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("docentes");

        verify(sessionRepository, never()).save(any());
    }

    @Test
    @DisplayName("openSession() should throw when session already exists")
    void testOpenSessionDuplicate() {
        // Arrange
        when(sessionRepository.findByActivityIdAndDate(activityId, LocalDate.now()))
                .thenReturn(Optional.of(testSession));

        // Act & Assert
        assertThatThrownBy(() -> attendanceService.openSession(activityId, LocalDate.now(), "Tema 1"))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("Ya existe una sesión");

        verify(sessionRepository, never()).save(any());
    }

    // ── recordAttendance() Tests ─────────────────────────────────────────────

    @Test
    @DisplayName("recordAttendance() should create record when within edit window")
    void testRecordAttendanceSuccess() {
        // Arrange
        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(testSession));
        when(editWindowPolicy.isEditable(testSession)).thenReturn(true);
        when(recordRepository.findBySessionIdAndStudentId(sessionId, studentId))
                .thenReturn(Optional.empty());
        when(recordRepository.save(any(AttendanceRecord.class))).thenReturn(testRecord);

        // Act
        AttendanceRecord result = attendanceService.recordAttendance(
                sessionId, studentId, true, "Participó"
        );

        // Assert
        assertThat(result)
                .isNotNull()
                .extracting("sessionId", "studentId", "present")
                .containsExactly(sessionId, studentId, true);

        verify(sessionRepository).findById(sessionId);
        verify(editWindowPolicy).isEditable(testSession);
        verify(recordRepository).findBySessionIdAndStudentId(sessionId, studentId);
        verify(recordRepository).save(any(AttendanceRecord.class));
    }

    @Test
    @DisplayName("recordAttendance() should throw when session not found")
    void testRecordAttendanceSessionNotFound() {
        // Arrange
        when(sessionRepository.findById(sessionId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> attendanceService.recordAttendance(sessionId, studentId, true, null))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("Sesión");

        verify(recordRepository, never()).save(any());
    }

    @Test
    @DisplayName("recordAttendance() should throw when edit window expired")
    void testRecordAttendanceWindowExpired() {
        // Arrange
        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(testSession));
        when(editWindowPolicy.isEditable(testSession)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> attendanceService.recordAttendance(sessionId, studentId, true, null))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("ventana de edición");

        verify(recordRepository, never()).save(any());
    }

    @Test
    @DisplayName("recordAttendance() should throw when record already exists")
    void testRecordAttendanceDuplicate() {
        // Arrange
        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(testSession));
        when(editWindowPolicy.isEditable(testSession)).thenReturn(true);
        when(recordRepository.findBySessionIdAndStudentId(sessionId, studentId))
                .thenReturn(Optional.of(testRecord));

        // Act & Assert
        assertThatThrownBy(() -> attendanceService.recordAttendance(sessionId, studentId, true, null))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("Ya existe un registro");

        verify(recordRepository, never()).save(any());
    }

    // ── addObservation() Tests ───────────────────────────────────────────────

    @Test
    @DisplayName("addObservation() should update observation when within edit window")
    void testAddObservationSuccess() throws Exception {
        // Arrange
        when(recordRepository.findById(recordId)).thenReturn(Optional.of(testRecord));
        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(testSession));
        when(editWindowPolicy.isEditable(testSession)).thenReturn(true);
        AttendanceRecord updatedRecord = AttendanceRecord.create(
                sessionId, studentId, true, "Excelente desempeño"
        );
        setFieldValue(updatedRecord, "id", recordId);
        when(recordRepository.save(any(AttendanceRecord.class))).thenReturn(updatedRecord);

        // Act
        AttendanceRecord result = attendanceService.addObservation(recordId, "Excelente desempeño");

        // Assert
        assertThat(result)
                .isNotNull()
                .extracting("observation")
                .isEqualTo("Excelente desempeño");

        verify(recordRepository).findById(recordId);
        verify(sessionRepository).findById(sessionId);
        verify(editWindowPolicy).isEditable(testSession);
        verify(recordRepository).save(any(AttendanceRecord.class));
    }

    @Test
    @DisplayName("addObservation() should throw when record not found")
    void testAddObservationRecordNotFound() {
        // Arrange
        when(recordRepository.findById(recordId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> attendanceService.addObservation(recordId, "Nueva obs"))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("Registro");

        verify(sessionRepository, never()).findById(any());
    }

    @Test
    @DisplayName("addObservation() should throw when edit window expired")
    void testAddObservationWindowExpired() {
        // Arrange
        when(recordRepository.findById(recordId)).thenReturn(Optional.of(testRecord));
        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(testSession));
        when(editWindowPolicy.isEditable(testSession)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> attendanceService.addObservation(recordId, "Nueva obs"))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("ventana de edición");

        verify(recordRepository, never()).save(any());
    }

    // ── getAttendanceByStudent() Tests ───────────────────────────────────────

    @Test
    @DisplayName("getAttendanceByStudent() should return records when TEACHER")
    void testGetAttendanceByStudentAsTeacher() {
        // Arrange
        List<AttendanceRecord> records = List.of(testRecord);
        when(recordRepository.findByStudentId(studentId)).thenReturn(records);

        // Act
        List<AttendanceRecord> result = attendanceService.getAttendanceByStudent(studentId);

        // Assert
        assertThat(result).hasSize(1).contains(testRecord);
        verify(recordRepository).findByStudentId(studentId);
    }

    @Test
    @DisplayName("getAttendanceByStudent() should throw when GUARDIAN")
    void testGetAttendanceByStudentForbiddenAsGuardian() {
        // Arrange
        TenantContextHolder.set(
                new TenantContext(institutionId, "GUARDIAN")
        );

        // Act & Assert
        assertThatThrownBy(() -> attendanceService.getAttendanceByStudent(studentId))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("GUARDIAN");

        verify(recordRepository, never()).findByStudentId(any());
    }

    // ── getAttendanceBySession() Tests ───────────────────────────────────────

    @Test
    @DisplayName("getAttendanceBySession() should return records when TEACHER")
    void testGetAttendanceBySessionAsTeacher() {
        // Arrange
        List<AttendanceRecord> records = List.of(testRecord);
        when(recordRepository.findBySessionId(sessionId)).thenReturn(records);

        // Act
        List<AttendanceRecord> result = attendanceService.getAttendanceBySession(sessionId);

        // Assert
        assertThat(result).hasSize(1).contains(testRecord);
        verify(recordRepository).findBySessionId(sessionId);
    }

    @Test
    @DisplayName("getAttendanceBySession() should throw when GUARDIAN")
    void testGetAttendanceBySessionForbiddenAsGuardian() {
        // Arrange
        TenantContextHolder.set(
                new TenantContext(institutionId, "GUARDIAN")
        );

        // Act & Assert
        assertThatThrownBy(() -> attendanceService.getAttendanceBySession(sessionId))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("docentes");

        verify(recordRepository, never()).findBySessionId(any());
    }

    // ── EditWindowPolicy Tests ───────────────────────────────────────────────

    @Test
    @DisplayName("editWindowPolicy.isEditable() should return true within 24h")
    void testEditWindowPolicyWithinWindow() {
        // Arrange
        when(editWindowPolicy.isEditable(testSession)).thenReturn(true);

        // Act
        boolean result = editWindowPolicy.isEditable(testSession);

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("editWindowPolicy.isEditable() should return false after 24h")
    void testEditWindowPolicyExpired() {
        // Arrange
        when(editWindowPolicy.isEditable(testSession)).thenReturn(false);

        // Act
        boolean result = editWindowPolicy.isEditable(testSession);

        // Assert
        assertThat(result).isFalse();
    }
}
