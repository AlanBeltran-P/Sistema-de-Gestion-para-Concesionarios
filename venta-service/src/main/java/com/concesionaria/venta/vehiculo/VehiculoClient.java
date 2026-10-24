package com.concesionaria.venta.vehiculo;

import com.concesionaria.venta.vehiculo.dto.CambiarEstadoRemotoRequest;
import com.concesionaria.venta.vehiculo.dto.CrearVehiculoRemotoRequest;
import com.concesionaria.venta.vehiculo.dto.EstadoVehiculo;
import com.concesionaria.venta.vehiculo.dto.ModeloRemoto;
import com.concesionaria.venta.vehiculo.dto.VehiculoRemoto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/** vehiculo-service. Las llamadas salen con token de servicio (rol SISTEMA, ver common). */
@FeignClient(name = "vehiculo-service", contextId = "vehiculoClient")
public interface VehiculoClient {

    @GetMapping("/api/vehiculos/{id}")
    VehiculoRemoto obtener(@PathVariable("id") Long id);

    @GetMapping("/api/modelos/{id}")
    ModeloRemoto obtenerModelo(@PathVariable("id") Long id);

    @PatchMapping("/api/vehiculos/{id}/estado")
    VehiculoRemoto cambiarEstado(@PathVariable("id") Long id, @RequestBody CambiarEstadoRemotoRequest request);

    /** RF-24: alta del auto recibido como parte de pago. */
    @PostMapping("/api/vehiculos")
    VehiculoRemoto crear(@RequestBody CrearVehiculoRemotoRequest request);

    @DeleteMapping("/api/vehiculos/{id}")
    void darDeBaja(@PathVariable("id") Long id);

    @PostMapping("/api/vehiculos/{id}/reactivar")
    VehiculoRemoto reactivar(@PathVariable("id") Long id);

    /** Cambia el estado solo si el vehículo está en el esperado (si no, 409). */
    default VehiculoRemoto cambiarEstado(Long id, EstadoVehiculo nuevo, EstadoVehiculo esperado) {
        return cambiarEstado(id, new CambiarEstadoRemotoRequest(nuevo, esperado));
    }
}
