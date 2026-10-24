package com.concesionaria.financiamiento.cliente;

import com.concesionaria.financiamiento.cliente.dto.ClienteRemoto;
import com.concesionaria.financiamiento.cliente.dto.UsuarioPortalRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/** cliente-service. Las llamadas salen con token de servicio (common). */
@FeignClient(name = "cliente-service", contextId = "clienteClient")
public interface ClienteClient {

    /** Perfil de cliente del usuario logueado en el portal (lo crea si no existe). */
    @PostMapping("/api/clientes/portal")
    ClienteRemoto delPortal(@RequestBody UsuarioPortalRequest request);
}
