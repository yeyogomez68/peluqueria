package com.stilum.citas.application.profesional.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;

/** DTO para un bloque de horario semanal de un profesional. */
public record HorarioDto(

        /** Día ISO: 1=Lunes … 7=Domingo */
        @NotNull @Min(1) @Max(7)
        Integer diaSemana,

        @NotNull
        LocalTime horaInicio,

        @NotNull
        LocalTime horaFin
) {}
