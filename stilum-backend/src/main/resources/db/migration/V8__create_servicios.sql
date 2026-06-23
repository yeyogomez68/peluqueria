-- =============================================================
-- V8: Catálogo de servicios por tenant
-- =============================================================

CREATE TABLE servicios (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    nombre          VARCHAR(100) NOT NULL,
    descripcion     TEXT,
    duracion_min    INTEGER NOT NULL CHECK (duracion_min > 0),  -- Duración en minutos
    precio          NUMERIC(10, 2) NOT NULL CHECK (precio >= 0),
    activo          BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_servicios_tenant_id ON servicios (tenant_id);
CREATE INDEX idx_servicios_tenant_activo ON servicios (tenant_id, activo);

COMMENT ON TABLE servicios IS 'Catálogo de servicios ofrecidos por cada tenant';
COMMENT ON COLUMN servicios.duracion_min IS 'Duración estimada del servicio en minutos (determina el tamaño del bloque en el calendario)';
