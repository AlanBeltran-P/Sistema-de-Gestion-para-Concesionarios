package com.concesionaria.venta.cotizacion.service;

import com.concesionaria.common.exception.AccesoDenegadoException;
import com.concesionaria.common.exception.ConflictoException;
import com.concesionaria.common.exception.NoEncontradoException;
import com.concesionaria.common.exception.SolicitudInvalidaException;
import com.concesionaria.common.security.UsuarioActual;
import com.concesionaria.venta.cliente.ClienteClient;
import com.concesionaria.venta.cliente.dto.ClienteRemoto;
import com.concesionaria.venta.config.VentasProperties;
import com.concesionaria.venta.cotizacion.Cotizacion;
import com.concesionaria.venta.cotizacion.dto.CotizacionResponse;
import com.concesionaria.venta.cotizacion.dto.CrearCotizacionRequest;
import com.concesionaria.venta.cotizacion.dto.ResponderCotizacionRequest;
import com.concesionaria.venta.cotizacion.dto.SolicitarCotizacionRequest;
import com.concesionaria.venta.cotizacion.enums.EstadoCotizacion;
import com.concesionaria.venta.cotizacion.repository.CotizacionRepository;
import com.concesionaria.venta.evento.CotizacionEvento;
import com.concesionaria.venta.tasacion.TasacionTradeIn;
import com.concesionaria.venta.tasacion.service.TasacionService;
import com.concesionaria.venta.vehiculo.VehiculoClient;
import com.concesionaria.venta.vehiculo.dto.EstadoVehiculo;
import com.concesionaria.venta.vehiculo.dto.VehiculoRemoto;
import com.concesionaria.venta.venta.service.ReglasPrecio;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * RF-18 y RF-19.
 * SOLICITADA (el cliente la pide) -> RESPONDIDA (el vendedor pone precio) -> ACEPTADA / RECHAZADA,
 * o VENCIDA si pasa la fecha de validez sin respuesta del cliente.
 */
@Service
@RequiredArgsConstructor
public class CotizacionService {

    private static final List<EstadoCotizacion> ABIERTAS = List.of(EstadoCotizacion.SOLICITADA, EstadoCotizacion.RESPONDIDA);

    private final CotizacionRepository cotizacionRepository;
    private final TasacionService tasacionService;
    private final ClienteClient clienteClient;
    private final VehiculoClient vehiculoClient;
    private final ReglasPrecio reglasPrecio;
    private final VentasProperties propiedades;
    private final ApplicationEventPublisher eventos;

    /** RF-18: pedido desde el portal. Queda sin vendedor hasta que alguno la responda. */
    @Transactional
    public CotizacionResponse solicitar(SolicitarCotizacionRequest request, UsuarioActual usuario) {
        ClienteRemoto cliente = clienteClient.delUsuario(usuario);
        VehiculoRemoto vehiculo = vehiculoDisponible(request.vehiculoId());
        validarSinCotizacionAbierta(cliente.id(), vehiculo.id(), "Ya tenés una cotización abierta para este vehículo");

        Cotizacion cotizacion = cotizacionRepository.save(Cotizacion.builder()
                .clienteId(cliente.id())
                .clienteNombre(cliente.nombreCompleto())
                .vehiculoId(vehiculo.id())
                .vehiculoDescripcion(vehiculo.descripcion())
                .precioLista(vehiculo.precio())
                .mensajeCliente(request.mensaje())
                .build());
        return CotizacionResponse.desde(cotizacion);
    }

    /** RF-19: el vendedor cotiza directamente a un cliente del local. */
    @Transactional
    public CotizacionResponse crear(CrearCotizacionRequest request, UsuarioActual vendedor) {
        ClienteRemoto cliente = clienteClient.obtener(request.clienteId());
        VehiculoRemoto vehiculo = vehiculoDisponible(request.vehiculoId());
        validarSinCotizacionAbierta(cliente.id(), vehiculo.id(), "El cliente ya tiene una cotización abierta para este vehículo");

        Cotizacion cotizacion = cotizacionRepository.save(Cotizacion.builder()
                .clienteId(cliente.id())
                .clienteNombre(cliente.nombreCompleto())
                .vehiculoId(vehiculo.id())
                .vehiculoDescripcion(vehiculo.descripcion())
                .precioLista(vehiculo.precio())
                .build());
        aplicarRespuesta(cotizacion, request.comoRespuesta(), vehiculo, vendedor, cliente);
        return CotizacionResponse.desde(cotizacion);
    }

