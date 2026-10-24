package com.concesionaria.cliente.evento;

import com.concesionaria.cliente.cliente.Cliente;
import com.concesionaria.common.evento.EventoIntegracion;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Datos de contacto del cliente después de cada alta o cambio.
 * notificacion-service los necesita para mandar emails (ej: recordatorio de cuota) y
 * reporte-service para mostrar el nombre en el reporte de cartera (RF-41).
 * Routing keys: cliente.creado, cliente.actualizado.
 */
public record ClienteEvento(
        String eventoId,
        String tipo,
        Long clienteId,
        Long usuarioId,
        String nombre,
        String apellido,
        String email,
        String telefono,
        String tipoDocumento,
        String numeroDocumento,
        LocalDateTime fecha
) implements EventoIntegracion {

    public static ClienteEvento creado(Cliente c) {
        return de(c, "creado");
    }

    public static ClienteEvento actualizado(Cliente c) {
        return de(c, "actualizado");
    }

    private static ClienteEvento de(Cliente c, String tipo) {
        return new ClienteEvento(
                UUID.randomUUID().toString(),
                tipo,
                c.getId(),
                c.getUsuarioId(),
                c.getNombre(),
                c.getApellido(),
                c.getEmail(),
                c.getTelefono(),
                c.getTipoDocumento() != null ? c.getTipoDocumento().name() : null,
                c.getNumeroDocumento(),
                LocalDateTime.now());
    }

    @Override
    public String routingKey() {
        return "cliente." + tipo;
    }
}
