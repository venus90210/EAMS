-- V10: Actualizar contraseñas de usuarios de prueba con hashes BCrypt válidos
-- Contraseña: "password" (para todos los usuarios de prueba)
-- Hash BCrypt generado con rounds=10

UPDATE users SET password_hash = '$2b$10$jH6m.hRzDu5mwXdt1erGfed7ussi8dvG.hk.UD7dSEAoIdvIoirDG'
WHERE email IN ('guardian@sanjose.edu.co', 'teacher@sanjose.edu.co', 'admin@sanjose.edu.co');
