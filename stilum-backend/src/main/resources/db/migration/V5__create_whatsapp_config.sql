-- =============================================================
-- V5: Configuración de WhatsApp por tenant
-- =============================================================

CREATE TABLE tenant_whatsapp_config (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id           UUID NOT NULL UNIQUE REFERENCES tenants(id) ON DELETE CASCADE,
    phone_number_id     VARCHAR(50) NOT NULL UNIQUE, -- ID del número en Meta
    whatsapp_token      TEXT NOT NULL,               -- Token cifrado (AES en la app)
    verify_token        VARCHAR(100) NOT NULL,        -- Token para verificar webhook
    numero_whatsapp     VARCHAR(20) NOT NULL,         -- Número visible ej: +573001234567
    activo              BOOLEAN NOT NULL DEFAULT FALSE,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_wa_config_phone_number_id ON tenant_whatsapp_config (phone_number_id);

COMMENT ON TABLE tenant_whatsapp_config IS 'Credenciales de WhatsApp Business API por tenant';
COMMENT ON COLUMN tenant_whatsapp_config.whatsapp_token IS 'Token cifrado con AES-256; se descifra en memoria al usar';
COMMENT ON COLUMN tenant_whatsapp_config.phone_number_id IS 'Clave de enrutamiento del webhook: identifica a qué tenant pertenece cada mensaje';
