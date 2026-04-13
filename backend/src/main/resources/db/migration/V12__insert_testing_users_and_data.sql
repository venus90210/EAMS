-- V12: Insert comprehensive testing data for multi-role testing
-- Uses the existing institution from V7_5 (Colegio San José)

-- ═══════════════════════════════════════════════════════════════════════════════
-- Institution already exists in V7_5 with ID: b716fa11-ea40-468a-9dc8-ae131402c7ff
-- ═══════════════════════════════════════════════════════════════════════════════

-- Additional test users for multi-role testing (same institution as V7_5)
INSERT INTO users (id, email, password_hash, role, first_name, last_name, institution_id, created_at, updated_at) VALUES
    -- Additional teacher for testing
    ('d4e5f6a7-b8c9-50db-e2f3-a4b5c6d7e8f9', 'prof.carlos@example.com',
     '$2a$12$OvYdddyxC5sXJf/Jf72lAOP01KlXRzMJH4eV0JbYdPW0Ly5pRpYJi', 'TEACHER',
     'Carlos', 'Méndez', 'b716fa11-ea40-468a-9dc8-ae131402c7ff', now(), now()),

    -- Additional guardians for testing
    ('f6a7b8c9-daeb-52fd-a4b5-c6d7e8f9a0b1', 'padre.luis@example.com',
     '$2a$12$OvYdddyxC5sXJf/Jf72lAOP01KlXRzMJH4eV0JbYdPW0Ly5pRpYJi', 'GUARDIAN',
     'Luis', 'Gómez', 'b716fa11-ea40-468a-9dc8-ae131402c7ff', now(), now()),

    ('a7b8c9da-ebfc-530e-b5c6-d7e8f9a0b1c2', 'madre.ana@example.com',
     '$2a$12$OvYdddyxC5sXJf/Jf72lAOP01KlXRzMJH4eV0JbYdPW0Ly5pRpYJi', 'GUARDIAN',
     'Ana', 'López', 'b716fa11-ea40-468a-9dc8-ae131402c7ff', now(), now())
ON CONFLICT (email) DO NOTHING;

-- ═══════════════════════════════════════════════════════════════════════════════
-- Additional students for testing
-- Each student must have a guardian_id (principal guardian)
-- ═══════════════════════════════════════════════════════════════════════════════

INSERT INTO students (id, first_name, last_name, grade, guardian_id, institution_id, created_at, updated_at) VALUES
    -- Students for padre.luis@example.com
    ('a0b1c2d3-e4f5-46a7-b8c9-daebfcadbecf', 'Santiago', 'Gómez', '9A',
     'f6a7b8c9-daeb-52fd-a4b5-c6d7e8f9a0b1', 'b716fa11-ea40-468a-9dc8-ae131402c7ff', now(), now()),
    ('b1c2d3e4-f5a6-47b8-c9da-ebfcadbecf00', 'Valentina', 'Gómez', '8B',
     'f6a7b8c9-daeb-52fd-a4b5-c6d7e8f9a0b1', 'b716fa11-ea40-468a-9dc8-ae131402c7ff', now(), now()),

    -- Students for madre.ana@example.com
    ('c2d3e4f5-a6b7-48c9-daeb-fcadbecf0011', 'Martín', 'López', '10A',
     'a7b8c9da-ebfc-530e-b5c6-d7e8f9a0b1c2', 'b716fa11-ea40-468a-9dc8-ae131402c7ff', now(), now()),

    -- Additional student for testing multiple guardians
    ('d3e4f5a6-b7c8-49da-ebfc-adbecf001122', 'Andrés', 'Rodríguez', '11A',
     'f6a7b8c9-daeb-52fd-a4b5-c6d7e8f9a0b1', 'b716fa11-ea40-468a-9dc8-ae131402c7ff', now(), now())
ON CONFLICT DO NOTHING;
