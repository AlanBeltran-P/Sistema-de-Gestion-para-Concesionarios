package com.concesionaria.cliente.historial.dto;

import com.concesionaria.cliente.cliente.dto.ClienteResponse;

import java.util.List;
import java.util.Map;

/**
 * RF-17: todo lo que el cliente hizo con la concesionaria.
 *
 * @param avisos secciones que no se pudieron cargar porque otro servicio no respondió
 *               (el resto del historial se devuelve igual)
 */
public record HistorialResponse(
        ClienteResponse cliente,
        List<Map<String, Object>> cotizaciones,
        List<Map<String, Object>> reservas,
        List<Map<String, Object>> compras,
        List<Map<String, Object>> financiamientos,
        List<Map<String, Object>> pagos,
        List<String> avisos
) {
}
