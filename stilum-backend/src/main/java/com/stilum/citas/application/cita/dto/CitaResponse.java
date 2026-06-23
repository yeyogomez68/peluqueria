package com.stilum.citas.application.cita.dto;

import com.stilum.citas.domain.cita.Cita;
import com.stilum.citas.domain.cita.CitaEstado;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.UUID;

public record CitaResponse(
        UUID id,
        UUID qrToken,
        CitaEstado estado,
        ZonedDateTime fechaHoraInicio,
        ZonedDateTime fechaHoraFin,
        int duracionMin,
        BigDecimal precioCobrado,
        String origen,
        String notas,
        String motivoCancelacion,
        String canceladoPor,
        Instant checkedInAt,
        ProfesionalResumen profesional,
        ServicioResumen servicio,
        ClienteResumen cliente,
        Instant createdAt
) {

    public record ProfesionalResumen(UUID id, String nombre, String colorAgenda) {}
    public record ServicioResumen(UUID id, String nombre, int duracionMin, BigDecimal precio) {}
    public record ClienteResumen(UUID id, String nombre, String telefono) {}

    public static CitaResponse from(Cita c) {
        return new CitaResponse(
                c.getId(),
                c.getQrToken(),
                c.getEstado(),
                c.getFechaHoraInicio(),
                c.getFechaHoraFin(),
                c.getDuracionMin(),
                c.getPrecioCobrado(),
                c.getOrigen(),
                c.getNotas(),
                c.getMotivoCancelacion(),
                c.getCanceladoPor(),
                c.getCheckedInAt(),
                new ProfesionalResumen(
                        c.getProfesional().getId(),
                        c.getProfesional().getNombre(),
                        c.getProfesional().getColorAgenda()),
                new ServicioResumen(
                        c.getServicio().getId(),
                        c.getServicio().getNombre(),
                        c.getServicio().getDuracionMin(),
                        c.getServicio().getPrecio()),
                new ClienteResumen(
                        c.getCliente().getId(),
                        c.getCliente().getNombre(),
                        c.getCliente().getTelefono()),
                c.getCreatedAt()
        );
    }
}
