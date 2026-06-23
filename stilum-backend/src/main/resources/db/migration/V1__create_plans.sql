-- =============================================================
-- V1: Tabla de planes de suscripción
-- =============================================================

CREATE TABLE plans (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre      VARCHAR(50) NOT NULL UNIQUE,
    precio_mensual NUMERIC(10, 2) NOT NULL,
    max_profesionales   INTEGER NOT NULL DEFAULT 3,
    max_citas_mes       INTEGER NOT NULL DEFAULT 200,
    whatsapp_habilitado         BOOLEAN NOT NULL DEFAULT FALSE,
    recordatorios_habilitados   BOOLEAN NOT NULL DEFAULT FALSE,
    reportes_avanzados          BOOLEAN NOT NULL DEFAULT FALSE,
    activo      BOOLEAN NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Planes iniciales del sistema
INSERT INTO plans (nombre, precio_mensual, max_profesionales, max_citas_mes,
                   whatsapp_habilitado, recordatorios_habilitados, reportes_avanzados)
VALUES
    ('BASICO',  29000, 2,  100, FALSE, TRUE,  FALSE),
    ('PRO',     59000, 5,  500, TRUE,  TRUE,  TRUE),
    ('PREMIUM', 99000, 20, 2000, TRUE, TRUE,  TRUE);

COMMENT ON TABLE plans IS 'Planes de suscripción disponibles para los tenants';
