package com.eams.shared.tenant;

import java.util.Optional;

/**
 * Almacena el TenantContext del request actual en un ThreadLocal (AD-08).
 *
 * Ciclo de vida:
 *   1. El API Gateway inyecta institution_id y role como headers internos DESPUÉS de validar JWT.
 *   2. TenantFilter (en shared/config) lee los headers y llama a set().
 *   3. Todos los módulos leen el contexto con get() o requireContext().
 *   4. TenantFilter llama a clear() al finalizar el request.
 *
 * También establece el parámetro de sesión de PostgreSQL usado por RLS:
 *   SET LOCAL app.current_institution_id = '<uuid>'
 *   SET LOCAL app.current_role = '<role>'
 *
 * ⚠️ PRECONDICIÓN: El API Gateway DEBE validar JWT y establecer TenantContext ANTES de
 *    que cualquier controlador lo intente leer. Si TenantContext no está presente:
 *
 *    - requireContext() lanza IllegalStateException (error de configuración, no de acceso)
 *    - get() retorna Optional.empty() (el código debe usar orElseThrow con DomainException.forbidden())
 *
 * SEGURIDAD (OWASP A02:2021 — Broken Authentication):
 *   - institutionId: nullable para SUPERADMIN (sin restricción de tenant)
 *   - role: siempre presente (GUARDIAN|TEACHER|ADMIN|SUPERADMIN)
 *   - no almacenar tokens, secrets, o datos sensibles en este contexto
 */
public final class TenantContextHolder {

    private static final ThreadLocal<TenantContext> CONTEXT = new ThreadLocal<>();

    private TenantContextHolder() {}

    public static void set(TenantContext context) {
        CONTEXT.set(context);
    }

    public static Optional<TenantContext> get() {
        return Optional.ofNullable(CONTEXT.get());
    }

    /**
     * Retorna el contexto o lanza IllegalStateException si no está presente.
     * Usado en módulos de dominio donde el contexto es obligatorio.
     */
    public static TenantContext requireContext() {
        return get().orElseThrow(() ->
                new IllegalStateException("TenantContext no disponible en el request actual"));
    }

    public static void clear() {
        CONTEXT.remove();
    }
}
