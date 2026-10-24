package com.concesionaria.cliente.historial;

import com.concesionaria.cliente.historial.dto.PaginaRemota;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/** financiamiento-service. */
@FeignClient(name = "financiamiento-service", contextId = "financiamientoClient")
public interface FinanciamientoClient {

    @GetMapping("/api/financiamientos?size=100")
    PaginaRemota financiamientos(@RequestParam("clienteId") Long clienteId);

    @GetMapping("/api/pagos?size=100")
    PaginaRemota pagos(@RequestParam("clienteId") Long clienteId);
}
