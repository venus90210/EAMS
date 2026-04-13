-- V12: Insert comprehensive testing data
-- Crea múltiples instituciones, usuarios con diferentes roles, estudiantes y actividades
-- para testing exhaustivo de funcionalidades por rol

-- ═══════════════════════════════════════════════════════════════════════════════
-- PARTE 1: INSTITUCIONES ADICIONALES
-- ═══════════════════════════════════════════════════════════════════════════════

INSERT INTO institutions (id, name, email_domain, created_at, updated_at) VALUES
    ('a1b2c3d4-e5f6-47a8-b9c0-d1e2f3a4b5c6', 'Colegio San José', 'sanjose.edu.co', now(), now()),
    ('b2c3d4e5-f6a7-48b9-c0d1-e2f3a4b5c6d7', 'Instituto Técnico Industrial', 'itindi.edu.co', now(), now()),
    ('c3d4e5f6-a7b8-49ca-d1e2-f3a4b5c6d7e8', 'Escuela de Artes y Oficios', 'eao.edu.co', now(), now())
ON CONFLICT DO NOTHING;

-- ═══════════════════════════════════════════════════════════════════════════════
-- PARTE 2: USUARIOS CON DIFERENTES ROLES
-- ═══════════════════════════════════════════════════════════════════════════════

-- INSTITUCIÓN 1: Instituto Técnico Metropolitano (original)
-- Ya tiene: guardian@example.com, teacher@example.com, admin@example.com

-- INSTITUCIÓN 2: Colegio San José
INSERT INTO users (id, email, password_hash, role, institution_id, mfa_secret, is_active, created_at, updated_at) VALUES
    -- Admin de Colegio San José
    ('d4e5f6a7-b8c9-50db-e2f3-a4b5c6d7e8f9', 'admin.sanjose@example.com',
     '$2a$12$OvYdddyxC5sXJf/Jf72lAOP01KlXRzMJH4eV0JbYdPW0Ly5pRpYJi', 'ADMIN',
     'a1b2c3d4-e5f6-47a8-b9c0-d1e2f3a4b5c6', NULL, true, now(), now()),

    -- Teacher de Colegio San José
    ('e5f6a7b8-c9da-51ec-f3a4-b5c6d7e8f9a0', 'prof.carlos@example.com',
     '$2a$12$OvYdddyxC5sXJf/Jf72lAOP01KlXRzMJH4eV0JbYdPW0Ly5pRpYJi', 'TEACHER',
     'a1b2c3d4-e5f6-47a8-b9c0-d1e2f3a4b5c6', NULL, true, now(), now()),

    -- Guardians de Colegio San José
    ('f6a7b8c9-daeb-52fd-a4b5-c6d7e8f9a0b1', 'padre.luis@example.com',
     '$2a$12$OvYdddyxC5sXJf/Jf72lAOP01KlXRzMJH4eV0JbYdPW0Ly5pRpYJi', 'GUARDIAN',
     'a1b2c3d4-e5f6-47a8-b9c0-d1e2f3a4b5c6', NULL, true, now(), now()),

    ('a7b8c9da-ebfc-530e-b5c6-d7e8f9a0b1c2', 'madre.ana@example.com',
     '$2a$12$OvYdddyxC5sXJf/Jf72lAOP01KlXRzMJH4eV0JbYdPW0Ly5pRpYJi', 'GUARDIAN',
     'a1b2c3d4-e5f6-47a8-b9c0-d1e2f3a4b5c6', NULL, true, now(), now())
ON CONFLICT DO NOTHING;

