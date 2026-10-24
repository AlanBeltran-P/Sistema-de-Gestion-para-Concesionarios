package com.concesionaria.common.exception;

import org.springframework.http.HttpStatus;

/** 403: está logueado pero no puede acceder a ese recurso (ej: un cliente viendo datos de otro). */
public class AccesoDenegadoException extends ApiException {

    public AccesoDenegadoException(String mensaje) {
        super(HttpStatus.FORBIDDEN, mensaje);
    }
}
