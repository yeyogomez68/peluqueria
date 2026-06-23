package com.stilum.citas.application.servicio.dto;

import com.stilum.citas.domain.servicio.Servicio;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ServicioResponse(
        UUID id,
        String nombre,
        String descripcion,
        int duracionMin,
        BigDecimal precio,
        boolean activo,
        Instant createdAt
) {
    public static ServicioResponse from(Servicio s) {
        return new ServicioResponse(
                s.getId(), s.getNombre(), s.getDescripcion(),
                s.getDuracionMin(), s.getPrecio(), s.isActivo(), s.getCreatedAt()
        );
    }
}
