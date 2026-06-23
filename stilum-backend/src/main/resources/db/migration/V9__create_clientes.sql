-- =============================================================
-- V9: Clientes por tenant (historial de quienes han pedido citas)
-- =============================================================

CREATE TABLE clientes (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    nombre          VARCHAR(100) NOT NULL,
    telefono        VARCHAR(20),
    email           VARCHAR(150),
    notas           TEXT,          -- Preferencias o información relevante del cliente
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_cliente_telefono_tenant UNIQUE (tenant_id, telefono)
);

CREATE INDEX idx_clientes_tenant_id ON clientes (tenant_id);
CREATE INDEX idx_clientes_telefono ON clientes (telefono);

COMMENT ON TABLE clientes IS 'Clientes que han agendado citas en cada tenant. Identificados principalmente por teléfono (WhatsApp).';
