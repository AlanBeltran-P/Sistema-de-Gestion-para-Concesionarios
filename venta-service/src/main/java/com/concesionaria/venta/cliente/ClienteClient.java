package com.concesionaria.venta.cliente;

import com.concesionaria.common.security.UsuarioActual;
import com.concesionaria.venta.cliente.dto.ClienteRemoto;
import com.concesionaria.venta.cliente.dto.UsuarioPortalRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/** cliente-service. */
@FeignClient(name = "cliente-service", contextId = "clienteClient")
public interface ClienteClient {

    @GetMapping("/api/clientes/{id}")
    ClienteRemoto obtener(@PathVariable("id") Long id);

    /** Perfil de cliente del usuario logueado en el portal (lo crea si todavía no existe). */
    @PostMapping("/api/clientes/portal")
    ClienteRemoto delPortal(@RequestBody UsuarioPortalRequest request);

    default ClienteRemoto delUsuario(UsuarioActual usuario) {
        return delPortal(new UsuarioPortalRequest(usuario.id(), usuario.email(), usuario.nombre()));
    }
}