    /** RF-19: responder (o corregir la respuesta) con precio, descuento y trade-in. */
    @Transactional
    public CotizacionResponse responder(Long id, ResponderCotizacionRequest request, UsuarioActual vendedor) {
        Cotizacion cotizacion = buscar(id);
        if (!ABIERTAS.contains(cotizacion.getEstado())) {
            throw new ConflictoException("La cotización está " + cotizacion.getEstado());
        }
        if (cotizacion.getVendedorId() != null && !cotizacion.getVendedorId().equals(vendedor.id()) && !vendedor.esAdmin()) {
            throw new AccesoDenegadoException("La cotización la atiende otro vendedor");
        }
        VehiculoRemoto vehiculo = vehiculoClient.obtener(cotizacion.getVehiculoId());
        if (!vehiculo.activo() || vehiculo.estado() == EstadoVehiculo.VENDIDO) {
            throw new ConflictoException("El vehículo ya no está a la venta");
        }
        ClienteRemoto cliente = clienteClient.obtener(cotizacion.getClienteId());
        aplicarRespuesta(cotizacion, request, vehiculo, vendedor, cliente);
        return CotizacionResponse.desde(cotizacion);
    }

    /** El cliente (o el vendedor en su nombre) acepta el precio. */
    @Transactional
    public CotizacionResponse aceptar(Long id, UsuarioActual usuario) {
        return decidir(id, usuario, EstadoCotizacion.ACEPTADA);
    }

    @Transactional
    public CotizacionResponse rechazar(Long id, UsuarioActual usuario) {
        return decidir(id, usuario, EstadoCotizacion.RECHAZADA);
    }

    @Transactional(readOnly = true)
    public CotizacionResponse obtener(Long id, UsuarioActual usuario) {
        Cotizacion cotizacion = buscar(id);
        validarAcceso(cotizacion, usuario);
        return CotizacionResponse.desde(cotizacion);
    }

    @Transactional(readOnly = true)
    public Page<CotizacionResponse> buscar(EstadoCotizacion estado, Long clienteId, Long vendedorId, boolean sinAsignar,
                                           Pageable pageable) {
        return cotizacionRepository.buscar(estado, clienteId, vendedorId, sinAsignar, pageable)
                .map(CotizacionResponse::desde);
    }

    @Transactional(readOnly = true)
    public Page<CotizacionResponse> delCliente(UsuarioActual usuario, Pageable pageable) {
        Long clienteId = clienteClient.delUsuario(usuario).id();
        return cotizacionRepository.buscar(null, clienteId, null, false, pageable).map(CotizacionResponse::desde);
    }

    /** Para reservas y ventas: la cotización del cliente sobre ese vehículo, vigente. */
    @Transactional(readOnly = true)
    public Cotizacion buscarVigente(Long id, Long clienteId, Long vehiculoId) {
        Cotizacion cotizacion = buscar(id);
        if (!cotizacion.getClienteId().equals(clienteId) || !cotizacion.getVehiculoId().equals(vehiculoId)) {
            throw new SolicitudInvalidaException("La cotización " + id + " es de otro cliente o de otro vehículo");
        }
        if (cotizacion.getEstado() != EstadoCotizacion.RESPONDIDA && cotizacion.getEstado() != EstadoCotizacion.ACEPTADA) {
            throw new ConflictoException("La cotización " + id + " está " + cotizacion.getEstado());
        }
        if (cotizacion.getFechaValidez().isBefore(LocalDate.now())) {
            throw new ConflictoException("La cotización " + id + " venció el " + cotizacion.getFechaValidez());
        }
        return cotizacion;
    }

    /** Job (RF-22): las respondidas que pasaron la fecha de validez. */
    @Transactional
    public int vencer(LocalDate hoy) {
        return cotizacionRepository.vencerRespondidas(hoy);
    }

