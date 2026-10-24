package com.concesionaria.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/** Error de negocio que se devuelve al cliente con un status HTTP y un mensaje legible. */
@Getter
public abstract class ApiException extends RuntimeException {

    private final HttpStatus status;

    protected ApiException(HttpStatus status, String mensaje) {
        super(mensaje);
        this.status = status;
    }
}
