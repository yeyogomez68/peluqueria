package com.stilum.citas.application.auth.dto;

import com.stilum.citas.domain.user.UserRol;

import java.util.UUID;

/**
 * DTO de respuesta para login exitoso.
 * SK-B-02: Records para DTOs inmutables.
 */
public record LoginResponse(
        String token,
        UUID userId,
        String nombre,
        String email,
        UserRol rol,
        UUID tenantId,    // null para SUPER_ADMIN
        String tenantNombre // null para SUPER_ADMIN
) {}
