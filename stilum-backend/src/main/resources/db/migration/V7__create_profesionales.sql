-- =============================================================
-- V7: Profesionales por tenant
-- =============================================================

CREATE TABLE profesionales (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    nombre          VARCHAR(100) NOT NULL,
    especialidad    VARCHAR(150),
    bio             TEXT,
    foto_url        TEXT,
    color_agenda    VARCHAR(7),   -- Color hex para el calendario (solo guardado, renderizado via tokens)
    activo          BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_profesionales_tenant_id ON profesionales (tenant_id);
CREATE INDEX idx_profesionales_tenant_activo ON profesionales (tenant_id, activo);

-- Horarios de disponibilidad por profesional (por día de semana)
CREATE TABLE horarios_profesional (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    profesional_id      UUID NOT NULL REFERENCES profesionales(id) ON DELETE CASCADE,
    tenant_id           UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    dia_semana          SMALLINT NOT NULL CHECK (dia_semana BETWEEN 1 AND 7), -- 1=Lunes, 7=Domingo
    hora_inicio         TIME NOT NULL,
    hora_fin            TIME NOT NULL,
    activo              BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT chk_horario_valido CHECK (hora_inicio < hora_fin),
    CONSTRAINT uq_profesional_dia UNIQUE (profesional_id, dia_semana)
);

CREATE INDEX idx_horarios_profesional_id ON horarios_profesional (profesional_id);
CREATE INDEX idx_horarios_tenant_id ON horarios_profesional (tenant_id);

COMMENT ON TABLE profesionales IS 'Profesionales que atienden citas en cada tenant';
COMMENT ON COLUMN profesionales.color_agenda IS 'Color identificador en el calendario ResourceTimeGrid';
COMMENT ON COLUMN horarios_profesional.dia_semana IS '1=Lunes ... 7=Domingo (ISO 8601)';
