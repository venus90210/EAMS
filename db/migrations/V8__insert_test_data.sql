-- V8: Datos de prueba para desarrollo
-- Institution and test users are already created in V7.5
-- Insertar estudiantes de prueba, asociados al guardian
-- El guardian es creado por TestDataInitializer (ID será el del usuario guardian@example.com)
-- Usamos subconsulta para obtener el ID del guardian
INSERT INTO students (id, first_name, last_name, grade, guardian_id, institution_id, created_at, updated_at)
SELECT
    '550e8400-e29b-41d4-a716-446655440001'::UUID,
    'Juan',
    'Pérez',
    '6A',
    u.id,  -- guardian_id desde el usuario guardian@example.com
    'b716fa11-ea40-468a-9dc8-ae131402c7ff'::UUID,
    NOW(),
    NOW()
FROM users u
WHERE u.email = 'guardian@example.com'
UNION ALL
SELECT
    '550e8400-e29b-41d4-a716-446655440002'::UUID,
    'María',
    'García',
    '6B',
    u.id,
    'b716fa11-ea40-468a-9dc8-ae131402c7ff'::UUID,
    NOW(),
    NOW()
FROM users u
WHERE u.email = 'guardian@example.com'
UNION ALL
SELECT
    '550e8400-e29b-41d4-a716-446655440003'::UUID,
    'Carlos',
    'López',
    '7A',
    u.id,
    'b716fa11-ea40-468a-9dc8-ae131402c7ff'::UUID,
    NOW(),
    NOW()
FROM users u
WHERE u.email = 'guardian@example.com'
ON CONFLICT DO NOTHING;

-- Crear usuario ADMIN de prueba para poder crear actividades
INSERT INTO users (id, email, password_hash, role, first_name, last_name, phone, institution_id, created_at, updated_at)
VALUES
    ('9999aaaa-9999-9999-9999-999999999999'::UUID, 'admin_temp@sanjose.edu.co', 'temp', 'ADMIN', 'Admin', 'Temp', '', 'b716fa11-ea40-468a-9dc8-ae131402c7ff'::UUID, NOW(), NOW())
ON CONFLICT (email) DO NOTHING;
