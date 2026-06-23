package com.stilum.citas.application.cita.dto;

import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * Slot de tiempo disponible para un profesional en un día.
 * Usado por el frontend al abrir el modal de nueva cita.
 */
public record SlotDisponibleResponse(
        UUID profesionalId,
        String profesionalNombre,
        ZonedDateTime inicio,
        ZonedDateTime fin,
        int duracionMin
) {}
