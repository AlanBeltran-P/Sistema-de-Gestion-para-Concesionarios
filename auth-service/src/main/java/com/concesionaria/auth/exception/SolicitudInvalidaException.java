package com.concesionaria.auth.exception;

import org.springframework.http.HttpStatus;

/** 400: el pedido no cumple una regla de negocio. */
public class SolicitudInvalidaException extends ApiException {

    public SolicitudInvalidaException(String mensaje) {
        super(HttpStatus.BAD_REQUEST, mensaje);
    }
}
