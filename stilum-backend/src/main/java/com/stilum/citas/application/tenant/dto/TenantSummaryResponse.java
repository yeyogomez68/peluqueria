package com.stilum.citas.application.tenant.dto;

import java.util.UUID;

/** DTO resumido para listados de tenants. */
public record TenantSummaryResponse(
        UUID id,
        String nombreNegocio,
        String emailContacto,
        String ciudad,
        boolean activo
) {}
