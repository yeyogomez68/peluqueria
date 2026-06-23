package com.stilum.citas.domain.shared;

/** HTTP 403 — el usuario no tiene permisos para realizar la operación. */
public class AccesoNoAutorizadoException extends ReglaNegocioException {

    public AccesoNoAutorizadoException(String mensaje) {
        super("ACCESO_NO_AUTORIZADO", mensaje);
    }
}
