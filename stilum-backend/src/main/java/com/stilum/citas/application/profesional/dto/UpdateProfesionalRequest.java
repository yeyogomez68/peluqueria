package com.stilum.citas.application.profesional.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

import java.util.List;

public record UpdateProfesionalRequest(

        @Size(max = 100)
        String nombre,

        @Size(max = 150)
        String especialidad,

        String bio,

        String fotoUrl,

        @Size(max = 7)
        String colorAgenda,

        @Valid
        List<HorarioDto> horarios
) {}
