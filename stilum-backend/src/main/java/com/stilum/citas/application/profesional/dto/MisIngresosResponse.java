package com.stilum.citas.application.profesional.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

public record MisIngresosResponse(
        LocalDate inicio,
        LocalDate fin,
        int totalCitas,
        BigDecimal totalFacturado,
        BigDecimal totalComision,
        List<IngresoDetalle> detalle
) {
    public record IngresoDetalle(
            ZonedDateTime fecha,
            String servicio,
            String cliente,
            BigDecimal precio,
            BigDecimal comision,
            String metodoPago
    ) {}
}
