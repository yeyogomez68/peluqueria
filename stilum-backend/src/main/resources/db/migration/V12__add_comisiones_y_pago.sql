-- V12: Comisiones por profesional y método de pago en citas

-- 1. Porcentaje de comisión por profesional (default 30%)
ALTER TABLE profesionales
    ADD COLUMN comision_porcentaje NUMERIC(5, 2) NOT NULL DEFAULT 30.00
        CHECK (comision_porcentaje >= 0 AND comision_porcentaje <= 100);

COMMENT ON COLUMN profesionales.comision_porcentaje IS
    'Porcentaje de comisión que le corresponde al profesional sobre precio_cobrado';

-- 2. Método de pago y comisión calculada al completar una cita
ALTER TABLE citas
    ADD COLUMN metodo_pago VARCHAR(20)
        CHECK (metodo_pago IN ('EFECTIVO', 'NEQUI', 'DAVIPLATA', 'TARJETA', 'TRANSFERENCIA')),
    ADD COLUMN comision_calculada NUMERIC(10, 2);

COMMENT ON COLUMN citas.metodo_pago IS 'Medio de pago registrado al completar la cita';
COMMENT ON COLUMN citas.comision_calculada IS
    'precio_cobrado * profesional.comision_porcentaje / 100, calculado al completar';
