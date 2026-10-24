package com.concesionaria.common.exception;

import org.springframework.http.HttpStatus;

/** 503: otro microservicio que se necesitaba para la operación no respondió o falló. */
public class ServicioNoDisponibleException extends ApiException {

    public ServicioNoDisponibleException(String mensaje) {
        super(HttpStatus.SERVICE_UNAVAILABLE, mensaje);
    }
}
