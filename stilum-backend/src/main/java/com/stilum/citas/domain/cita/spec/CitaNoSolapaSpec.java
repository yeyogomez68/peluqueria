package com.stilum.citas.domain.cita.spec;

import com.stilum.citas.domain.cita.Cita;
import com.stilum.citas.domain.cita.CitaRepository;
import com.stilum.citas.domain.shared.Specification;

import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * RN-CITA-001: Un profesional no puede tener dos citas activas solapadas.
 * La DB también lo garantiza con un EXCLUDE constraint, pero esta spec
 * lo valida a nivel de dominio antes de llegar a la DB.
 */
public class CitaNoSolapaSpec implements Specification<Cita> {

    private final CitaRepository citaRepository;

    public CitaNoSolapaSpec(CitaRepository citaRepository) {
        this.citaRepository = citaRepository;
    }

    @Override
    public boolean esCumplida(Cita cita) {
        UUID profesionalId = cita.getProfesional().getId();
        ZonedDateTime inicio = cita.getFechaHoraInicio();
        ZonedDateTime fin    = cita.getFechaHoraFin();
        UUID citaId          = cita.getId(); // null si es nueva

        return !citaRepository.existeSolapamiento(profesionalId, inicio, fin, citaId);
    }
}
