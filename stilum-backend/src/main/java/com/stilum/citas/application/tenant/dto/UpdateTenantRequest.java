package com.stilum.citas.application.tenant.dto;

import jakarta.validation.constraints.Size;

/** DTO para actualizar datos básicos de un tenant. Todos los campos son opcionales. */
public record UpdateTenantRequest(

        @Size(max = 100)
        String nombreNegocio,

        String telefonoContacto,

        String ciudad,

        String pais,

        String logoUrl
) {}
