-- V8: Datos de prueba para desarrollo
-- Insertar institución de prueba
INSERT INTO institutions (id, name, email_domain, created_at, updated_at)
VALUES (
    'b716fa11-ea40-468a-9dc8-ae131402c7ff'::UUID,
    'Colegio San José',
    'sanjose.edu.co',
    NOW(),
    NOW()
) ON CONFLICT (email_domain) DO NOTHING;

-- Insertar estudiantes de prueba
INSERT INTO students (id, first_name, last_name, grade, institution_id, created_at, updated_at)
VALUES
    ('550e8400-e29b-41d4-a716-446655440001'::UUID, 'Juan', 'Pérez', '6A', 'b716fa11-ea40-468a-9dc8-ae131402c7ff'::UUID, NOW(), NOW()),
    ('550e8400-e29b-41d4-a716-446655440002'::UUID, 'María', 'García', '6B', 'b716fa11-ea40-468a-9dc8-ae131402c7ff'::UUID, NOW(), NOW()),
    ('550e8400-e29b-41d4-a716-446655440003'::UUID, 'Carlos', 'López', '7A', 'b716fa11-ea40-468a-9dc8-ae131402c7ff'::UUID, NOW(), NOW())
ON CONFLICT DO NOTHING;

-- Crear usuario ADMIN de prueba para poder crear actividades
INSERT INTO users (id, email, password_hash, role, first_name, last_name, phone, institution_id, created_at, updated_at)
VALUES
    ('9999aaaa-9999-9999-9999-999999999999'::UUID, 'admin_temp@sanjose.edu.co', 'temp', 'ADMIN', 'Admin', 'Temp', '', 'b716fa11-ea40-468a-9dc8-ae131402c7ff'::UUID, NOW(), NOW())
ON CONFLICT (email) DO NOTHING;
