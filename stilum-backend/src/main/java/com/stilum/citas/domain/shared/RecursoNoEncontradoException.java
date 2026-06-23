package com.stilum.citas.domain.shared;

/** HTTP 404 — el recurso solicitado no existe. */
public class RecursoNoEncontradoException extends ReglaNegocioException {

    public RecursoNoEncontradoException(String recurso, Object id) {
        super("RECURSO_NO_ENCONTRADO", recurso + " con id '" + id + "' no encontrado");
    }
}
