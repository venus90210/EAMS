-- V7.5: Create test institution and test users for enrollment testing
-- These users must exist before V8 inserts students that reference them

INSERT INTO institutions (id, name, email_domain, created_at, updated_at)
VALUES (
    'b716fa11-ea40-468a-9dc8-ae131402c7ff'::UUID,
    'Colegio San José',
    'sanjose.edu.co',
    NOW(),
    NOW()
) ON CONFLICT (email_domain) DO NOTHING;

INSERT INTO users (id, email, password_hash, role, first_name, last_name, phone, institution_id, created_at, updated_at)
VALUES
    ('e349f2e1-a891-4299-b53c-b21ef76256c5'::UUID, 'guardian@example.com', '$2a$10$Tj3BtaplLt3imej9i0ka0urHrmMKjeJBgetWYJosGGFf2TOwjNQhS', 'GUARDIAN', 'Ana', 'Martínez', '+57 300 1234567', 'b716fa11-ea40-468a-9dc8-ae131402c7ff'::UUID, NOW(), NOW()),
    ('c1234567-a891-4299-b53c-b21ef76256c5'::UUID, 'teacher@example.com', '$2a$10$W.Y3PVY0Ej8Y2J4X5K1QIe5M6N7O8P9QR0S1T2U3V4W5X6Y7Z8', 'TEACHER', 'Pedro', 'Rodríguez', '+57 300 2345678', 'b716fa11-ea40-468a-9dc8-ae131402c7ff'::UUID, NOW(), NOW()),
    ('6a7dae15-2c5b-4932-87f9-d3c3132b2dfb'::UUID, 'admin@example.com', '$2a$10$1nTsRiZSalCYS2LEI7tlquikaBpM96OnED1HN4B87BONf9PoLdBG.', 'ADMIN', 'Clara', 'Sánchez', '+57 300 3456789', 'b716fa11-ea40-468a-9dc8-ae131402c7ff'::UUID, NOW(), NOW())
ON CONFLICT (email) DO NOTHING;
