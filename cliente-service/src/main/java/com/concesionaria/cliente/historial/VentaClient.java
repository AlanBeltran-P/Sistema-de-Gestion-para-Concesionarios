package com.concesionaria.cliente.historial;

import com.concesionaria.cliente.historial.dto.PaginaRemota;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/** venta-service (token de servicio, ver common). Trae hasta 100 registros, los más recientes primero. */
@FeignClient(name = "venta-service", contextId = "ventaClient")
public interface VentaClient {

    @GetMapping("/api/cotizaciones?size=100")
    PaginaRemota cotizaciones(@RequestParam("clienteId") Long clienteId);

    @GetMapping("/api/reservas?size=100")
    PaginaRemota reservas(@RequestParam("clienteId") Long clienteId);

    @GetMapping("/api/ventas?size=100")
    PaginaRemota ventas(@RequestParam("clienteId") Long clienteId);
}
