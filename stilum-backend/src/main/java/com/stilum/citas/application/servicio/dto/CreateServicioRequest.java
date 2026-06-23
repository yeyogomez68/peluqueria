package com.stilum.citas.application.servicio.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateServicioRequest(

        @NotBlank @Size(max = 100)
        String nombre,

        String descripcion,

        /** Duración en minutos — debe ser > 0 (RN-SERV-001). */
        @Min(1)
        int duracionMin,

        @DecimalMin("0.00")
        BigDecimal precio
) {}
