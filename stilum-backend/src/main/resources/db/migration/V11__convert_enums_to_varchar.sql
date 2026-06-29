-- V11: Convert custom PostgreSQL ENUM types to VARCHAR for Hibernate 6 compatibility
-- This allows @Enumerated(EnumType.STRING) to work without custom JDBC type mapping.

-- 1. Drop constraints referencing enum types (must go first)
ALTER TABLE users DROP CONSTRAINT IF EXISTS chk_tenant_rol;
ALTER TABLE citas DROP CONSTRAINT IF EXISTS no_solapamiento_citas;

-- 2. Drop DEFAULT values that reference enum types (required before DROP TYPE)
ALTER TABLE citas ALTER COLUMN estado DROP DEFAULT;
ALTER TABLE subscriptions ALTER COLUMN estado DROP DEFAULT;

-- 3. users.rol (user_rol → VARCHAR)
ALTER TABLE users ALTER COLUMN rol TYPE VARCHAR(50) USING rol::text;

-- 4. subscriptions.estado (subscription_estado → VARCHAR)
ALTER TABLE subscriptions ALTER COLUMN estado TYPE VARCHAR(30) USING estado::text;

-- 5. citas.estado (cita_estado → VARCHAR)
ALTER TABLE citas ALTER COLUMN estado TYPE VARCHAR(30) USING estado::text;

-- 6. Drop the now-unused custom types (no dependencies remain)
DROP TYPE IF EXISTS user_rol;
DROP TYPE IF EXISTS subscription_estado;
DROP TYPE IF EXISTS cita_estado;

-- 7. Restore DEFAULT values using plain VARCHAR literals
ALTER TABLE citas ALTER COLUMN estado SET DEFAULT 'PENDIENTE';
ALTER TABLE subscriptions ALTER COLUMN estado SET DEFAULT 'PRUEBA';

-- 8. Recreate chk_tenant_rol using plain VARCHAR comparisons
ALTER TABLE users ADD CONSTRAINT chk_tenant_rol
    CHECK (((rol = 'SUPER_ADMIN' AND tenant_id IS NULL) OR (rol <> 'SUPER_ADMIN' AND tenant_id IS NOT NULL)));

-- 9. Recreate the no-overlap EXCLUDE constraint using VARCHAR (btree_gist already installed from V10)
ALTER TABLE citas ADD CONSTRAINT no_solapamiento_citas
    EXCLUDE USING gist (
        profesional_id WITH =,
        tstzrange(fecha_hora_inicio, fecha_hora_fin, '[)') WITH &&
    ) WHERE (estado NOT IN ('CANCELADA', 'NO_SHOW'));
