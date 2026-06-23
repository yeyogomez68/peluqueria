package com.stilum.citas.application.plan.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record PlanResponse(
        UUID id,
        String nombre,
        BigDecimal precioMensual,
        int maxProfesionales,
        int maxCitasMes,
        boolean whatsappHabilitado,
        boolean recordatoriosHabilitados,
        boolean reportesAvanzados,
        boolean activo
) {}
