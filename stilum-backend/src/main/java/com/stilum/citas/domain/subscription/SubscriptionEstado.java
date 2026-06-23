package com.stilum.citas.domain.subscription;

public enum SubscriptionEstado {
    PRUEBA,
    ACTIVA,
    VENCIDA,
    CANCELADA;

    public boolean estaVigente() {
        return this == ACTIVA || this == PRUEBA;
    }
}
