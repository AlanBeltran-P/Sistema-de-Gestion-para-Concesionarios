package com.concesionaria.vehiculo.evento;

import com.concesionaria.common.evento.EventoIntegracion;
import com.concesionaria.vehiculo.vehiculo.Vehiculo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Foto completa del vehículo después de cada cambio. reporte-service la usa para mantener
 * su copia del inventario (RF-40): con este evento hace un "upsert" por vehiculoId.
 * Routing keys: vehiculo.creado, vehiculo.actualizado, vehiculo.estado-cambiado,
 * vehiculo.baja, vehiculo.reactivado.
 */
public record VehiculoEvento(
        String eventoId,
        String tipo,
        Long vehiculoId,
        String vin,
        Long modeloId,
        String marca,
        String modelo,
        String categoria,
        Integer anio,
        String condicion,
        String estado,
        String origen,
        BigDecimal precio,
        boolean activo,
        LocalDateTime fechaIngreso,
        LocalDateTime fecha
) implements EventoIntegracion {

    public enum Tipo {
        CREADO("creado"),
        ACTUALIZADO("actualizado"),
        ESTADO_CAMBIADO("estado-cambiado"),
        BAJA("baja"),
        REACTIVADO("reactivado");

        private final String clave;

        Tipo(String clave) {
            this.clave = clave;
        }
    }

    public static VehiculoEvento de(Vehiculo v, Tipo tipo) {
        var modelo = v.getModelo();
        return new VehiculoEvento(
                UUID.randomUUID().toString(),
                tipo.clave,
                v.getId(),
                v.getVin(),
                modelo.getId(),
                modelo.getMarca().getNombre(),
                modelo.getNombre(),
                modelo.getCategoria().getNombre(),
                v.getAnio(),
                v.getCondicion().name(),
                v.getEstado().name(),
                v.getOrigen().name(),
                v.getPrecio(),
                v.isActivo(),
                v.getFechaIngreso(),
                LocalDateTime.now());
    }

    @Override
    public String routingKey() {
        return "vehiculo." + tipo;
    }
}
