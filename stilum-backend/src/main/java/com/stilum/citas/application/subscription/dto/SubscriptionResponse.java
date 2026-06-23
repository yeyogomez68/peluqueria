package com.stilum.citas.application.subscription.dto;

import com.stilum.citas.domain.subscription.SubscriptionEstado;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record SubscriptionResponse(
        UUID id,
        UUID tenantId,
        String tenantNombre,
        UUID planId,
        String planNombre,
        SubscriptionEstado estado,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        BigDecimal precioMensual,
        String notasPago,
        boolean vigente
) {}
