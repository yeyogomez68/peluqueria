package com.stilum.citas.domain.cita.spec;

import com.stilum.citas.domain.cita.Cita;
import com.stilum.citas.domain.profesional.HorarioProfesional;
import com.stilum.citas.domain.shared.Specification;

import java.time.DayOfWeek;
import java.time.LocalTime;

/**
 * RN-CITA-003: La cita debe estar dentro del horario laboral del profesional.
 * DayOfWeek ISO: MONDAY=1, SUNDAY=7
 */
public class HorarioLaboralSpec implements Specification<Cita> {

    @Override
    public boolean esCumplida(Cita cita) {
        int diaSemana = cita.getFechaHoraInicio().getDayOfWeek().getValue(); // ISO 1-7
        LocalTime horaInicio = cita.getFechaHoraInicio().toLocalTime();
        LocalTime horaFin    = cita.getFechaHoraFin().toLocalTime();

        return cita.getProfesional().getHorarios().stream()
                .filter(h -> h.getDiaSemana() == diaSemana && h.isActivo())
                .anyMatch(h -> !horaInicio.isBefore(h.getHoraInicio())
                            && !horaFin.isAfter(h.getHoraFin()));
    }
}
