package com.stilum.citas.application.cita.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;

public record RegistrarPagoRequest(
        @NotNull @DecimalMin("0.00") BigDecimal precioCobrado,
        @NotNull @Pattern(regexp = "EFECTIVO|NEQUI|DAVIPLATA|TARJETA|TRANSFERENCIA")
        String metodoPago
) {}
