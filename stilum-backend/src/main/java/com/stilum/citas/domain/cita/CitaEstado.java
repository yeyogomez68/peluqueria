package com.stilum.citas.domain.cita;

import java.util.Set;

public enum CitaEstado {
    PENDIENTE,
    CONFIRMADA,
    EN_CURSO,
    COMPLETADA,
    CANCELADA,
    NO_SHOW;

    private static final Set<CitaEstado> ACTIVOS = Set.of(PENDIENTE, CONFIRMADA, EN_CURSO);
    private static final Set<CitaEstado> TERMINADOS = Set.of(COMPLETADA, CANCELADA, NO_SHOW);

    public boolean estaActiva()    { return ACTIVOS.contains(this); }
    public boolean estaTerminada() { return TERMINADOS.contains(this); }

    /** Transiciones válidas desde este estado. */
    public boolean puedeTransicionarA(CitaEstado destino) {
        return switch (this) {
            case PENDIENTE   -> destino == CONFIRMADA || destino == CANCELADA;
            case CONFIRMADA  -> destino == EN_CURSO   || destino == CANCELADA || destino == NO_SHOW;
            case EN_CURSO    -> destino == COMPLETADA || destino == CANCELADA;
            default          -> false;
        };
    }
}