    private void aplicarRespuesta(Cotizacion cotizacion, ResponderCotizacionRequest request, VehiculoRemoto vehiculo,
                                  UsuarioActual vendedor, ClienteRemoto cliente) {
        BigDecimal precio = vehiculo.precio();
        BigDecimal descuento = request.descuento() != null ? request.descuento() : BigDecimal.ZERO;
        reglasPrecio.validarDescuento(descuento, precio, vendedor);

        TasacionTradeIn tasacion = request.tasacionId() != null
                ? tasacionService.buscarUtilizable(request.tasacionId(), cotizacion.getClienteId())
                : null;
        BigDecimal valorTradeIn = tasacion != null ? tasacion.getValorTasado() : BigDecimal.ZERO;
        BigDecimal precioFinal = precio.subtract(descuento).subtract(valorTradeIn);
        if (precioFinal.signum() < 0) {
            throw new SolicitudInvalidaException("El valor del trade-in supera el precio con descuento");
        }
        int dias = request.diasValidez() != null ? request.diasValidez() : propiedades.cotizacion().diasValidez();

        if (cotizacion.getVendedorId() == null) {
            cotizacion.setVendedorId(vendedor.id());
            cotizacion.setVendedorNombre(vendedor.nombre());
        }
        cotizacion.setPrecioLista(precio);
        cotizacion.setDescuento(descuento);
        cotizacion.setTasacion(tasacion);
        cotizacion.setValorTradeIn(valorTradeIn);
        cotizacion.setPrecioFinal(precioFinal);
        cotizacion.setRespuestaVendedor(request.respuesta().trim());
        cotizacion.setFechaRespuesta(LocalDateTime.now());
        cotizacion.setFechaValidez(LocalDate.now().plusDays(dias));
        cotizacion.setEstado(EstadoCotizacion.RESPONDIDA);
        cotizacionRepository.saveAndFlush(cotizacion);

        eventos.publishEvent(CotizacionEvento.respondida(cotizacion, cliente.email()));
    }

    private CotizacionResponse decidir(Long id, UsuarioActual usuario, EstadoCotizacion decision) {
        Cotizacion cotizacion = buscar(id);
        validarAcceso(cotizacion, usuario);
        if (cotizacion.getEstado() != EstadoCotizacion.RESPONDIDA) {
            throw new ConflictoException("Solo se puede aceptar o rechazar una cotización RESPONDIDA (está "
                    + cotizacion.getEstado() + ")");
        }
        if (decision == EstadoCotizacion.ACEPTADA && cotizacion.getFechaValidez().isBefore(LocalDate.now())) {
            throw new ConflictoException("La cotización venció el " + cotizacion.getFechaValidez() + "; pedí una nueva");
        }
        cotizacion.setEstado(decision);
        return CotizacionResponse.desde(cotizacion);
    }

    /** Un cliente solo ve y decide sobre sus propias cotizaciones. */
    private void validarAcceso(Cotizacion cotizacion, UsuarioActual usuario) {
        if (usuario.esCliente() && !cotizacion.getClienteId().equals(clienteClient.delUsuario(usuario).id())) {
            throw new AccesoDenegadoException("Esa cotización no es tuya");
        }
    }

    private VehiculoRemoto vehiculoDisponible(Long vehiculoId) {
        VehiculoRemoto vehiculo = vehiculoClient.obtener(vehiculoId);
        if (!vehiculo.activo() || vehiculo.estado() != EstadoVehiculo.DISPONIBLE) {
            throw new ConflictoException("El vehículo no está disponible");
        }
        return vehiculo;
    }

    private void validarSinCotizacionAbierta(Long clienteId, Long vehiculoId, String mensaje) {
        if (cotizacionRepository.existsByClienteIdAndVehiculoIdAndEstadoIn(clienteId, vehiculoId, ABIERTAS)) {
            throw new ConflictoException(mensaje);
        }
    }

    private Cotizacion buscar(Long id) {
        return cotizacionRepository.findById(id)
                .orElseThrow(() -> new NoEncontradoException("Cotización " + id + " no encontrada"));
    }
}
