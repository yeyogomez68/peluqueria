package com.stilum.citas.application.profesional.dto;

import com.stilum.citas.domain.profesional.HorarioProfesional;
import com.stilum.citas.domain.profesional.Profesional;

import java.time.Instant;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public record ProfesionalResponse(
        UUID id,
        String nombre,
        String especialidad,
        String bio,
        String fotoUrl,
        String colorAgenda,
        boolean activo,
        List<HorarioItem> horarios,
        Instant createdAt
) {

    public record HorarioItem(
            UUID id,
            int diaSemana,
            LocalTime horaInicio,
            LocalTime horaFin,
            boolean activo
    ) {
        public static HorarioItem from(HorarioProfesional h) {
            return new HorarioItem(h.getId(), h.getDiaSemana(),
                    h.getHoraInicio(), h.getHoraFin(), h.isActivo());
        }
    }

    public static ProfesionalResponse from(Profesional p) {
        List<HorarioItem> horarios = p.getHorarios().stream()
                .map(HorarioItem::from)
                .toList();
        return new ProfesionalResponse(
                p.getId(), p.getNombre(), p.getEspecialidad(),
                p.getBio(), p.getFotoUrl(), p.getColorAgenda(),
                p.isActivo(), horarios, p.getCreatedAt()
        );
    }
}
