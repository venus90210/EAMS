-- V7: Configuración inicial de RLS y función auxiliar de tenant
-- Fuente: AD-08 (multi-tenancy con Row-Level Security)
-- Esta migración configura la función helper que el backend llama al inicio de cada sesión

-- Función que el backend invoca al inicio de cada request para establecer el contexto de tenant
-- Uso desde Spring: entityManager.createNativeQuery("SELECT set_tenant_context(:institutionId, :role)").setParameter(...)
CREATE OR REPLACE FUNCTION set_tenant_context(
    p_institution_id UUID,
    p_role           TEXT
)
RETURNS VOID AS $$
BEGIN
    -- Establece el institution_id para que las políticas RLS lo lean con current_setting()
    PERFORM set_config('app.current_institution_id', p_institution_id::TEXT, true);
    PERFORM set_config('app.current_role', p_role, true);
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

COMMENT ON FUNCTION set_tenant_context IS
    'Establece el contexto de tenant para la sesión actual. '
    'Debe invocarse al inicio de cada request desde el TenantContextHolder de Spring. '
    'El parámetro true en set_config hace que el valor sea local a la transacción.';

-- Función para pruebas de integración (IT-02): verifica que RLS aísla correctamente
CREATE OR REPLACE FUNCTION count_cross_tenant_leaks(
    p_institution_id UUID
)
RETURNS INTEGER AS $$
DECLARE
    leak_count INTEGER;
BEGIN
    -- Intenta leer registros de otra institución — con RLS activo debe devolver 0
    SELECT COUNT(*) INTO leak_count
    FROM activities
    WHERE institution_id != p_institution_id;
    RETURN leak_count;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION count_cross_tenant_leaks IS
    'Función de diagnóstico para IT-02: verifica que RLS impide leer datos de otra institución. '
    'Debe retornar siempre 0 cuando el tenant context está configurado.';
