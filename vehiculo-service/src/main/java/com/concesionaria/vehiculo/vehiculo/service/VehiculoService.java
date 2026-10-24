package com.concesionaria.vehiculo.vehiculo.service;

import com.concesionaria.common.exception.AccesoDenegadoException;
import com.concesionaria.common.exception.ConflictoException;
import com.concesionaria.common.exception.NoEncontradoException;
import com.concesionaria.common.exception.SolicitudInvalidaException;
import com.concesionaria.common.security.UsuarioActual;
import com.concesionaria.vehiculo.evento.VehiculoEvento;
import com.concesionaria.vehiculo.modelo.Modelo;
import com.concesionaria.vehiculo.modelo.service.ModeloService;
import com.concesionaria.vehiculo.vehiculo.Vehiculo;
import com.concesionaria.vehiculo.vehiculo.dto.ActualizarVehiculoRequest;
import com.concesionaria.vehiculo.vehiculo.dto.CrearVehiculoRequest;
import com.concesionaria.vehiculo.vehiculo.dto.FiltroVehiculos;
import com.concesionaria.vehiculo.vehiculo.dto.VehiculoResponse;
import com.concesionaria.vehiculo.vehiculo.dto.VehiculoResumenResponse;
import com.concesionaria.vehiculo.vehiculo.enums.CondicionVehiculo;
import com.concesionaria.vehiculo.vehiculo.enums.EstadoVehiculo;
import com.concesionaria.vehiculo.vehiculo.enums.OrigenVehiculo;
import com.concesionaria.vehiculo.vehiculo.repository.VehiculoRepository;
import com.concesionaria.vehiculo.vehiculo.repository.VehiculoSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.Set;

/** RF-08, RF-09, RF-11, RF-12, RF-13 */
@Service
@RequiredArgsConstructor
public class VehiculoService {

    /** Campos por los que se puede ordenar el catálogo (?sort=precio,asc). */
    private static final Set<String> ORDEN_PERMITIDO = Set.of("precio", "anio", "kilometraje", "fechaIngreso");

    private final VehiculoRepository vehiculoRepository;
    private final ModeloService modeloService;
    private final ApplicationEventPublisher eventos;

    /** RF-12. El público ve solo activos y disponibles; los empleados pueden filtrar por estado. */
    @Transactional(readOnly = true)
    public Page<VehiculoResumenResponse> buscar(FiltroVehiculos filtro, Pageable pageable, boolean esEmpleado) {
        validarFiltro(filtro, pageable);
        return vehiculoRepository.findAll(VehiculoSpecifications.conFiltros(filtro, esEmpleado), pageable)
                .map(VehiculoResumenResponse::desde);
    }

    /** RF-13. Un vehículo dado de baja solo lo ven los empleados. */
    @Transactional(readOnly = true)
    public VehiculoResponse obtener(Long id, boolean esEmpleado) {
        Vehiculo vehiculo = buscarDetalle(id);
        if (!vehiculo.isActivo() && !esEmpleado) {
            throw new NoEncontradoException("Vehículo " + id + " no encontrado");
        }
        return VehiculoResponse.desde(vehiculo);
    }

    /** RF-08 (y RF-24 cuando venta-service manda origen TRADE_IN). */
    @Transactional
    public VehiculoResponse crear(CrearVehiculoRequest request) {
        String vin = request.vin().toUpperCase();
        if (vehiculoRepository.existsByVin(vin)) {
            throw new ConflictoException("Ya existe un vehículo con el VIN " + vin);
        }
        OrigenVehiculo origen = request.origen() != null ? request.origen() : OrigenVehiculo.COMPRA;
        if (origen == OrigenVehiculo.TRADE_IN && request.condicion() != CondicionVehiculo.USADO) {
            throw new SolicitudInvalidaException("Un vehículo recibido como parte de pago tiene que ser USADO");
        }
        validarAnio(request.anio());
        Modelo modelo = modeloService.buscarActivo(request.modeloId());

        Vehiculo vehiculo = vehiculoRepository.save(Vehiculo.builder()
                .vin(vin)
                .modelo(modelo)
                .anio(request.anio())
                .color(request.color().trim())
                .kilometraje(request.kilometraje())
                .precio(request.precio())
                .condicion(request.condicion())
                .origen(origen)
                .descripcion(request.descripcion())
                .build());

        eventos.publishEvent(VehiculoEvento.de(vehiculo, VehiculoEvento.Tipo.CREADO));
        return VehiculoResponse.desde(vehiculo);
    }

