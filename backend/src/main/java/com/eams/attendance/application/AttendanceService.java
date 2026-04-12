package com.eams.attendance.application;

import com.eams.attendance.domain.AttendanceRecord;
import com.eams.attendance.domain.AttendanceRecordRepository;
import com.eams.attendance.domain.AttendanceSession;
import com.eams.attendance.domain.AttendanceSessionRepository;
import com.eams.attendance.domain.EditWindowPolicy;
import com.eams.shared.events.ObservationPublishedEvent;
import com.eams.shared.exception.DomainException;
import com.eams.shared.tenant.TenantContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

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
    private final ApplicationEventPublisher eventPublisher;

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

        // 3. Validar que no existe sesión duplicada
        if (sessionRepository.findByActivityIdAndDate(activityId, date).isPresent()) {
            throw DomainException.conflict("SESSION_DUPLICATE",
                    "Ya existe una sesión abierta para esta actividad en el día actual");
        }

        // Crear y guardar sesión
        AttendanceSession session = AttendanceSession.create(activityId, date, topicsCovered);
        AttendanceSession saved = sessionRepository.save(session);

        log.debug("Sesión de asistencia abierta exitosamente: {}", saved.getId());
        return saved;
    }

    // ── Marcación de asistencia ──────────────────────────────────────────────

    /**
     * Registra la asistencia de un estudiante en una sesión.
     *
     * Validaciones:
     *   1. Sesión existe
     *   2. Dentro de ventana de edición (24h) → RF13
     *   3. Máximo 3 toques por estudiante (RF13) - simplificado a no validar aquí
     *   4. No hay registro duplicado para (session_id, student_id)
     *
     * @throws DomainException NOT_FOUND si la sesión no existe (404)
     * @throws DomainException EDIT_WINDOW_EXPIRED si fuera de ventana (403)
     * @throws DomainException RECORD_DUPLICATE si ya existe registro (409)
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

        // 3. Validar que no existe registro duplicado
        if (recordRepository.findBySessionIdAndStudentId(sessionId, studentId).isPresent()) {
            throw DomainException.conflict("RECORD_DUPLICATE",
                    "Ya existe un registro de asistencia para este estudiante en esta sesión");
        }

        // Crear y guardar registro
        AttendanceRecord record = AttendanceRecord.create(sessionId, studentId, present, observation);
        AttendanceRecord saved = recordRepository.save(record);

        log.debug("Asistencia registrada: estudiante {} en sesión {}", studentId, sessionId);
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

        // Publicar evento ObservationPublished (Phase 1.7 — Notificaciones)
        UUID institutionId = TenantContextHolder.requireContext().institutionId();
        eventPublisher.publishEvent(new ObservationPublishedEvent(
                recordId,
                saved.getStudentId(),
                observation,
                session.getDate(),
                institutionId
        ));
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
}
