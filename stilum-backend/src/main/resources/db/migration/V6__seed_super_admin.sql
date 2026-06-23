-- =============================================================
-- V6: Super Admin inicial del sistema
-- IMPORTANTE: cambiar la contraseña en el primer despliegue
-- Password: Admin@Stilum2024! → BCrypt hash
-- =============================================================

INSERT INTO users (tenant_id, nombre, email, password_hash, rol, activo)
VALUES (
    NULL,
    'Super Administrador',
    'admin@stilum.com',
    '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LetdrXCH8LoJPZfZK', -- Admin@Stilum2024!
    'SUPER_ADMIN',
    TRUE
);
