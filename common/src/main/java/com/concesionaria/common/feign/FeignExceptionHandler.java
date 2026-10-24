package com.concesionaria.common.feign;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** El otro servicio no responde (caído, timeout, no registrado en Eureka): 503 en vez de 500. */
@Slf4j
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class FeignExceptionHandler {

    @ExceptionHandler(FeignException.class)
    public ProblemDetail manejarFeign(FeignException ex) {
        log.error("Falló una llamada a otro servicio", ex);
        return ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE,
                "Un servicio interno no está disponible. Intentá de nuevo en unos minutos.");
    }
}
