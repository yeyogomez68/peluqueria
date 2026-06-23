package com.stilum.citas.domain.shared;

/**
 * Base del Specification Pattern.
 * SK-B-04 / RN-CITA-001: Reglas de negocio componibles con .y()/.o()/.no().
 *
 * @param <T> Tipo del objeto evaluado (ej: Cita, Profesional).
 */
public interface Specification<T> {

    boolean esCumplida(T objeto);

    /** Verifica y lanza excepción si no se cumple. */
    default void verificar(T objeto, String codigoError, String mensaje) {
        if (!esCumplida(objeto)) {
            throw new ReglaNegocioException(codigoError, mensaje);
        }
    }

    default Specification<T> y(Specification<T> otra) {
        return objeto -> this.esCumplida(objeto) && otra.esCumplida(objeto);
    }

    default Specification<T> o(Specification<T> otra) {
        return objeto -> this.esCumplida(objeto) || otra.esCumplida(objeto);
    }

    default Specification<T> no() {
        return objeto -> !this.esCumplida(objeto);
    }
}
