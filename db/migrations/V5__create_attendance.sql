-- V5: Asistencia — sesiones y registros individuales
-- Fuente: RF11, RF12, RF13, AD-08 (RLS)

CREATE TABLE attendance_sessions (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    activity_id     UUID        NOT NULL REFERENCES activities(id),
    date            DATE        NOT NULL,
    topics_covered  TEXT,
    recorded_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    -- Ventana de edición: 24h desde recorded_at (regla de negocio en Módulo Asistencia)

    CONSTRAINT uq_attendance_sessions_activity_date
        UNIQUE (activity_id, date)
);

COMMENT ON TABLE attendance_sessions IS 'Una sesión por actividad por fecha. La ventana de edición de 24h se calcula en la capa de dominio.';

CREATE TABLE attendance_records (
    id           UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    session_id   UUID        NOT NULL REFERENCES attendance_sessions(id),
    student_id   UUID        NOT NULL REFERENCES students(id),
    present      BOOLEAN     NOT NULL DEFAULT false,
    observation  TEXT,
    recorded_at  TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT uq_attendance_records_session_student
        UNIQUE (session_id, student_id)
);

COMMENT ON TABLE attendance_records IS 'Registro individual de asistencia por estudiante y sesión. La observación es editable dentro de la ventana de 24h (RF13).';

-- ── Índices ────────────────────────────────────────────────────────────────
CREATE INDEX idx_attendance_sessions_activity  ON attendance_sessions(activity_id);
CREATE INDEX idx_attendance_sessions_date      ON attendance_sessions(date);
CREATE INDEX idx_attendance_records_session    ON attendance_records(session_id);
CREATE INDEX idx_attendance_records_student    ON attendance_records(student_id);

-- ── Row-Level Security (AD-08) ─────────────────────────────────────────────
ALTER TABLE attendance_sessions ENABLE ROW LEVEL SECURITY;
ALTER TABLE attendance_records  ENABLE ROW LEVEL SECURITY;

CREATE POLICY tenant_isolation_attendance_sessions ON attendance_sessions
    USING (
        activity_id IN (
            SELECT id FROM activities
            WHERE institution_id = current_setting('app.current_institution_id', true)::UUID
        )
        OR current_setting('app.current_role', true) = 'SUPERADMIN'
    );

CREATE POLICY tenant_isolation_attendance_records ON attendance_records
    USING (
        session_id IN (
            SELECT s.id FROM attendance_sessions s
            JOIN activities a ON a.id = s.activity_id
            WHERE a.institution_id = current_setting('app.current_institution_id', true)::UUID
        )
        OR current_setting('app.current_role', true) = 'SUPERADMIN'
    );