-- INSTITUCIÓN 3: Instituto Técnico Industrial
INSERT INTO users (id, email, password_hash, role, institution_id, mfa_secret, is_active, created_at, updated_at) VALUES
    ('b8c9daeb-fcad-541f-c6d7-e8f9a0b1c2d3', 'admin.itti@example.com',
     '$2a$12$OvYdddyxC5sXJf/Jf72lAOP01KlXRzMJH4eV0JbYdPW0Ly5pRpYJi', 'ADMIN',
     'b2c3d4e5-f6a7-48b9-c0d1-e2f3a4b5c6d7', NULL, true, now(), now()),

    ('c9daebfc-adbe-552a-d7e8-f9a0b1c2d3e4', 'prof.juan@example.com',
     '$2a$12$OvYdddyxC5sXJf/Jf72lAOP01KlXRzMJH4eV0JbYdPW0Ly5pRpYJi', 'TEACHER',
     'b2c3d4e5-f6a7-48b9-c0d1-e2f3a4b5c6d7', NULL, true, now(), now()),

    ('daebfcad-bfcf-563b-e8f9-a0b1c2d3e4f5', 'guardiana.maria@example.com',
     '$2a$12$OvYdddyxC5sXJf/Jf72lAOP01KlXRzMJH4eV0JbYdPW0Ly5pRpYJi', 'GUARDIAN',
     'b2c3d4e5-f6a7-48b9-c0d1-e2f3a4b5c6d7', NULL, true, now(), now())
ON CONFLICT DO NOTHING;

-- INSTITUCIÓN 4: Escuela de Artes y Oficios
INSERT INTO users (id, email, password_hash, role, institution_id, mfa_secret, is_active, created_at, updated_at) VALUES
    ('ebfcadbe-cfda-574c-f9a0-b1c2d3e4f5a6', 'admin.eao@example.com',
     '$2a$12$OvYdddyxC5sXJf/Jf72lAOP01KlXRzMJH4eV0JbYdPW0Ly5pRpYJi', 'ADMIN',
     'c3d4e5f6-a7b8-49ca-d1e2-f3a4b5c6d7e8', NULL, true, now(), now()),

    ('fcadbecf-eafb-585d-a0b1-c2d3e4f5a6b7', 'prof.diego@example.com',
     '$2a$12$OvYdddyxC5sXJf/Jf72lAOP01KlXRzMJH4eV0JbYdPW0Ly5pRpYJi', 'TEACHER',
     'c3d4e5f6-a7b8-49ca-d1e2-f3a4b5c6d7e8', NULL, true, now(), now())
ON CONFLICT DO NOTHING;

-- ═══════════════════════════════════════════════════════════════════════════════
-- PARTE 3: ESTUDIANTES (vinculados a guardianes)
-- ═══════════════════════════════════════════════════════════════════════════════

-- Estudiantes para Colegio San José (padre.luis@example.com)
INSERT INTO students (id, first_name, last_name, grade, institution_id, created_at, updated_at) VALUES
    ('a0b1c2d3-e4f5-46a7-b8c9-daebfcadbecf', 'Santiago', 'Gómez', '9A',
     'a1b2c3d4-e5f6-47a8-b9c0-d1e2f3a4b5c6', now(), now()),
    ('b1c2d3e4-f5a6-47b8-c9da-ebfcadbecf00', 'Valentina', 'Gómez', '8B',
     'a1b2c3d4-e5f6-47a8-b9c0-d1e2f3a4b5c6', now(), now())
ON CONFLICT DO NOTHING;

-- Estudiantes para Colegio San José (madre.ana@example.com)
INSERT INTO students (id, first_name, last_name, grade, institution_id, created_at, updated_at) VALUES
    ('c2d3e4f5-a6b7-48c9-daeb-fcadbecf0011', 'Martín', 'López', '10A',
     'a1b2c3d4-e5f6-47a8-b9c0-d1e2f3a4b5c6', now(), now())
ON CONFLICT DO NOTHING;

