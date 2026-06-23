package com.stilum.citas.domain.shared;

/**
 * Excepción base para violaciones de reglas de negocio.
 * Las subclases especializadas mapean a códigos HTTP específicos.
 * SK-B-10: Jerarquía de excepciones de dominio.
 */
public class ReglaNegocioException extends RuntimeException {

    private final String codigo;

    public ReglaNegocioException(String codigo, String mensaje) {
        super(mensaje);
        this.codigo = codigo;
    }

    public String getCodigo() {
        return codigo;
    }
}
