-- V2: Usuarios y estudiantes
-- Fuente: RF08, RF10, AD-08 (institution_id obligatorio)

CREATE TABLE users (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    email           VARCHAR(255) NOT NULL,
    password_hash   VARCHAR(255) NOT NULL,
    role            VARCHAR(50)  NOT NULL,
    first_name      VARCHAR(100),
    last_name       VARCHAR(100),
    phone           VARCHAR(20),
    mfa_secret      VARCHAR(64),            -- TOTP secret (RNF04, AD-06)
    institution_id  UUID        REFERENCES institutions(id),  -- NULL solo para SUPERADMIN
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT uq_users_email UNIQUE (email),
    CONSTRAINT ck_users_institution_required
        CHECK (role = 'SUPERADMIN' OR institution_id IS NOT NULL)
);

COMMENT ON TABLE users IS 'Usuarios de la plataforma: GUARDIAN, TEACHER, ADMIN, SUPERADMIN.';
COMMENT ON COLUMN users.institution_id IS 'Obligatorio para todos los roles excepto SUPERADMIN.';

CREATE TABLE students (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    first_name      VARCHAR(100) NOT NULL,
    last_name       VARCHAR(100) NOT NULL,
    grade           VARCHAR(20),
    guardian_id     UUID        NOT NULL REFERENCES users(id),
    institution_id  UUID        NOT NULL REFERENCES institutions(id),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

COMMENT ON TABLE students IS 'Estudiantes registrados en una institución.';

CREATE TABLE guardian_students (
    guardian_id  UUID NOT NULL REFERENCES users(id),
    student_id   UUID NOT NULL REFERENCES students(id),
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),

    PRIMARY KEY (guardian_id, student_id)
);

COMMENT ON TABLE guardian_students IS 'Relación acudiente ↔ estudiante (RF10 — asociar estudiantes con acudientes).';

-- ── Índices ────────────────────────────────────────────────────────────────
CREATE INDEX idx_users_institution_id   ON users(institution_id);
CREATE INDEX idx_students_institution_id ON students(institution_id);
CREATE INDEX idx_students_guardian_id ON students(guardian_id);
CREATE INDEX idx_guardian_students_guardian ON guardian_students(guardian_id);
CREATE INDEX idx_guardian_students_student  ON guardian_students(student_id);

-- ── Row-Level Security (AD-08) ─────────────────────────────────────────────
ALTER TABLE users    ENABLE ROW LEVEL SECURITY;
ALTER TABLE students ENABLE ROW LEVEL SECURITY;

CREATE POLICY tenant_isolation_users ON users
    USING (
        institution_id = current_setting('app.current_institution_id', true)::UUID
        OR current_setting('app.current_role', true) = 'SUPERADMIN'
    );

CREATE POLICY tenant_isolation_students ON students
    USING (
        institution_id = current_setting('app.current_institution_id', true)::UUID
        OR current_setting('app.current_role', true) = 'SUPERADMIN'
    );
