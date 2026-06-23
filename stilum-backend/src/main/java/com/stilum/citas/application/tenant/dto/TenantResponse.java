package com.stilum.citas.application.tenant.dto;

import java.time.Instant;
import java.util.UUID;

/** DTO completo de un tenant para respuestas de la API. */
public record TenantResponse(
        UUID id,
        String nombreNegocio,
        String emailContacto,
        String telefonoContacto,
        String ciudad,
        String pais,
        String logoUrl,
        boolean activo,
        Instant createdAt,
        Instant fechaInactivacion
) {}
