package com.eams.shared.tenant;

import java.util.Optional;

/**
 * Almacena el TenantContext del request actual en un ThreadLocal.
 *
 * Ciclo de vida:
 *   1. El API Gateway inyecta institution_id y role como headers internos.
 *   2. TenantFilter (en shared/config) lee los headers y llama a set().
 *   3. Todos los módulos leen el contexto con get() o requireContext().
 *   4. TenantFilter llama a clear() al finalizar el request.
 *
 * También establece el parámetro de sesión de PostgreSQL usado por RLS (AD-08):
 *   SET LOCAL app.current_institution_id = '<uuid>'
 *   SET LOCAL app.current_role = '<role>'
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
