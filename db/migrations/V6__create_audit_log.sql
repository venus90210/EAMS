-- V6: Audit log
-- Fuente: AD-07 (cambios en total_spots), AD-08, RNF06 (trazabilidad Ley 1581)

CREATE TABLE audit_log (
    id             UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    table_name     VARCHAR(100) NOT NULL,
    record_id      UUID         NOT NULL,
    action         VARCHAR(20)  NOT NULL CHECK (action IN ('INSERT', 'UPDATE', 'DELETE')),
    old_value      JSONB,
    new_value      JSONB,
    performed_by   UUID         REFERENCES users(id),
    institution_id UUID         REFERENCES institutions(id),
    performed_at   TIMESTAMPTZ  NOT NULL DEFAULT now()
);

COMMENT ON TABLE audit_log IS 'Registro de auditoría para cambios críticos: total_spots, estados de actividad, datos de menores (RNF06, Ley 1581).';
COMMENT ON COLUMN audit_log.old_value IS 'Estado anterior en JSONB — permite reconstruir el estado de cualquier registro en cualquier punto del tiempo.';

-- ── Índices ────────────────────────────────────────────────────────────────
CREATE INDEX idx_audit_log_table_record  ON audit_log(table_name, record_id);
CREATE INDEX idx_audit_log_performed_at  ON audit_log(performed_at DESC);
CREATE INDEX idx_audit_log_institution   ON audit_log(institution_id);
