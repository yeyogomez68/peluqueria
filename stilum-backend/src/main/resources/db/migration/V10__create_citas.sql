-- =============================================================
-- V10: Citas (núcleo del sistema)
-- =============================================================

CREATE TYPE cita_estado AS ENUM (
    'PENDIENTE',
    'CONFIRMADA',
    'EN_CURSO',
    'COMPLETADA',
    'CANCELADA',
    'NO_SHOW'
);

CREATE TABLE citas (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id           UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    profesional_id      UUID NOT NULL REFERENCES profesionales(id),
    servicio_id         UUID NOT NULL REFERENCES servicios(id),
    cliente_id          UUID NOT NULL REFERENCES clientes(id),

    -- Tiempo
    fecha_hora_inicio   TIMESTAMPTZ NOT NULL,
    fecha_hora_fin      TIMESTAMPTZ NOT NULL,  -- inicio + duracion_min del servicio
    duracion_min        INTEGER NOT NULL,       -- snapshot al crear (servicio puede cambiar)

    -- Estado
    estado              cita_estado NOT NULL DEFAULT 'PENDIENTE',

    -- Precio
    precio_cobrado      NUMERIC(10, 2),         -- null hasta completar; puede diferir del catálogo

    -- QR check-in
    qr_token            UUID UNIQUE DEFAULT gen_random_uuid(),
    checked_in_at       TIMESTAMPTZ,

    -- Cancelación
    motivo_cancelacion  TEXT,
    cancelado_por       VARCHAR(20) CHECK (cancelado_por IN ('CLIENTE', 'NEGOCIO', 'SISTEMA')),

    -- Canal de origen
    origen              VARCHAR(20) NOT NULL DEFAULT 'WEB'
                        CHECK (origen IN ('WEB', 'WHATSAPP', 'MANUAL')),

    -- Notas
    notas               TEXT,

    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_fechas_cita CHECK (fecha_hora_inicio < fecha_hora_fin)
);

-- Índices para las queries más frecuentes
CREATE INDEX idx_citas_tenant_id ON citas (tenant_id);
CREATE INDEX idx_citas_profesional_fecha ON citas (profesional_id, fecha_hora_inicio);
CREATE INDEX idx_citas_tenant_fecha ON citas (tenant_id, fecha_hora_inicio);
CREATE INDEX idx_citas_cliente_id ON citas (cliente_id);
CREATE INDEX idx_citas_estado ON citas (tenant_id, estado);
CREATE INDEX idx_citas_qr_token ON citas (qr_token);

-- Prevenir solapamiento de citas para el mismo profesional
-- (exclusión de rangos de tiempo por profesional)
CREATE EXTENSION IF NOT EXISTS btree_gist;
ALTER TABLE citas ADD CONSTRAINT no_solapamiento_citas
    EXCLUDE USING GIST (
        profesional_id WITH =,
        tstzrange(fecha_hora_inicio, fecha_hora_fin, '[)') WITH &&
    )
    WHERE (estado NOT IN ('CANCELADA', 'NO_SHOW'));

COMMENT ON TABLE citas IS 'Citas agendadas — núcleo del sistema. Aisladas por tenant_id.';
COMMENT ON COLUMN citas.qr_token IS 'UUID para generar el QR de check-in del cliente';
COMMENT ON COLUMN citas.precio_cobrado IS 'Precio real cobrado al completar; puede diferir del catálogo';
COMMENT ON CONSTRAINT no_solapamiento_citas ON citas IS 'Garantía de base de datos: un profesional no puede tener dos citas activas solapadas';
