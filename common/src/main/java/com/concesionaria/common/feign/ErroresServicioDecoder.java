package com.concesionaria.common.feign;

import com.concesionaria.common.exception.ConflictoException;
import com.concesionaria.common.exception.NoEncontradoException;
import com.concesionaria.common.exception.ServicioNoDisponibleException;
import com.concesionaria.common.exception.SolicitudInvalidaException;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.io.InputStream;

/**
 * Convierte la respuesta de error de otro servicio (ProblemDetail) en la excepción equivalente,
 * con el mismo mensaje. Así un 409 de vehiculo-service ("No se puede pasar de VENDIDO a RESERVADO")
 * le llega igual al usuario de venta-service. 401, 403 y 5xx son errores internos: se devuelve 503.
 */
@Slf4j
public class ErroresServicioDecoder implements ErrorDecoder {

    private static final ObjectMapper JSON = JsonMapper.builder().build();

    @Override
    public Exception decode(String metodo, Response respuesta) {
        String detalle = leerDetalle(respuesta);
        int status = respuesta.status();
        return switch (status) {
            case 400, 422 -> new SolicitudInvalidaException(detalle);
            case 404 -> new NoEncontradoException(detalle);
            case 409 -> new ConflictoException(detalle);
            default -> {
                log.error("Llamada {} falló con {}: {}", metodo, status, detalle);
                yield new ServicioNoDisponibleException(
                        "Un servicio interno no pudo completar la operación. Intentá de nuevo en unos minutos.");
            }
        };
    }

    private static String leerDetalle(Response respuesta) {
        if (respuesta.body() == null) {
            return "Error " + respuesta.status();
        }
        try (InputStream cuerpo = respuesta.body().asInputStream()) {
            JsonNode json = JSON.readTree(cuerpo);
            JsonNode detalle = json.path("detail");
            return detalle.isString() ? detalle.asString() : "Error " + respuesta.status();
        } catch (Exception e) {
            return "Error " + respuesta.status();
        }
    }
}
