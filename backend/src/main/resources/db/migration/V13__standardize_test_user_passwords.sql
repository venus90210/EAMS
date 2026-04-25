-- V13: Standardize all test user passwords to 'password123'
-- Hash: $2a$12$OvYdddyxC5sXJf/Jf72lAOP01KlXRzMJH4eV0JbYdPW0Ly5pRpYJi

UPDATE users 
SET password_hash = '$2a$12$OvYdddyxC5sXJf/Jf72lAOP01KlXRzMJH4eV0JbYdPW0Ly5pRpYJi'
WHERE email IN (
    'teacher@example.com',
    'admin@example.com',
    'prof.carlos@example.com',
    'padre.luis@example.com',
    'madre.ana@example.com'
);
