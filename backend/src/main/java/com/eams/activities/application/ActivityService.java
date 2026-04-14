package com.eams.activities.application;

import com.eams.activities.domain.Activity;
import com.eams.activities.domain.ActivityCachePort;
import com.eams.activities.domain.ActivityRepository;
import com.eams.activities.domain.ActivityStatus;
import com.eams.shared.audit.AuditLogService;
import com.eams.shared.exception.DomainException;
import com.eams.shared.tenant.TenantContextHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Casos de uso del módulo Activities (F5-estado-actividad.feature).
 *
 * Reglas de negocio:
 *   - ADMIN puede crear (DRAFT) y publicar (DRAFT→PUBLISHED)
 *   - ADMIN puede cambiar estado (PUBLISHED↔DISABLED)
 *   - Solo ADMIN puede modificar total_spots (genera audit log — Fase 1.4 pendiente)
 *   - GUARDIAN ve solo PUBLISHED de su institución
 *   - available_spots se cachea en Redis con TTL 30s
 */
@Service
@RequiredArgsConstructor
public class ActivityService {

    private final ActivityRepository activityRepository;
    private final ActivityCachePort cachePort;
    private final AuditLogService auditLogService;

    // ── Crear ────────────────────────────────────────────────────────────────

    /**
     * Crea una actividad en estado DRAFT.
     *
     * @throws DomainException INSUFFICIENT_ROLE si no es ADMIN (403)
     */
    public Activity create(Activity activity) {
        String role = TenantContextHolder.requireContext().role();
        if (!"ADMIN".equals(role) && !"SUPERADMIN".equals(role)) {
            throw DomainException.forbidden("INSUFFICIENT_ROLE",
                    "Solo ADMIN puede crear actividades");
        }

        return activityRepository.save(activity);
    }

    // ── Obtener ──────────────────────────────────────────────────────────────

    public Activity findById(UUID activityId) {
        return activityRepository.findById(activityId)
                .orElseThrow(() -> DomainException.notFound(
                        "Actividad no encontrada: " + activityId));
    }

    /**
     * Lista actividades según rol del solicitante.
     *
     * - GUARDIAN: solo PUBLISHED de su institución
     * - TEACHER/ADMIN: todas de su institución
     * - SUPERADMIN: todas
     */
    public List<Activity> listForRole(UUID institutionId, ActivityStatus statusFilter) {
        var ctx = TenantContextHolder.get();
        if (ctx.isEmpty()) {
            throw DomainException.forbidden("FORBIDDEN",
                    "Se requiere autenticación para listar actividades");
        }

        String role = ctx.get().role();
        UUID contextInstitution = ctx.get().institutionId();

        // SUPERADMIN puede ver de cualquier institución
        if ("SUPERADMIN".equals(role)) {
            return activityRepository.findByInstitutionId(institutionId, statusFilter);
        }

        // Otros roles solo ven su institución
        if (contextInstitution != null && !contextInstitution.equals(institutionId)) {
            throw DomainException.forbidden("INSTITUTION_MISMATCH",
                    "No tienes acceso a actividades de otra institución");
        }

        // GUARDIAN ve solo PUBLISHED
        if ("GUARDIAN".equals(role)) {
            return activityRepository.findByInstitutionId(institutionId, ActivityStatus.PUBLISHED);
        }

        // TEACHER/ADMIN ven todas (sin filtro si statusFilter es null)
        if (statusFilter == null) {
            return activityRepository.findByInstitutionId(institutionId);
        }
        return activityRepository.findByInstitutionId(institutionId, statusFilter);
    }

    // ── Actualizar ───────────────────────────────────────────────────────────

    /**
     * Actualiza nombre, descripción y horario.
     * Solo ADMIN puede cambiar total_spots (genera audit log).
     *
     * @throws DomainException INSUFFICIENT_ROLE si intenta cambiar total_spots siendo TEACHER (403)
     */
    public Activity update(UUID activityId, String name, String description, Integer totalSpots) {
        Activity activity = findById(activityId);

        String role = TenantContextHolder.requireContext().role();
        boolean isAdmin = "ADMIN".equals(role) || "SUPERADMIN".equals(role);

        // Verificar institución
        UUID institutionId = TenantContextHolder.requireContext().institutionId();
        if (!activity.getInstitutionId().equals(institutionId)) {
            throw DomainException.forbidden("INSTITUTION_MISMATCH",
                    "No tienes acceso a modificar esta actividad");
        }

        // Solo ADMIN puede cambiar total_spots
        if (totalSpots != null && !isAdmin) {
            throw DomainException.forbidden("INSUFFICIENT_ROLE",
                    "Solo ADMIN puede modificar total_spots");
        }

        // Actualizar campos
        activity.updateBasicInfo(name, description);
        if (totalSpots != null) {
            int oldTotalSpots = activity.getTotalSpots();
            activity.updateTotalSpots(totalSpots);
            // Registrar auditoría de cambio en cupos
            auditLogService.log(
                    "activities",
                    activityId,
                    "UPDATE",
                    Map.of("totalSpots", oldTotalSpots),
                    Map.of("totalSpots", totalSpots),
                    institutionId
            );
            cachePort.invalidate(activityId);  // Invalidar caché al cambiar cupos
        }

        return activityRepository.save(activity);
    }