-- Estudiantes para ITTI (guardiana.maria@example.com)
INSERT INTO students (id, first_name, last_name, grade, institution_id, created_at, updated_at) VALUES
    ('d3e4f5a6-b7c8-49da-ebfc-adbecf001122', 'Andrés', 'Rodríguez', '11A',
     'b2c3d4e5-f6a7-48b9-c0d1-e2f3a4b5c6d7', now(), now()),
    ('e4f5a6b7-c8d9-50eb-fcad-becf00112233', 'Catalina', 'Rodríguez', '9C',
     'b2c3d4e5-f6a7-48b9-c0d1-e2f3a4b5c6d7', now(), now())
ON CONFLICT DO NOTHING;

-- ═══════════════════════════════════════════════════════════════════════════════
-- PARTE 4: VINCULACIÓN GUARDIAN-ESTUDIANTE
-- ═══════════════════════════════════════════════════════════════════════════════

-- padre.luis@example.com (f6a7b8c9-daeb-52fd-a4b5-c6d7e8f9a0b1) → Santiago y Valentina
INSERT INTO guardian_students (guardian_id, student_id) VALUES
    ('f6a7b8c9-daeb-52fd-a4b5-c6d7e8f9a0b1', 'a0b1c2d3-e4f5-46a7-b8c9-daebfcadbecf'),
    ('f6a7b8c9-daeb-52fd-a4b5-c6d7e8f9a0b1', 'b1c2d3e4-f5a6-47b8-c9da-ebfcadbecf00')
ON CONFLICT DO NOTHING;

-- madre.ana@example.com (a7b8c9da-ebfc-530e-b5c6-d7e8f9a0b1c2) → Martín
INSERT INTO guardian_students (guardian_id, student_id) VALUES
    ('a7b8c9da-ebfc-530e-b5c6-d7e8f9a0b1c2', 'c2d3e4f5-a6b7-48c9-daeb-fcadbecf0011')
ON CONFLICT DO NOTHING;

-- guardiana.maria@example.com (daebfcad-bfcf-563b-e8f9-a0b1c2d3e4f5) → Andrés y Catalina
INSERT INTO guardian_students (guardian_id, student_id) VALUES
    ('daebfcad-bfcf-563b-e8f9-a0b1c2d3e4f5', 'd3e4f5a6-b7c8-49da-ebfc-adbecf001122'),
    ('daebfcad-bfcf-563b-e8f9-a0b1c2d3e4f5', 'e4f5a6b7-c8d9-50eb-fcad-becf00112233')
ON CONFLICT DO NOTHING;

-- ═══════════════════════════════════════════════════════════════════════════════
-- PARTE 5: ACTIVIDADES DE PRUEBA (diferentes instituciones, estados)
-- ═══════════════════════════════════════════════════════════════════════════════

-- COLEGIO SAN JOSÉ: Actividades en diferentes estados
INSERT INTO activities (id, name, description, status, total_spots, available_spots, institution_id, created_at, updated_at) VALUES
    -- DRAFT (no visible para GUARDIAN aún)
    ('a1a2a3a4-a5a6-a7a8-a9aa-ababacadaeaf', 'Taller de Electrónica', 'Aprende lo básico de electrónica digital', 'DRAFT',
     15, 15, 'a1b2c3d4-e5f6-47a8-b9c0-d1e2f3a4b5c6', now(), now()),

    -- PUBLISHED (visible para GUARDIAN, con cupos)
    ('b2b3b4b5-b6b7-b8b9-babb-bbcbbdbebfb0', 'Fútbol Profesional', 'Entrenamiento de fútbol 5 vs 5', 'PUBLISHED',
     20, 8, 'a1b2c3d4-e5f6-47a8-b9c0-d1e2f3a4b5c6', now(), now()),

    -- PUBLISHED (sin cupos → no se puede inscribir)
    ('c3c4c5c6-c7c8-c9ca-cbcc-cdcececfcac1', 'Ajedrez Avanzado', 'Solo para jugadores nivel intermedio+', 'PUBLISHED',
     10, 0, 'a1b2c3d4-e5f6-47a8-b9c0-d1e2f3a4b5c6', now(), now()),

    -- PUBLISHED (con cupos disponibles)
    ('d4d5d6d7-d8d9-dada-dbdc-dddedfdad2d3', 'Danza Contemporánea', 'Expresión corporal y movimiento libre', 'PUBLISHED',
     25, 12, 'a1b2c3d4-e5f6-47a8-b9c0-d1e2f3a4b5c6', now(), now()),

    -- DISABLED (no visible para GUARDIAN)
    ('e5e6e7e8-e9ea-ebec-eded-eeeef0e1e2e3', 'Debate Académico', 'Competencias de debate y oratoria', 'DISABLED',
     20, 15, 'a1b2c3d4-e5f6-47a8-b9c0-d1e2f3a4b5c6', now(), now())
