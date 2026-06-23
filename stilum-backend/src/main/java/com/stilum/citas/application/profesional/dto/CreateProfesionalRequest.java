package com.stilum.citas.application.profesional.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateProfesionalRequest(

        @NotBlank @Size(max = 100)
        String nombre,

        @Size(max = 150)
        String especialidad,

        String bio,

        /** Color hexadecimal para el calendario (p. ej. #C9A84C). */
        @Size(max = 7)
        String colorAgenda,

        @Valid
        List<HorarioDto> horarios
) {}
