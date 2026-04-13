-- ── Migración V3: Agregar columna guardian_id a students ──────────────────
-- Propósito: La clase Student en Spring Data esperaba una relación one-to-one
--           con guardian_id, pero la tabla no tenía esa columna.
--           Se agrega la columna como foreign key a users (GUARDIAN).

ALTER TABLE students
ADD COLUMN guardian_id UUID NOT NULL REFERENCES users(id),
ADD CONSTRAINT fk_students_guardian FOREIGN KEY (guardian_id) REFERENCES users(id);

-- Crear índice para mejorar queries por guardian
CREATE INDEX idx_students_guardian_id ON students(guardian_id);
