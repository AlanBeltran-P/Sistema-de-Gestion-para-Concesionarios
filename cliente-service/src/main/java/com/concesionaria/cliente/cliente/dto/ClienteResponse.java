package com.concesionaria.cliente.cliente.dto;

import com.concesionaria.cliente.cliente.Cliente;
import com.concesionaria.cliente.cliente.enums.OrigenCliente;
import com.concesionaria.cliente.cliente.enums.TipoDocumento;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @param tieneCuenta    true si el cliente puede entrar al portal (tiene usuario en auth-service)
 * @param perfilCompleto documento, teléfono y dirección cargados (necesarios para comprar)
 */
public record ClienteResponse(
        Long id,
        Long usuarioId,
        String nombre,
        String apellido,
        TipoDocumento tipoDocumento,
        String numeroDocumento,
        String email,
        String telefono,
        DireccionDto direccion,
        LocalDate fechaNacimiento,
        OrigenCliente origen,
        boolean tieneCuenta,
        boolean perfilCompleto,
        LocalDateTime fechaCreacion
) {

    public static ClienteResponse desde(Cliente c) {
        DireccionDto direccion = DireccionDto.desde(c.getDireccion());
        boolean completo = c.getTipoDocumento() != null && c.getNumeroDocumento() != null
                && c.getTelefono() != null && direccion != null;
        return new ClienteResponse(
                c.getId(),
                c.getUsuarioId(),
                c.getNombre(),
                c.getApellido(),
                c.getTipoDocumento(),
                c.getNumeroDocumento(),
                c.getEmail(),
                c.getTelefono(),
                direccion,
                c.getFechaNacimiento(),
                c.getOrigen(),
                c.getUsuarioId() != null,
                completo,
                c.getFechaCreacion());
    }
}
