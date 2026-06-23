package com.stilum.citas.presentation.exception;

import com.stilum.citas.domain.shared.AccesoNoAutorizadoException;
import com.stilum.citas.domain.shared.ConflictoException;
import com.stilum.citas.domain.shared.RecursoNoEncontradoException;
import com.stilum.citas.domain.shared.ReglaNegocioException;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Manejador global de excepciones — mapea excepciones de dominio a respuestas HTTP.
 * SK-B-10: Jerarquía de excepciones → HTTP status codes.
 *
 * Formato de respuesta: RFC 7807 Problem Details (ProblemDetail de Spring 6).
 */
@RestControllerAdvice
@Hidden
public class GlobalExceptionHandler {

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ProblemDetail> handleNotFound(RecursoNoEncontradoException ex) {
        return buildProblem(HttpStatus.NOT_FOUND, ex.getCodigo(), ex.getMessage());
    }

    @ExceptionHandler(AccesoNoAutorizadoException.class)
    public ResponseEntity<ProblemDetail> handleForbidden(AccesoNoAutorizadoException ex) {
        return buildProblem(HttpStatus.FORBIDDEN, ex.getCodigo(), ex.getMessage());
    }

    @ExceptionHandler(ConflictoException.class)
    public ResponseEntity<ProblemDetail> handleConflict(ConflictoException ex) {
        return buildProblem(HttpStatus.CONFLICT, ex.getCodigo(), ex.getMessage());
    }

    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<ProblemDetail> handleReglaNegocio(ReglaNegocioException ex) {
        return buildProblem(HttpStatus.UNPROCESSABLE_ENTITY, ex.getCodigo(), ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String field = error instanceof FieldError fe ? fe.getField() : error.getObjectName();
            errors.put(field, error.getDefaultMessage());
        });

        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setTitle("Error de validación");
        problem.setDetail("Uno o más campos tienen valores inválidos");
        problem.setProperty("errores", errors);
        problem.setProperty("timestamp", Instant.now());
        return ResponseEntity.badRequest().body(problem);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleGeneral(Exception ex) {
        // Log sin exponer detalles internos al cliente
        return buildProblem(HttpStatus.INTERNAL_SERVER_ERROR,
                "ERROR_INTERNO", "Ocurrió un error interno. Por favor contacte soporte.");
    }

    private ResponseEntity<ProblemDetail> buildProblem(HttpStatus status, String codigo, String detalle) {
        ProblemDetail problem = ProblemDetail.forStatus(status);
        problem.setDetail(detalle);
        problem.setProperty("codigo", codigo);
        problem.setProperty("timestamp", Instant.now());
        return ResponseEntity.status(status).body(problem);
    }
}
