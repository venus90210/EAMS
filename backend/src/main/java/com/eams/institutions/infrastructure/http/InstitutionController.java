package com.eams.institutions.infrastructure.http;

import com.eams.institutions.application.InstitutionService;
import com.eams.institutions.application.dto.CreateInstitutionRequest;
import com.eams.institutions.application.dto.InstitutionResponse;
import com.eams.institutions.application.dto.UpdateInstitutionRequest;
import com.eams.shared.exception.DomainException;
import com.eams.shared.tenant.TenantContextHolder;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Adaptador HTTP del módulo Instituciones (AD-03).
 *
 * Endpoints:
 *   POST   /institutions          — crear (solo SUPERADMIN)
 *   GET    /institutions          — listar todas (solo SUPERADMIN)
 *   GET    /institutions/{id}     — obtener por id (cualquier rol)
 *   PATCH  /institutions/{id}     — actualizar (solo SUPERADMIN)
 *
 * La validación JWT viene del API Gateway (AD-04).
 * Este controlador verifica el rol SUPERADMIN desde TenantContextHolder.
 */
@RestController
@RequestMapping("/institutions")
@RequiredArgsConstructor
public class InstitutionController {

    private final InstitutionService institutionService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public InstitutionResponse create(@Valid @RequestBody CreateInstitutionRequest request) {
        requireSuperAdmin();
        return institutionService.create(request);
    }

    @GetMapping
    public List<InstitutionResponse> findAll() {
        requireSuperAdmin();
        return institutionService.findAll();
    }

    @GetMapping("/{id}")
    public InstitutionResponse findById(@PathVariable UUID id) {
        return institutionService.findById(id);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<InstitutionResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateInstitutionRequest request) {
        requireSuperAdmin();
        return ResponseEntity.ok(institutionService.update(id, request));
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private void requireSuperAdmin() {
        var ctx = TenantContextHolder.requireContext();
        if (!ctx.isSuperAdmin()) {
            throw DomainException.forbidden("INSUFFICIENT_ROLE",
                    "Solo SUPERADMIN puede gestionar instituciones");
        }
    }
}
