-- V13: Estado de conversaciones WhatsApp para el bot

CREATE TABLE whatsapp_conversaciones (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    telefono        VARCHAR(20) NOT NULL,
    estado          VARCHAR(30) NOT NULL DEFAULT 'INICIO'
                    CHECK (estado IN ('INICIO','ESPERANDO_NECESIDAD','ESPERANDO_FECHA',
                                     'ESPERANDO_CONFIRMACION','COMPLETADO','CANCELADO')),
    datos_json      TEXT NOT NULL DEFAULT '{}',
    ultimo_mensaje  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (tenant_id, telefono)
);

CREATE INDEX idx_conv_tenant_telefono ON whatsapp_conversaciones (tenant_id, telefono);
CREATE INDEX idx_conv_ultimo_mensaje ON whatsapp_conversaciones (ultimo_mensaje);

COMMENT ON TABLE whatsapp_conversaciones IS
    'Estado de conversación activa por número de WhatsApp. Se borra al completar o tras 24h.';
