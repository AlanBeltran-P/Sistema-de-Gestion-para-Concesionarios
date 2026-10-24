package com.concesionaria.auth.exception;

import org.springframework.http.HttpStatus;

/** 409: el recurso ya existe o choca con otro (ej: email repetido). */
public class ConflictoException extends ApiException {

    public ConflictoException(String mensaje) {
        super(HttpStatus.CONFLICT, mensaje);
    }
}
