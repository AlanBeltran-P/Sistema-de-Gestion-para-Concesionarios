package com.concesionaria.common.exception;

import org.springframework.http.HttpStatus;

/** 409: el recurso ya existe o está en un estado que no permite la operación. */
public class ConflictoException extends ApiException {

    public ConflictoException(String mensaje) {
        super(HttpStatus.CONFLICT, mensaje);
    }
}
