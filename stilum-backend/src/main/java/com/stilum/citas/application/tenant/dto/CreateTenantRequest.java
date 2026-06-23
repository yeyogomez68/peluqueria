package com.stilum.citas.application.tenant.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * DTO para crear un nuevo tenant.
 * SK-B-02: Records para DTOs inmutables.
 */
public record CreateTenantRequest(

        @NotBlank(message = "El nombre del negocio es obligatorio")
        @Size(max = 100, message = "El nombre no puede superar 100 caracteres")
        String nombreNegocio,

        @NotBlank(message = "El email de contacto es obligatorio")
        @Email(message = "El email debe tener un formato válido")
        String emailContacto,

        String telefonoContacto,

        String ciudad,

        String pais,

        // Plan inicial para la suscripción de prueba
        @NotNull(message = "El plan es obligatorio")
        UUID planId,

        // Datos del usuario administrador inicial del tenant
        @NotBlank(message = "El nombre del administrador es obligatorio")
        String adminNombre,

        @NotBlank(message = "La contraseña inicial del administrador es obligatoria")
        @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
        String adminPassword
) {}
