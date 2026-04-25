package com.eams.activities.infrastructure.http;

import com.eams.activities.application.ActivityService;
import com.eams.activities.application.dto.ActivityResponse;
import com.eams.activities.application.dto.CreateActivityRequest;
import com.eams.activities.application.dto.SpotsResponse;
import com.eams.activities.application.dto.UpdateActivityRequest;
import com.eams.activities.domain.Activity;
import com.eams.activities.domain.ActivityStatus;
import com.eams.shared.tenant.TenantContextHolder;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Adaptador HTTP del módulo Activities (F5-estado-actividad.feature).
 *
 * Endpoints:
 *   GET    /activities                    — listar actividades (filtradas por rol)
 *   POST   /activities                    — crear actividad (DRAFT, solo ADMIN)
 *   GET    /activities/{id}               — obtener detalle
 *   PATCH  /activities/{id}               — actualizar (nombre, descripción, total_spots para ADMIN)
 *   POST   /activities/{id}/publish       — publicar (DRAFT→PUBLISHED, solo ADMIN)
 *   PATCH  /activities/{id}/status        — cambiar estado (PUBLISHED↔DISABLED, solo ADMIN)
 *   GET    /activities/{id}/spots         — consultar cupos (cacheado en Redis)
 */
@RestController
@RequestMapping("/activities")
@RequiredArgsConstructor
public class ActivityController {

    private final ActivityService activityService;

    @GetMapping
    public List<ActivityResponse> listActivities(
            @RequestParam(required = false) ActivityStatus status) {
        UUID institutionId = TenantContextHolder.requireContext().institutionId();
        return activityService.listForRole(institutionId, status)
                .stream()
                .map(ActivityResponse::from)
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ActivityResponse createActivity(
            @Valid @RequestBody CreateActivityRequest request) {
        UUID institutionId = TenantContextHolder.requireContext().institutionId();
        UUID userId = extractUserIdFromSecurityContext();

        Activity activity = Activity.create(
                request.name(),
                request.description(),
                request.totalSpots(),
                request.toSchedule(),
                institutionId,
                userId);

        Activity created = activityService.create(activity);
        return ActivityResponse.from(created);
    }

    /**
     * Extrae el userId del JWT token en el Spring Security context.
     * Temporalmente usa un UUID fijo para pruebas.
     */
    private UUID extractUserIdFromSecurityContext() {
        // TODO: Implementar extracción real del userId del JWT
        // Por ahora, usamos un UUID temporal para las pruebas
        return UUID.fromString("9999aaaa-9999-9999-9999-999999999999");
    }

    @GetMapping("/{activityId}")
    public ActivityResponse getActivity(@PathVariable UUID activityId) {
        Activity activity = activityService.findById(activityId);
        return ActivityResponse.from(activity);
    }

    @PatchMapping("/{activityId}")
    public ActivityResponse updateActivity(
            @PathVariable UUID activityId,
            @RequestBody UpdateActivityRequest request) {
        Activity updated = activityService.update(
                activityId,
                request.name(),
                request.description(),
                request.totalSpots());

        return ActivityResponse.from(updated);
    }

    @PostMapping("/{activityId}/publish")
    public ActivityResponse publishActivity(@PathVariable UUID activityId) {
        Activity published = activityService.publish(activityId);
        return ActivityResponse.from(published);
    }

    @PatchMapping("/{activityId}/status")
    public ActivityResponse updateActivityStatus(
            @PathVariable UUID activityId,
            @Valid @RequestBody UpdateStatusRequest request) {
        Activity updated = activityService.updateStatus(activityId, request.status());
        return ActivityResponse.from(updated);
    }

    @GetMapping("/{activityId}/spots")
    public SpotsResponse getAvailableSpots(@PathVariable UUID activityId) {
        Activity activity = activityService.findById(activityId);
        Integer available = activityService.getAvailableSpots(activityId);

        return new SpotsResponse(
                activityId,
                activity.getTotalSpots(),
                available);
    }

    // ── Helper ───────────────────────────────────────────────────────────────

    public record UpdateStatusRequest(ActivityStatus status) {}
}