ON CONFLICT DO NOTHING;

-- INSTITUTO TÉCNICO INDUSTRIAL
INSERT INTO activities (id, name, description, status, total_spots, available_spots, institution_id, created_at, updated_at) VALUES
    ('f6f7f8f9-fafa-fbfc-fdfe-ffffffffa4f4', 'Soldadura Industrial', 'Técnicas de soldadura MIG/TIG', 'PUBLISHED',
     12, 5, 'b2c3d4e5-f6a7-48b9-c0d1-e2f3a4b5c6d7', now(), now()),

    ('a7a8a9aa-abac-adae-afb0-b1b2b3b4b5f5', 'Mecánica Automotriz', 'Diagnóstico y reparación de vehículos', 'PUBLISHED',
     18, 9, 'b2c3d4e5-f6a7-48b9-c0d1-e2f3a4b5c6d7', now(), now()),

    ('b8b9babb-acad-aeaf-b0b1-b2b3b4b5b6f6', 'Programación en Python', 'Introducción a programación con Python', 'DRAFT',
     25, 25, 'b2c3d4e5-f6a7-48b9-c0d1-e2f3a4b5c6d7', now(), now())
ON CONFLICT DO NOTHING;

-- ESCUELA DE ARTES Y OFICIOS
INSERT INTO activities (id, name, description, status, total_spots, available_spots, institution_id, created_at, updated_at) VALUES
    ('c9cacbcc-cdce-cfda-dbe1-c2c3c4c5c6f7', 'Pintura Acrílica', 'Técnicas de pintura en acrílico sobre lienzo', 'PUBLISHED',
     20, 15, 'c3d4e5f6-a7b8-49ca-d1e2-f3a4b5c6d7e8', now(), now()),

    ('dadbbcbd-cecf-d0db-dce3-d4d5d6d7d8f8', 'Cerámica y Alfarería', 'Técnicas tradicionales de trabajo en cerámica', 'PUBLISHED',
     15, 7, 'c3d4e5f6-a7b8-49ca-d1e2-f3a4b5c6d7e8', now(), now())
ON CONFLICT DO NOTHING;

-- ═══════════════════════════════════════════════════════════════════════════════
-- PARTE 6: ALGUNOS ENROLLMENTS DE PRUEBA (para testing de asistencia)
-- ═══════════════════════════════════════════════════════════════════════════════

-- Santiago inscrito en Fútbol
INSERT INTO enrollments (id, student_id, activity_id, institution_id, status, enrolled_at, created_at, updated_at) VALUES
    ('e0e1e2e3-e4e5-e6e7-e8e9-eaebecedeff9', 'a0b1c2d3-e4f5-46a7-b8c9-daebfcadbecf',
     'b2b3b4b5-b6b7-b8b9-babb-bbcbbdbebfb0', 'a1b2c3d4-e5f6-47a8-b9c0-d1e2f3a4b5c6',
     'ACTIVE', now(), now(), now())
ON CONFLICT DO NOTHING;

