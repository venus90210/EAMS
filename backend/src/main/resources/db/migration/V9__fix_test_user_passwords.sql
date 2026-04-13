-- V9: Crear usuarios de prueba y actividades

-- Insertar usuarios principales (contraseñas temporales)
INSERT INTO users (id, email, password_hash, role, first_name, last_name, phone, institution_id, created_at, updated_at)
VALUES
    ('3a05ed1a-7637-47a7-bb14-d52f25cc4f9f'::UUID, 'guardian@sanjose.edu.co', 'temp_guardian', 'GUARDIAN', 'Ana', 'Martínez', '+57 300 1234567', 'b716fa11-ea40-468a-9dc8-ae131402c7ff'::UUID, NOW(), NOW()),
    ('4b05ed1a-7637-47a7-bb14-d52f25cc4f9f'::UUID, 'teacher@sanjose.edu.co', 'temp_teacher', 'TEACHER', 'Pedro', 'Rodríguez', '+57 300 2345678', 'b716fa11-ea40-468a-9dc8-ae131402c7ff'::UUID, NOW(), NOW()),
    ('5c05ed1a-7637-47a7-bb14-d52f25cc4f9f'::UUID, 'admin@sanjose.edu.co', 'temp_admin', 'ADMIN', 'Clara', 'Sánchez', '+57 300 3456789', 'b716fa11-ea40-468a-9dc8-ae131402c7ff'::UUID, NOW(), NOW())
ON CONFLICT (email) DO NOTHING;

-- Asociar estudiantes con guardian
INSERT INTO guardian_students (guardian_id, student_id, created_at)
VALUES
    ('3a05ed1a-7637-47a7-bb14-d52f25cc4f9f'::UUID, '550e8400-e29b-41d4-a716-446655440001'::UUID, NOW()),
    ('3a05ed1a-7637-47a7-bb14-d52f25cc4f9f'::UUID, '550e8400-e29b-41d4-a716-446655440002'::UUID, NOW())
ON CONFLICT DO NOTHING;

-- Insertar actividades de prueba
INSERT INTO activities (id, name, description, total_spots, available_spots, status, institution_id, created_by, created_at, updated_at)
VALUES
    (
        '6d05ed1a-7637-47a7-bb14-d52f25cc4f9f'::UUID,
        'Fútbol',
        'Entrenamiento de fútbol para todas las edades',
        20,
        15,
        'PUBLISHED',
        'b716fa11-ea40-468a-9dc8-ae131402c7ff'::UUID,
        '9999aaaa-9999-9999-9999-999999999999'::UUID,
        NOW(),
        NOW()
    ),
    (
        '7e05ed1a-7637-47a7-bb14-d52f25cc4f9f'::UUID,
        'Matemáticas Avanzadas',
        'Nivelación y profundización en matemáticas',
        15,
        8,
        'PUBLISHED',
        'b716fa11-ea40-468a-9dc8-ae131402c7ff'::UUID,
        '9999aaaa-9999-9999-9999-999999999999'::UUID,
        NOW(),
        NOW()
    ),
    (
        '8f05ed1a-7637-47a7-bb14-d52f25cc4f9f'::UUID,
        'Arte y Creatividad',
        'Taller de artes visuales y expresión creativa',
        12,
        5,
        'PUBLISHED',
        'b716fa11-ea40-468a-9dc8-ae131402c7ff'::UUID,
        '9999aaaa-9999-9999-9999-999999999999'::UUID,
        NOW(),
        NOW()
    )
ON CONFLICT DO NOTHING;

-- Insertar horarios para las actividades
INSERT INTO schedules (id, activity_id, day_of_week, start_time, end_time, location)
VALUES
    ('a1111111-1111-1111-1111-111111111111'::UUID, '6d05ed1a-7637-47a7-bb14-d52f25cc4f9f'::UUID, 'MONDAY', '15:00'::TIME, '16:30'::TIME, 'Cancha Principal'),
    ('a2222222-2222-2222-2222-222222222222'::UUID, '6d05ed1a-7637-47a7-bb14-d52f25cc4f9f'::UUID, 'WEDNESDAY', '15:00'::TIME, '16:30'::TIME, 'Cancha Principal'),
    ('b1111111-1111-1111-1111-111111111111'::UUID, '7e05ed1a-7637-47a7-bb14-d52f25cc4f9f'::UUID, 'TUESDAY', '16:00'::TIME, '17:00'::TIME, 'Aula 203'),
    ('b2222222-2222-2222-2222-222222222222'::UUID, '7e05ed1a-7637-47a7-bb14-d52f25cc4f9f'::UUID, 'THURSDAY', '16:00'::TIME, '17:00'::TIME, 'Aula 203'),
    ('c1111111-1111-1111-1111-111111111111'::UUID, '8f05ed1a-7637-47a7-bb14-d52f25cc4f9f'::UUID, 'FRIDAY', '14:00'::TIME, '15:30'::TIME, 'Taller de Arte')
ON CONFLICT DO NOTHING;
