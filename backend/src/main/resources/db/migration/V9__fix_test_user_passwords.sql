-- V9: Crear actividades y datos de prueba
-- Nota: Los usuarios de prueba se crean mediante TestDataInitializer en la aplicación Spring
-- para asegurar que las contraseñas estén correctamente hasheadas con bcrypt.

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
