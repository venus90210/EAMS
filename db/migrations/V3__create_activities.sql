-- V3: Actividades extracurriculares
-- Fuente: RF01, RF02, AD-08 (RLS), AD-07 (available_spots)

CREATE TYPE activity_status AS ENUM ('DRAFT', 'PUBLISHED', 'DISABLED');

CREATE TYPE day_of_week AS ENUM (
    'MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY'
);

CREATE TABLE activities (
    id               UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    name             VARCHAR(100)    NOT NULL,
    description      TEXT,
    status           activity_status NOT NULL DEFAULT 'DRAFT',
    total_spots      INTEGER         NOT NULL CHECK (total_spots > 0),
    available_spots  INTEGER         NOT NULL CHECK (available_spots >= 0),
    institution_id   UUID            NOT NULL REFERENCES institutions(id),
    created_by       UUID            NOT NULL REFERENCES users(id),
    created_at       TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ     NOT NULL DEFAULT now(),

    CONSTRAINT ck_activities_spots_consistency
        CHECK (available_spots <= total_spots)
);

COMMENT ON TABLE activities IS 'Catálogo de actividades extracurriculares por institución.';
COMMENT ON COLUMN activities.total_spots    IS 'Inmutable operativamente. Solo ADMIN puede modificarlo (genera entrada en audit_log).';
COMMENT ON COLUMN activities.available_spots IS 'Contador dinámico. Se decrementa con SELECT FOR UPDATE (AD-07).';

-- Tabla informativa de horarios — no participa en validación de conflictos (AD-02)
CREATE TABLE schedules (
    id           UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    activity_id  UUID        NOT NULL REFERENCES activities(id) ON DELETE CASCADE,
    day_of_week  day_of_week NOT NULL,
    start_time   TIME        NOT NULL,
    end_time     TIME        NOT NULL,
    location     VARCHAR(100),

    CONSTRAINT ck_schedules_time_range CHECK (end_time > start_time)
);

COMMENT ON TABLE schedules IS 'Horario informativo de la actividad. No se usa para validar conflictos de inscripción.';

-- ── Índices ────────────────────────────────────────────────────────────────
CREATE INDEX idx_activities_institution_status ON activities(institution_id, status);
CREATE INDEX idx_activities_institution_id     ON activities(institution_id);
CREATE INDEX idx_schedules_activity_id         ON schedules(activity_id);

-- ── Row-Level Security (AD-08) ─────────────────────────────────────────────
ALTER TABLE activities ENABLE ROW LEVEL SECURITY;
ALTER TABLE schedules  ENABLE ROW LEVEL SECURITY;

CREATE POLICY tenant_isolation_activities ON activities
    USING (
        institution_id = current_setting('app.current_institution_id', true)::UUID
        OR current_setting('app.current_role', true) = 'SUPERADMIN'
    );

CREATE POLICY tenant_isolation_schedules ON schedules
    USING (
        activity_id IN (
            SELECT id FROM activities
            WHERE institution_id = current_setting('app.current_institution_id', true)::UUID
        )
        OR current_setting('app.current_role', true) = 'SUPERADMIN'
    );