    /** RF-09 */
    @Transactional
    public VehiculoResponse actualizar(Long id, ActualizarVehiculoRequest request) {
        Vehiculo vehiculo = buscarDetalle(id);
        String vin = request.vin().toUpperCase();
        if (vehiculoRepository.existsByVinAndIdNot(vin, id)) {
            throw new ConflictoException("Ya existe un vehículo con el VIN " + vin);
        }
        validarAnio(request.anio());
        if (!vehiculo.getModelo().getId().equals(request.modeloId())) {
            vehiculo.setModelo(modeloService.buscarActivo(request.modeloId()));
        }
        vehiculo.setVin(vin);
        vehiculo.setAnio(request.anio());
        vehiculo.setColor(request.color().trim());
        vehiculo.setKilometraje(request.kilometraje());
        vehiculo.setPrecio(request.precio());
        vehiculo.setCondicion(request.condicion());
        vehiculo.setDescripcion(request.descripcion());

        eventos.publishEvent(VehiculoEvento.de(vehiculo, VehiculoEvento.Tipo.ACTUALIZADO));
        return VehiculoResponse.desde(vehiculo);
    }

    /**
     * RF-11. Lo usan los vendedores y venta-service al reservar, vender o anular.
     * Si dos pedidos cambian el mismo vehículo a la vez, el segundo falla con 409 (@Version).
     */
    @Transactional
    public VehiculoResponse cambiarEstado(Long id, EstadoVehiculo nuevoEstado, EstadoVehiculo estadoEsperado,
                                          UsuarioActual usuario) {
        Vehiculo vehiculo = buscarDetalle(id);
        if (!vehiculo.isActivo()) {
            throw new ConflictoException("El vehículo está dado de baja");
        }
        EstadoVehiculo actual = vehiculo.getEstado();
        if (estadoEsperado != null && actual != estadoEsperado) {
            throw new ConflictoException("El vehículo está " + actual);
        }
        if (actual == nuevoEstado) {
            return VehiculoResponse.desde(vehiculo);
        }
        if (!actual.puedePasarA(nuevoEstado)) {
            throw new ConflictoException("No se puede pasar de " + actual + " a " + nuevoEstado);
        }
        // SISTEMA: venta-service, cuando un ADMIN anula la venta (RF-26)
        if (actual == EstadoVehiculo.VENDIDO && !usuario.esAdmin() && !usuario.esSistema()) {
            throw new AccesoDenegadoException("Solo un ADMIN puede volver a poner disponible un vehículo vendido");
        }
        vehiculo.setEstado(nuevoEstado);

        eventos.publishEvent(VehiculoEvento.de(vehiculo, VehiculoEvento.Tipo.ESTADO_CAMBIADO));
        return VehiculoResponse.desde(vehiculo);
    }

    /** RF-09: baja lógica. No se puede dar de baja un vehículo reservado. */
    @Transactional
    public void darDeBaja(Long id) {
        Vehiculo vehiculo = buscarDetalle(id);
        if (!vehiculo.isActivo()) {
            return;
        }
        if (vehiculo.getEstado() == EstadoVehiculo.RESERVADO) {
            throw new ConflictoException("El vehículo tiene una reserva activa; liberala antes de darlo de baja");
        }
        vehiculo.setActivo(false);
        vehiculo.setFechaBaja(LocalDateTime.now());
        eventos.publishEvent(VehiculoEvento.de(vehiculo, VehiculoEvento.Tipo.BAJA));
    }

    @Transactional
    public VehiculoResponse reactivar(Long id) {
        Vehiculo vehiculo = buscarDetalle(id);
        if (!vehiculo.isActivo()) {
            vehiculo.setActivo(true);
            vehiculo.setFechaBaja(null);
            eventos.publishEvent(VehiculoEvento.de(vehiculo, VehiculoEvento.Tipo.REACTIVADO));
        }
        return VehiculoResponse.desde(vehiculo);
    }

    /** Para el servicio de imágenes. */
    @Transactional(readOnly = true)
    public Vehiculo buscarEntidad(Long id) {
        return buscarDetalle(id);
    }

    private Vehiculo buscarDetalle(Long id) {
        return vehiculoRepository.findDetalleById(id)
                .orElseThrow(() -> new NoEncontradoException("Vehículo " + id + " no encontrado"));
    }

    private void validarAnio(int anio) {
        int maximo = Year.now().getValue() + 1;
        if (anio > maximo) {
            throw new SolicitudInvalidaException("El año no puede ser mayor a " + maximo);
        }
    }

    private void validarFiltro(FiltroVehiculos f, Pageable pageable) {
        if (f.precioMin() != null && f.precioMax() != null && f.precioMin().compareTo(f.precioMax()) > 0) {
            throw new SolicitudInvalidaException("precioMin no puede ser mayor que precioMax");
        }
        if (f.anioMin() != null && f.anioMax() != null && f.anioMin() > f.anioMax()) {
            throw new SolicitudInvalidaException("anioMin no puede ser mayor que anioMax");
        }
        for (Sort.Order orden : pageable.getSort()) {
            if (!ORDEN_PERMITIDO.contains(orden.getProperty())) {
                throw new SolicitudInvalidaException("No se puede ordenar por '" + orden.getProperty()
                        + "'. Opciones: " + String.join(", ", ORDEN_PERMITIDO));
            }
        }
    }
}
