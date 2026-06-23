package com.stilum.citas.application.user.dto;

import com.stilum.citas.domain.user.UserRol;

import java.time.Instant;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String nombre,
        String email,
        UserRol rol,
        boolean activo,
        Instant ultimoLogin,
        Instant createdAt
) {}
