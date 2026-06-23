-- V11: Convert custom PostgreSQL ENUM types to VARCHAR for Hibernate 6 compatibility
-- This allows @Enumerated(EnumType.STRING) to work without custom JDBC type mapping.

-- 1. users.rol (user_rol → VARCHAR)
ALTER TABLE users ALTER COLUMN rol TYPE VARCHAR(50) USING rol::text;

-- 2. subscriptions.estado (subscription_estado → VARCHAR)
ALTER TABLE subscriptions ALTER COLUMN estado TYPE VARCHAR(30) USING estado::text;

-- 3. citas.estado (cita_estado → VARCHAR)
ALTER TABLE citas ALTER COLUMN estado TYPE VARCHAR(30) USING estado::text;

-- Drop the now-unused custom types (optional but clean)
DROP TYPE IF EXISTS user_rol;
DROP TYPE IF EXISTS subscription_estado;
DROP TYPE IF EXISTS cita_estado;
