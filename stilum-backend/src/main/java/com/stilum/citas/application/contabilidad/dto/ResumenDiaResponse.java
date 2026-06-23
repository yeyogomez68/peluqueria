package com.stilum.citas.application.contabilidad.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ResumenDiaResponse(
        LocalDate fecha,
        int totalCitas,
        BigDecimal totalVendido,
        BigDecimal totalComisiones,
        BigDecimal totalNegocio,
        List<ResumenProfesional> porProfesional,
        List<ResumenServicio> porServicio,
        List<ResumenMetodoPago> porMetodoPago
) {
    public record ResumenProfesional(
            String nombre,
            int citas,
            BigDecimal totalVendido,
            BigDecimal comision,
            BigDecimal comisionPorcentaje
    ) {}

    public record ResumenServicio(
            String nombre,
            int cantidad,
            BigDecimal totalVendido
    ) {}

    public record ResumenMetodoPago(
            String metodo,
            int cantidad,
            BigDecimal total
    ) {}
}
