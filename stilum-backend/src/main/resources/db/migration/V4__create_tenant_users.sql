-- =============================================================
-- V4: Usuarios del sistema
-- Super admins (sin tenant_id) y usuarios de cada tenant
-- =============================================================

CREATE TYPE user_rol AS ENUM ('SUPER_ADMIN', 'ADMIN_TENANT', 'PROFESIONAL');

-- Tabla unificada de usuarios
CREATE TABLE users (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id   UUID REFERENCES tenants(id) ON DELETE CASCADE, -- NULL = SUPER_ADMIN
    nombre      VARCHAR(100) NOT NULL,
    email       VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    rol         user_rol NOT NULL,
    activo      BOOLEAN NOT NULL DEFAULT TRUE,
    ultimo_login TIMESTAMPTZ,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    -- SUPER_ADMIN no tiene tenant_id; los demás sí
    CONSTRAINT chk_tenant_rol CHECK (
        (rol = 'SUPER_ADMIN' AND tenant_id IS NULL) OR
        (rol <> 'SUPER_ADMIN' AND tenant_id IS NOT NULL)
    )
);

CREATE INDEX idx_users_email ON users (email);
CREATE INDEX idx_users_tenant_id ON users (tenant_id) WHERE tenant_id IS NOT NULL;
CREATE INDEX idx_users_tenant_activo ON users (tenant_id, activo) WHERE tenant_id IS NOT NULL;

COMMENT ON TABLE users IS 'Usuarios del sistema: super admins globales y usuarios por tenant';
COMMENT ON COLUMN users.tenant_id IS 'NULL para SUPER_ADMIN; obligatorio para ADMIN_TENANT y PROFESIONAL';
