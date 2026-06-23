package com.stilum.citas.application.servicio.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record UpdateServicioRequest(

        @Size(max = 100)
        String nombre,

        String descripcion,

        @Min(1)
        int duracionMin,

        @DecimalMin("0.00")
        BigDecimal precio
) {}