-- Valentina inscrita en Danza
INSERT INTO enrollments (id, student_id, activity_id, institution_id, status, enrolled_at, created_at, updated_at) VALUES
    ('f1f2f3f4-f5f6-f7f8-f9fa-fbfcfdfeff0a', 'b1c2d3e4-f5a6-47b8-c9da-ebfcadbecf00',
     'd4d5d6d7-d8d9-dada-dbdc-dddedfdad2d3', 'a1b2c3d4-e5f6-47a8-b9c0-d1e2f3a4b5c6',
     'ACTIVE', now(), now(), now())
ON CONFLICT DO NOTHING;

-- Andrés inscrito en Soldadura
INSERT INTO enrollments (id, student_id, activity_id, institution_id, status, enrolled_at, created_at, updated_at) VALUES
    ('a2a3a4a5-a6a7-a8a9-aaab-acabacadae0b', 'd3e4f5a6-b7c8-49da-ebfc-adbecf001122',
     'f6f7f8f9-fafa-fbfc-fdfe-ffffffffa4f4', 'b2c3d4e5-f6a7-48b9-c0d1-e2f3a4b5c6d7',
     'ACTIVE', now(), now(), now())
ON CONFLICT DO NOTHING;

-- Catalina inscrita en Mecánica Automotriz
INSERT INTO enrollments (id, student_id, activity_id, institution_id, status, enrolled_at, created_at, updated_at) VALUES
    ('b3b4b5b6-b7b8-b9ba-bbbc-bcbdbebf0c0c', 'e4f5a6b7-c8d9-50eb-fcad-becf00112233',
     'a7a8a9aa-abac-adae-afb0-b1b2b3b4b5f5', 'b2c3d4e5-f6a7-48b9-c0d1-e2f3a4b5c6d7',
     'ACTIVE', now(), now(), now())
ON CONFLICT DO NOTHING;

-- ═══════════════════════════════════════════════════════════════════════════════
-- PARTE 7: ATTENDANCE SESSION DE PRUEBA
-- ═══════════════════════════════════════════════════════════════════════════════

INSERT INTO attendance_sessions (id, activity_id, institution_id, date, opened_at, closed_at) VALUES
    ('c4c5c6c7-c8c9-caca-cbcc-cdcececfad0d', 'b2b3b4b5-b6b7-b8b9-babb-bbcbbdbebfb0',
     'a1b2c3d4-e5f6-47a8-b9c0-d1e2f3a4b5c6', now()::date, now() - INTERVAL '2 hours', NULL)
ON CONFLICT DO NOTHING;

-- ═══════════════════════════════════════════════════════════════════════════════
-- RESUMEN DE DATOS CREADOS
-- ═══════════════════════════════════════════════════════════════════════════════
-- INSTITUCIONES:
--   1. Instituto Técnico Metropolitano (original)
--   2. Colegio San José
--   3. Instituto Técnico Industrial
--   4. Escuela de Artes y Oficios
--
-- USUARIOS POR ROL:
--   SUPERADMIN: ninguno (crear manualmente si es necesario)
--   ADMIN: 4 (uno por institución)
--   TEACHER: 4 (uno o dos por institución)
--   GUARDIAN: 3 (en Colegio San José + ITTI)
--
-- ESTUDIANTES: 7 (vinculados a guardianes)
-- ACTIVIDADES: 10 (en diferentes estados: DRAFT, PUBLISHED, DISABLED)
-- ENROLLMENTS: 4 (para testing de asistencia)
--
-- CREDENCIALES DE PRUEBA (todas con contraseña: password123):
-- Colegio San José:
--   ADMIN: admin.sanjose@example.com
--   TEACHER: prof.carlos@example.com
--   GUARDIAN: padre.luis@example.com, madre.ana@example.com
--
-- ITTI:
--   ADMIN: admin.itti@example.com
--   TEACHER: prof.juan@example.com
--   GUARDIAN: guardiana.maria@example.com
--
-- EAO:
--   ADMIN: admin.eao@example.com
--   TEACHER: prof.diego@example.com
