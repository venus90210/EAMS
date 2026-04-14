-- V14: Fix audit_log column types
-- Cambiar old_value y new_value de JSONB a TEXT para compatibilidad con Hibernate 6.x
-- El contenido sigue siendo JSON válido, solo que almacenado como TEXT

ALTER TABLE audit_log ALTER COLUMN old_value TYPE text USING old_value::text;
ALTER TABLE audit_log ALTER COLUMN new_value TYPE text USING new_value::text;

COMMENT ON COLUMN audit_log.old_value IS 'Estado anterior en JSON — permite reconstruir el estado de cualquier registro en cualquier punto del tiempo.';
COMMENT ON COLUMN audit_log.new_value IS 'Estado nuevo en JSON — permite reconstruir el estado de cualquier registro en cualquier punto del tiempo.';
