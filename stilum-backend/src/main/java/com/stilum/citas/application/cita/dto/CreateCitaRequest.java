package com.stilum.citas.application.cita.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.ZonedDateTime;
import java.util.UUID;

public record CreateCitaRequest(

        @NotNull
        UUID profesionalId,

        @NotNull
        UUID servicioId,

        /** Teléfono del cliente — identifica o crea al cliente (RN-CLIENTE-001). */
        @NotNull @Size(max = 20)
        String clienteTelefono,

        @Size(max = 100)
        String clienteNombre,

        @NotNull
        ZonedDateTime fechaHoraInicio,

        /** "WEB" o "WHATSAPP" — por defecto WEB. */
        String origen,

        String notas
) {}
