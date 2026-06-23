package com.stilum.citas.application.subscription.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record ActivarSubscriptionRequest(

        @NotNull(message = "La fecha de fin es obligatoria")
        @FutureOrPresent(message = "La fecha de fin debe ser hoy o en el futuro")
        LocalDate fechaFin,

        String notas
) {}
