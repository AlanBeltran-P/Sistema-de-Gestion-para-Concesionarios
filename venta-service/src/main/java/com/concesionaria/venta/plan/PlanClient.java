package com.concesionaria.venta.plan;

import com.concesionaria.venta.plan.dto.SimulacionRemota;
import com.concesionaria.venta.plan.dto.SimulacionRemotaRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * financiamiento-service. Antes de registrar una venta financiada se simula con el plan elegido:
 * si el plan no permite esa cantidad de cuotas o el anticipo no alcanza, devuelve 400.
 */
@FeignClient(name = "financiamiento-service", contextId = "planClient")
public interface PlanClient {

    @PostMapping("/api/planes/simulacion")
    SimulacionRemota simular(@RequestBody SimulacionRemotaRequest request);
}
