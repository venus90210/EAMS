package com.eams.shared.tenant;

import java.util.UUID;

/**
 * Contenedor inmutable del contexto de tenant para el request actual.
 * Propagado por TenantContextHolder en cada petición autenticada.
 *
 * @param institutionId ID de la institución extraído del JWT. Null para SUPERADMIN.
 * @param role          Rol del usuario autenticado.
 */
public record TenantContext(UUID institutionId, String role) {

    public boolean isSuperAdmin() {
        return "SUPERADMIN".equals(role);
    }

    public boolean hasInstitution() {
        return institutionId != null;
    }
}
