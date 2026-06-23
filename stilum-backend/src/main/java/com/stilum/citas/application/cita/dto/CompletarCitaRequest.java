package com.stilum.citas.application.cita.dto;

import java.math.BigDecimal;

public record CompletarCitaRequest(
        /** Precio cobrado — si es null se usa el precio del servicio. */
        BigDecimal precioCobrado
) {}
