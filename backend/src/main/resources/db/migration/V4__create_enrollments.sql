-- V4: Inscripciones
-- Fuente: RF03, RF04, RF05, AD-07 (SELECT FOR UPDATE), AD-08 (RLS)

CREATE TYPE enrollment_status AS ENUM ('ACTIVE', 'CANCELLED');

CREATE TABLE enrollments (
    id           UUID              PRIMARY KEY DEFAULT gen_random_uuid(),
    student_id   UUID              NOT NULL REFERENCES students(id),
    activity_id  UUID              NOT NULL REFERENCES activities(id),
    status       enrollment_status NOT NULL DEFAULT 'ACTIVE',
    enrolled_at  TIMESTAMPTZ       NOT NULL DEFAULT now(),
    cancelled_at TIMESTAMPTZ,

    -- RF05: 0% de inscripciones duplicadas válidas
    CONSTRAINT uq_enrollments_active
        UNIQUE NULLS NOT DISTINCT (student_id, activity_id),

    CONSTRAINT ck_enrollments_cancelled_at
        CHECK (status = 'ACTIVE' OR cancelled_at IS NOT NULL)
);

COMMENT ON TABLE enrollments IS 'Inscripciones de estudiantes en actividades. Nunca se eliminan — se cancelan (AD-07).';
COMMENT ON CONSTRAINT uq_enrollments_active ON enrollments
    IS 'Garantiza 0% duplicados activos (RF05). El bloqueo pesimista SELECT FOR UPDATE opera sobre activities.available_spots.';

-- ── Índices ────────────────────────────────────────────────────────────────
-- Usado por la validación "un solo enrollment ACTIVE por estudiante"
CREATE INDEX idx_enrollments_student_status   ON enrollments(student_id, status);
CREATE INDEX idx_enrollments_activity_status  ON enrollments(activity_id, status);

-- ── Row-Level Security (AD-08) ─────────────────────────────────────────────
ALTER TABLE enrollments ENABLE ROW LEVEL SECURITY;

CREATE POLICY tenant_isolation_enrollments ON enrollments
    USING (
        student_id IN (
            SELECT id FROM students
            WHERE institution_id = current_setting('app.current_institution_id', true)::UUID
        )
        OR current_setting('app.current_role', true) = 'SUPERADMIN'
    );
