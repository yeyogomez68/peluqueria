package com.stilum.citas.application.cita.dto;

import jakarta.validation.constraints.Size;

public record CancelarCitaRequest(

        @Size(max = 500)
        String motivo,

        /** "CLIENTE", "PROFESIONAL" o "ADMIN" */
        @Size(max = 20)
        String canceladoPor
) {}
