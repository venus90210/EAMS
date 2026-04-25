package com.eams.shared.tenant;

import jakarta.persistence.EntityManager;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Filtro que extrae los headers internos inyectados por el API Gateway
 * y establece el TenantContext para el request actual.
 *
 * Headers esperados (inyectados por NestJS Gateway tras validar JWT):
 *   X-Institution-Id : UUID de la institución
 *   X-User-Role      : Rol del usuario (GUARDIAN, TEACHER, ADMIN, SUPERADMIN)
 *
 * También establece los parámetros de sesión de PostgreSQL para RLS (AD-08).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TenantFilter extends OncePerRequestFilter {

    static final String HEADER_INSTITUTION_ID = "X-Institution-Id";
    static final String HEADER_USER_ROLE      = "X-User-Role";

    private final EntityManager entityManager;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String institutionIdHeader = request.getHeader(HEADER_INSTITUTION_ID);
        String role = request.getHeader(HEADER_USER_ROLE);

        try {
            if (role != null) {
                UUID institutionId = institutionIdHeader != null
                        ? UUID.fromString(institutionIdHeader)
                        : null;

                TenantContext context = new TenantContext(institutionId, role);
                TenantContextHolder.set(context);

                // Establece parámetros de sesión para las políticas RLS de PostgreSQL (AD-08)
                setPostgresSessionParams(institutionId, role);
            }

            filterChain.doFilter(request, response);

        } finally {
            TenantContextHolder.clear();
        }
    }

    private void setPostgresSessionParams(UUID institutionId, String role) {
        try {
            String id = institutionId != null ? institutionId.toString() : "";
            entityManager.createNativeQuery(
                    "SELECT set_tenant_context(CAST(:institutionId AS UUID), :role)"
            )
            .setParameter("institutionId", institutionId != null ? institutionId.toString() : null)
            .setParameter("role", role)
            .getSingleResult();
        } catch (Exception e) {
            log.warn("No se pudo establecer el contexto de tenant en PostgreSQL: {}", e.getMessage());
        }
    }
}
