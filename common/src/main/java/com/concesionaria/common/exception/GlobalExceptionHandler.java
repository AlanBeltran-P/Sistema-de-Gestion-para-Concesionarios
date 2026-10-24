package com.concesionaria.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.query.sqm.UnknownPathException;
import org.jspecify.annotations.Nullable;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.core.PropertyReferenceException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Todos los errores se devuelven con el formato estándar ProblemDetail (RFC 9457):
 * { "status": 404, "title": "Not Found", "detail": "Vehículo 5 no encontrado", "instance": "/api/..." }
 * Los errores de validación agregan "errores": { "campo": "mensaje" }.
 * Se registra solo desde CommonWebAutoConfiguration.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ProblemDetail manejarApiException(ApiException ex) {
        return ProblemDetail.forStatusAndDetail(ex.getStatus(), ex.getMessage());
    }

    /** @PreAuthorize rechazado: rol sin permiso. */
    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail manejarAccesoDenegado(AccessDeniedException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, "No tenés permiso para realizar esta operación");
    }

    /** Dos usuarios modificaron el mismo registro a la vez (@Version). */
    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ProblemDetail manejarConcurrencia(OptimisticLockingFailureException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
                "Otro usuario modificó este registro al mismo tiempo. Volvé a intentarlo.");
    }

    /** ?sort=campoQueNoExiste en consultas derivadas o Specifications. */
    @ExceptionHandler(PropertyReferenceException.class)
    public ProblemDetail manejarOrdenInvalido(PropertyReferenceException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                "No se puede ordenar ni filtrar por '" + ex.getPropertyName() + "'");
    }

    /** ?sort=campoQueNoExiste en consultas @Query: Hibernate no encuentra el atributo. */
    @ExceptionHandler(InvalidDataAccessApiUsageException.class)
    public ProblemDetail manejarUsoInvalidoDeDatos(InvalidDataAccessApiUsageException ex) {
        for (Throwable causa = ex; causa != null && causa != causa.getCause(); causa = causa.getCause()) {
            if (causa instanceof UnknownPathException) {
                return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                        "No se puede ordenar por un campo que no existe");
            }
        }
        return manejarErrorInesperado(ex);
    }

    /** Cualquier error no previsto: se loguea completo y al cliente le llega un mensaje genérico. */
    @ExceptionHandler(Exception.class)
    public ProblemDetail manejarErrorInesperado(Exception ex) {
        log.error("Error inesperado", ex);
        return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrió un error inesperado");
    }

    @Override
    @Nullable
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {
        Map<String, String> errores = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(e -> errores.putIfAbsent(e.getField(), e.getDefaultMessage()));

        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Hay datos inválidos");
        problema.setProperty("errores", errores);
        return ResponseEntity.badRequest().body(problema);
    }
}
