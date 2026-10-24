package com.concesionaria.common.exception;

import org.springframework.http.HttpStatus;

/** 401: credenciales o token inválidos. */
public class NoAutorizadoException extends ApiException {

    public NoAutorizadoException(String mensaje) {
        super(HttpStatus.UNAUTHORIZED, mensaje);
    }
}
