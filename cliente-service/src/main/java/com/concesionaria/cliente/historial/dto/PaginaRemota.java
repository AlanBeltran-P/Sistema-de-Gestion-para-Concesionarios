package com.concesionaria.cliente.historial.dto;

import java.util.List;
import java.util.Map;

/**
 * Página que devuelven los otros servicios ({ "content": [...], "page": {...} }).
 * Los elementos se pasan tal cual al historial, con el mismo formato que en su servicio.
 */
public record PaginaRemota(List<Map<String, Object>> content) {
}
