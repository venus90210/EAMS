package com.eams.institutions.application;

import com.eams.institutions.domain.Institution;
import com.eams.institutions.domain.InstitutionRepository;
import com.eams.shared.exception.DomainException;
import com.eams.shared.tenant.TenantContext;
import com.eams.shared.tenant.TenantContextHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Resuelve y valida la institución activa del tenant actual (AD-08).
 *
 * Combina el contexto del request ({@link TenantContextHolder}) con la
 * persistencia para garantizar que la institución del token existe y es válida
 * antes de procesar cualquier operación de dominio.
 */
@Component
@RequiredArgsConstructor
public class InstitutionContextProvider {

    private final InstitutionRepository institutionRepository;

    /**
     * Retorna la institución actual del contexto de tenant.
     *
     * @throws DomainException FORBIDDEN si no hay contexto de tenant activo
     * @throws DomainException NOT_FOUND si el institution_id del token no existe en BD
     */
    public Institution requireCurrentInstitution() {
        UUID institutionId = TenantContextHolder.get()
                .map(TenantContext::institutionId)
                .orElseThrow(() -> DomainException.forbidden("FORBIDDEN",
                        "No hay contexto de tenant en el request actual"));

        return institutionRepository.findById(institutionId)
                .orElseThrow(() -> DomainException.notFound(
                        "Institución no encontrada para el tenant actual: " + institutionId));
    }

    /**
     * Verifica que el institution_id del contexto actual coincide con el id solicitado.
     * Permite que SUPERADMIN acceda a cualquier institución.
     *
     * @throws DomainException INSTITUTION_MISMATCH si la institución no coincide (F4 — escenario RBAC)
     */
    public void assertAccessTo(UUID targetInstitutionId) {
        var ctx = TenantContextHolder.get()
                .orElseThrow(() -> DomainException.forbidden("FORBIDDEN",
                        "No hay contexto de tenant en el request actual"));

        if (ctx.isSuperAdmin()) return;

        if (!targetInstitutionId.equals(ctx.institutionId())) {
            throw DomainException.forbidden("INSTITUTION_MISMATCH",
                    "No tienes acceso a la institución solicitada");
        }
    }
}
