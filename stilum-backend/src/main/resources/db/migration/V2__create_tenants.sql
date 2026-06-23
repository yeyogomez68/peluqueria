-- =============================================================
-- V2: Tabla de tenants (negocios de peluquería)
-- =============================================================

CREATE TABLE tenants (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre_negocio      VARCHAR(100) NOT NULL,
    email_contacto      VARCHAR(150) NOT NULL UNIQUE,
    telefono_contacto   VARCHAR(20),
    ciudad              VARCHAR(100),
    pais                VARCHAR(80) NOT NULL DEFAULT 'Colombia',
    logo_url            TEXT,
    activo              BOOLEAN NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    fecha_inactivacion  TIMESTAMPTZ
);

CREATE INDEX idx_tenants_email ON tenants (email_contacto);
CREATE INDEX idx_tenants_activo ON tenants (activo);

COMMENT ON TABLE tenants IS 'Negocios de peluquería / barbería suscritos a la plataforma';
COMMENT ON COLUMN tenants.activo IS 'FALSE = acceso bloqueado para todo el tenant';
