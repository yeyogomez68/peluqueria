-- =============================================================
-- V3: Tabla de suscripciones (tenant → plan)
-- =============================================================

CREATE TYPE subscription_estado AS ENUM ('PRUEBA', 'ACTIVA', 'VENCIDA', 'CANCELADA');

CREATE TABLE subscriptions (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    plan_id         UUID NOT NULL REFERENCES plans(id),
    estado          subscription_estado NOT NULL DEFAULT 'PRUEBA',
    fecha_inicio    DATE NOT NULL DEFAULT CURRENT_DATE,
    fecha_fin       DATE NOT NULL,
    precio_mensual  NUMERIC(10, 2) NOT NULL,
    notas_pago      TEXT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_subscriptions_tenant_id ON subscriptions (tenant_id);
CREATE INDEX idx_subscriptions_estado ON subscriptions (estado);
-- Para verificar rápidamente si un tenant tiene suscripción activa
CREATE INDEX idx_subscriptions_tenant_estado ON subscriptions (tenant_id, estado);

COMMENT ON TABLE subscriptions IS 'Historial de suscripciones de cada tenant';
COMMENT ON COLUMN subscriptions.precio_mensual IS 'Precio al momento de la suscripción (no cambia si el plan cambia precio)';
