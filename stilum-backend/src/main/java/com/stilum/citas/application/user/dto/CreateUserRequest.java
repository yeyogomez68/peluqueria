package com.stilum.citas.application.user.dto;

import com.stilum.citas.domain.user.UserRol;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100)
        String nombre,

        @NotBlank(message = "El email es obligatorio")
        @Email
        String email,

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 8, message = "Mínimo 8 caracteres")
        String password,

        @NotNull(message = "El rol es obligatorio")
        UserRol rol
) {}