    // ── Publicar ─────────────────────────────────────────────────────────────

    /**
     * Transiciona DRAFT → PUBLISHED.
     *
     * @throws DomainException INSUFFICIENT_ROLE si no es ADMIN (403)
     * @throws DomainException INVALID_STATUS_TRANSITION si no está en DRAFT (409)
     */
    public Activity publish(UUID activityId) {
        Activity activity = findById(activityId);

        String role = TenantContextHolder.requireContext().role();
        if (!"ADMIN".equals(role) && !"SUPERADMIN".equals(role)) {
            throw DomainException.forbidden("INSUFFICIENT_ROLE",
                    "Solo ADMIN puede publicar actividades");
        }

        ActivityStatus oldStatus = activity.getStatus();
        try {
            activity.transitionTo(ActivityStatus.PUBLISHED);
        } catch (IllegalArgumentException e) {
            throw DomainException.conflict("INVALID_STATUS_TRANSITION", e.getMessage());
        }

        // Registrar auditoría de cambio de estado
        UUID institutionId = TenantContextHolder.requireContext().institutionId();
        auditLogService.log(
                "activities",
                activityId,
                "UPDATE",
                Map.of("status", oldStatus.name()),
                Map.of("status", activity.getStatus().name()),
                institutionId
        );

        cachePort.invalidate(activityId);  // Caché es irrelevante en DRAFT, pero invalidamos por seguridad
        return activityRepository.save(activity);
    }

    // ── Cambiar estado ───────────────────────────────────────────────────────

    /**
     * Cambia estado (PUBLISHED ↔ DISABLED).
     * Invalida caché y publica evento ActivityStatusChanged.
     *
     * @throws DomainException INSUFFICIENT_ROLE si no es ADMIN (403)
     * @throws DomainException INVALID_STATUS_TRANSITION si la transición no es válida (409)
     */
    public Activity updateStatus(UUID activityId, ActivityStatus newStatus) {
        Activity activity = findById(activityId);

        String role = TenantContextHolder.requireContext().role();
        if (!"ADMIN".equals(role) && !"SUPERADMIN".equals(role)) {
            throw DomainException.forbidden("INSUFFICIENT_ROLE",
                    "Solo ADMIN puede cambiar estado de actividades");
        }

        ActivityStatus oldStatus = activity.getStatus();
        try {
            activity.transitionTo(newStatus);
        } catch (IllegalArgumentException e) {
            throw DomainException.conflict("INVALID_STATUS_TRANSITION", e.getMessage());
        }

        // Registrar auditoría de cambio de estado
        UUID institutionId = TenantContextHolder.requireContext().institutionId();
        auditLogService.log(
                "activities",
                activityId,
                "UPDATE",
                Map.of("status", oldStatus.name()),
                Map.of("status", newStatus.name()),
                institutionId
        );

        cachePort.invalidate(activityId);  // Invalidar caché tras cambio de estado
        Activity saved = activityRepository.save(activity);

        // TODO: Publicar evento ActivityStatusChanged para notificaciones (Fase 1.7)

        return saved;
    }

    // ── Cupos ────────────────────────────────────────────────────────────────

    /**
     * Retorna cupos disponibles, leyendo desde caché primero.
     * Si caché vacío, obtiene de BD y cachea con TTL 30s.
     *
     * Garantía RF04: respuesta en <1 segundo.
     */
    public Integer getAvailableSpots(UUID activityId) {
        // Intentar leer desde caché primero
        var cached = cachePort.getAvailableSpots(activityId);
        if (cached.isPresent()) {
            return cached.get();
        }

        // No está en caché — leer de BD y cachear
        Activity activity = findById(activityId);
        Integer spots = activity.getAvailableSpots();
        cachePort.setAvailableSpots(activityId, spots);

        return spots;
    }
}
