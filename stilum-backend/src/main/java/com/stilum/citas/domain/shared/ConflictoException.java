package com.stilum.citas.domain.shared;

/** HTTP 409 — conflicto con el estado actual del recurso. */
public class ConflictoException extends ReglaNegocioException {

    public ConflictoException(String codigo, String mensaje) {
        super(codigo, mensaje);
    }
}
