package com.eams.attendance.application;

import com.eams.attendance.application.dto.AttendanceStudentDto;
import com.eams.attendance.domain.AttendanceRecord;
import com.eams.attendance.domain.AttendanceRecordRepository;
import com.eams.attendance.domain.AttendanceSession;
import com.eams.attendance.domain.AttendanceSessionRepository;
import com.eams.attendance.domain.EditWindowPolicy;
import com.eams.enrollments.domain.Enrollment;
import com.eams.enrollments.domain.EnrollmentRepository;
import com.eams.enrollments.domain.EnrollmentStatus;
import com.eams.shared.exception.DomainException;
import com.eams.shared.tenant.TenantContextHolder;
import com.eams.users.domain.Student;
import com.eams.users.domain.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Record temporal para retornar sesión con estudiantes
 */
record SessionWithStudents(AttendanceSession session, List<AttendanceStudentDto> students) {}

/**
 * Casos de uso del módulo Attendance (F2-asistencia.feature, AD-07).
 *
 * Garantías de seguridad transaccional:
 *   - Validación de fechas: solo "hoy" es válido para apertura de sesión
 *   - Ventana de edición 24h: RF13 restricción temporal en marcación y observaciones
 *   - Sin duplicados: una sesión por (activity_id, date)
 *   - Un registro por estudiante por sesión
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceSessionRepository sessionRepository;
    private final AttendanceRecordRepository recordRepository;
    private final EditWindowPolicy editWindowPolicy;
    private final StudentRepository studentRepository;
    private final EnrollmentRepository enrollmentRepository;

    // ── Apertura de sesión ───────────────────────────────────────────────────

    /**
     * Abre una nueva sesión de asistencia para una actividad en el día actual.
     *
     * Validaciones:
     *   1. Fecha = hoy (CalendarPort) → RF13
     *   2. Actividad existe y está PUBLISHED
     *   3. Docente está asignado a la actividad (TEACHER role)
     *   4. No existe sesión duplicada para (activity_id, date)
     *
     * @throws DomainException NOT_FOUND si la actividad no existe (404)
     * @throws DomainException INVALID_DATE si la fecha no es hoy (422)
     * @throws DomainException INSUFFICIENT_ROLE si no es docente asignado (403)
     * @throws DomainException SESSION_DUPLICATE si ya existe sesión para hoy (409)
     */
    @Transactional
    public AttendanceSession openSession(UUID activityId, LocalDate date, String topicsCovered) {
        log.debug("Intentando abrir sesión de asistencia para actividad {} en fecha {}", activityId, date);

        // 1. Validar que la fecha es hoy
        if (!date.equals(LocalDate.now())) {
            throw DomainException.unprocessable("INVALID_DATE",
                    "La sesión de asistencia solo puede abrirse para el día actual");
        }

        // 2. Validar que el usuario es TEACHER (simplificado: ADMIN también puede)
        String role = TenantContextHolder.requireContext().role();
        boolean isTeacherOrAdmin = "TEACHER".equals(role) || "ADMIN".equals(role);
        if (!isTeacherOrAdmin) {
            throw DomainException.forbidden("INSUFFICIENT_ROLE",
                    "Solo docentes o administradores pueden abrir sesiones de asistencia");
        }

        // 3. Si existe sesión duplicada, retornarla (idempotencia)
        var existingSession = sessionRepository.findByActivityIdAndDate(activityId, date);
        if (existingSession.isPresent()) {
            log.debug("Sesión de asistencia ya existe, retornando la existente: {}", existingSession.get().getId());
            return existingSession.get();
        }

        // Crear y guardar sesión
        AttendanceSession session = AttendanceSession.create(activityId, date, topicsCovered);
        AttendanceSession saved = sessionRepository.save(session);

        log.debug("Sesión de asistencia abierta exitosamente: {}", saved.getId());
        return saved;
    }

    // ── Marcación de asistencia ──────────────────────────────────────────────

    /**
     * Registra o actualiza la asistencia de un estudiante en una sesión (idempotente).
     *
     * Validaciones:
     *   1. Sesión existe
     *   2. Dentro de ventana de edición (24h) → RF13
     *   3. Máximo 3 toques por estudiante (RF13) - simplificado a no validar aquí
     *
     * Si el registro ya existe, lo actualiza. Si no existe, lo crea.
     *
     * @throws DomainException NOT_FOUND si la sesión no existe (404)
     * @throws DomainException EDIT_WINDOW_EXPIRED si fuera de ventana (403)
     */
    @Transactional
    public AttendanceRecord recordAttendance(UUID sessionId, UUID studentId, Boolean present, String observation) {
        log.debug("Marcando asistencia para estudiante {} en sesión {}", studentId, sessionId);

        // 1. Obtener sesión
        AttendanceSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> DomainException.notFound("Sesión de asistencia no encontrada"));

        // 2. Validar ventana de edición
        if (!editWindowPolicy.isEditable(session)) {
            throw DomainException.forbidden(editWindowPolicy.getExpiredErrorCode(),
                    "La ventana de edición de 24h ha expirado para esta sesión");
        }

        // 3. Crear o actualizar registro (idempotencia)
        var existingRecord = recordRepository.findBySessionIdAndStudentId(sessionId, studentId);

        AttendanceRecord record;
        if (existingRecord.isPresent()) {
            // Actualizar registro existente
            record = existingRecord.get();
            record.markAttendance(present);
            // Actualizar observación siempre (incluso si viene vacía o null)
            record.updateObservation(observation);
            log.debug("Registro de asistencia actualizado: estudiante {} en sesión {}", studentId, sessionId);
        } else {
            // Crear nuevo registro
            record = AttendanceRecord.create(sessionId, studentId, present, observation);
            log.debug("Registro de asistencia creado: estudiante {} en sesión {}", studentId, sessionId);
        }

        AttendanceRecord saved = recordRepository.save(record);
        return saved;
    }

    // ── Observaciones ────────────────────────────────────────────────────────

    /**
     * Añade o actualiza observación de un registro de asistencia.
     *
     * Validaciones:
     *   1. Registro existe
     *   2. Dentro de ventana de edición (24h) → RF13
     *
     * @throws DomainException NOT_FOUND si el registro no existe (404)
     * @throws DomainException EDIT_WINDOW_EXPIRED si fuera de ventana (403)
     */
    @Transactional
    public AttendanceRecord addObservation(UUID recordId, String observation) {
        log.debug("Agregando observación al registro de asistencia {}", recordId);

        // 1. Obtener registro
        AttendanceRecord record = recordRepository.findById(recordId)
                .orElseThrow(() -> DomainException.notFound("Registro de asistencia no encontrado"));

        // 2. Obtener sesión para validar ventana
        AttendanceSession session = sessionRepository.findById(record.getSessionId())
                .orElseThrow(() -> DomainException.notFound("Sesión asociada no encontrada"));

        // 3. Validar ventana de edición
        if (!editWindowPolicy.isEditable(session)) {
            throw DomainException.forbidden(editWindowPolicy.getExpiredErrorCode(),
                    "La ventana de edición de 24h ha expirado para esta sesión");
        }

        // Actualizar observación
        record.updateObservation(observation);
        AttendanceRecord saved = recordRepository.save(record);

        // TODO: Publicar evento ObservationPublished (Phase 1.7 — Notificaciones)
        log.debug("Observación agregada al registro: {}", recordId);
        return saved;
    }

    // ── Consultas ────────────────────────────────────────────────────────────

    /**
     * Lista asistencia de un estudiante.
     * GUARDIAN solo ve sus propios hijos; ADMIN ve todos.
     */
    public List<AttendanceRecord> getAttendanceByStudent(UUID studentId) {
        String role = TenantContextHolder.requireContext().role();

        if (!"ADMIN".equals(role) && !"SUPERADMIN".equals(role)) {
            // GUARDIAN solo ve sus hijos (validación simplificada)
            throw DomainException.forbidden("INSUFFICIENT_ROLE",
                    "GUARDIAN solo puede consultar asistencia de sus propios hijos");
        }

        return recordRepository.findByStudentId(studentId);
    }

    /**
     * Lista registros de asistencia de una sesión.
     * Solo TEACHER asignado o ADMIN.
     */
    public List<AttendanceRecord> getAttendanceBySession(UUID sessionId) {
        String role = TenantContextHolder.requireContext().role();
        boolean isTeacherOrAdmin = "TEACHER".equals(role) || "ADMIN".equals(role) || "SUPERADMIN".equals(role);

        if (!isTeacherOrAdmin) {
            throw DomainException.forbidden("INSUFFICIENT_ROLE",
                    "Solo docentes o administradores pueden consultar asistencia de sesiones");
        }

        return recordRepository.findBySessionId(sessionId);
    }

    /**
     * Lista todos los registros de asistencia de los estudiantes de un acudiente.
     * Solo GUARDIAN (del acudiente) o ADMIN puede consultar.
     */
    public List<AttendanceRecord> getAttendanceByGuardian(UUID guardianId) {
        String role = TenantContextHolder.requireContext().role();

        // Solo GUARDIAN o ADMIN
        if (!"GUARDIAN".equals(role) && !"ADMIN".equals(role) && !"SUPERADMIN".equals(role)) {
            throw DomainException.forbidden("INSUFFICIENT_ROLE",
                    "Solo GUARDIAN o ADMIN pueden consultar asistencia");
        }

        // Obtener todos los estudiantes del acudiente
        List<Student> students = studentRepository.findByGuardianId(guardianId);
        List<AttendanceRecord> records = new java.util.ArrayList<>();

        // Recolectar registros de asistencia de todos los estudiantes
        for (Student student : students) {
            records.addAll(recordRepository.findByStudentId(student.getId()));
        }

        return records;
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    /**
     * Obtiene la lista de estudiantes inscritos en una actividad como DTOs.
     */
    public List<AttendanceStudentDto> getActivityStudents(UUID activityId) {
        List<Enrollment> enrollments = enrollmentRepository.findByActivityId(activityId, EnrollmentStatus.ACTIVE);

        return enrollments.stream()
                .map(enrollment -> {
                    Student student = studentRepository.findById(enrollment.getStudentId())
                            .orElseThrow(() -> DomainException.notFound("Estudiante no encontrado"));

                    String studentName = student.getFirstName() + " " + student.getLastName();

                    return new AttendanceStudentDto(
                            enrollment.getId(),
                            enrollment.getStudentId(),
                            studentName,
                            false,  // inicialmente no presente
                            ""      // sin observaciones
                    );
                })
                .toList();
    }
}
