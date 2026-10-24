package com.concesionaria.common.exception;

import org.springframework.http.HttpStatus;

/** 404 */
public class NoEncontradoException extends ApiException {

    public NoEncontradoException(String mensaje) {
        super(HttpStatus.NOT_FOUND, mensaje);
    }
}
