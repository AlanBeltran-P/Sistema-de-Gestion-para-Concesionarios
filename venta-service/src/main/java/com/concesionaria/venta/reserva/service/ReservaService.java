package com.concesionaria.venta.reserva.service;

import com.concesionaria.common.exception.AccesoDenegadoException;
import com.concesionaria.common.exception.ConflictoException;
import com.concesionaria.common.exception.NoEncontradoException;
import com.concesionaria.common.exception.SolicitudInvalidaException;
import com.concesionaria.common.security.UsuarioActual;
import com.concesionaria.common.transaccion.AlHacerRollback;
import com.concesionaria.venta.cliente.ClienteClient;
import com.concesionaria.venta.cliente.dto.ClienteRemoto;
import com.concesionaria.venta.config.VentasProperties;
import com.concesionaria.venta.cotizacion.Cotizacion;
import com.concesionaria.venta.cotizacion.enums.EstadoCotizacion;
import com.concesionaria.venta.cotizacion.service.CotizacionService;
import com.concesionaria.venta.evento.ReservaEvento;
import com.concesionaria.venta.reserva.Reserva;
import com.concesionaria.venta.reserva.dto.CrearReservaRequest;
import com.concesionaria.venta.reserva.dto.ReservaResponse;
import com.concesionaria.venta.reserva.enums.EstadoReserva;
import com.concesionaria.venta.reserva.repository.ReservaRepository;
import com.concesionaria.venta.vehiculo.VehiculoClient;
import com.concesionaria.venta.vehiculo.dto.EstadoVehiculo;
import com.concesionaria.venta.vehiculo.dto.VehiculoRemoto;
import com.concesionaria.venta.venta.service.ReglasPrecio;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * RF-21, RF-22 y RF-26 (reservas).
 * Al reservar, el vehículo pasa a RESERVADO en vehiculo-service; si la reserva vence o se anula,
 * vuelve a DISPONIBLE. Si algo falla después de cambiar el vehículo, se deshace (AlHacerRollback).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReservaService {

    private final ReservaRepository reservaRepository;
    private final CotizacionService cotizacionService;
    private final ClienteClient clienteClient;
    private final VehiculoClient vehiculoClient;
    private final VentasProperties propiedades;
    private final ApplicationEventPublisher eventos;
    private final TransactionTemplate transactionTemplate;

    /** RF-21: reserva con seña. La pueden hacer un vendedor (para un cliente) o el propio cliente. */
    @Transactional
    public ReservaResponse crear(CrearReservaRequest request, UsuarioActual usuario) {
        ClienteRemoto cliente = resolverCliente(request.clienteId(), usuario);
        VehiculoRemoto vehiculo = vehiculoClient.obtener(request.vehiculoId());
        if (!vehiculo.activo() || vehiculo.estado() != EstadoVehiculo.DISPONIBLE) {
            throw new ConflictoException("El vehículo no está disponible para reservar");
        }
        if (reservaRepository.findFirstByVehiculoIdAndEstado(vehiculo.id(), EstadoReserva.ACTIVA).isPresent()) {
            throw new ConflictoException("El vehículo ya tiene una reserva activa");
        }

        Cotizacion cotizacion = null;
        BigDecimal precio = vehiculo.precio();
        if (request.cotizacionId() != null) {
            cotizacion = cotizacionService.buscarVigente(request.cotizacionId(), cliente.id(), vehiculo.id());
            cotizacion.setEstado(EstadoCotizacion.ACEPTADA);
            precio = cotizacion.getPrecioFinal();
        }
        validarAnticipo(request.montoAnticipo(), precio);
        int dias = diasVigencia(request.diasVigencia());

        Reserva reserva = reservaRepository.saveAndFlush(Reserva.builder()
                .clienteId(cliente.id())
                .clienteNombre(cliente.nombreCompleto())
                .vehiculoId(vehiculo.id())
                .vehiculoDescripcion(vehiculo.descripcion())
                .vendedorId(usuario.esCliente() ? null : usuario.id())
                .cotizacion(cotizacion)
                .montoAnticipo(request.montoAnticipo())
                .fechaVencimiento(LocalDateTime.now().plusDays(dias))
                .build());

        Long vehiculoId = vehiculo.id();
        vehiculoClient.cambiarEstado(vehiculoId, EstadoVehiculo.RESERVADO, EstadoVehiculo.DISPONIBLE);
        AlHacerRollback.ejecutar("liberar vehículo " + vehiculoId,
                () -> vehiculoClient.cambiarEstado(vehiculoId, EstadoVehiculo.DISPONIBLE, EstadoVehiculo.RESERVADO));

        eventos.publishEvent(ReservaEvento.creada(reserva, cliente.email()));
        return ReservaResponse.desde(reserva);
    }

    /** RF-26: el ADMIN anula una reserva activa y el vehículo vuelve a estar disponible. */
    @Transactional
    public ReservaResponse anular(Long id, String motivo, UsuarioActual admin) {
        Reserva reserva = buscar(id);
        if (reserva.getEstado() != EstadoReserva.ACTIVA) {
            throw new ConflictoException("Solo se puede anular una reserva ACTIVA (está " + reserva.getEstado() + ")");
        }
        reserva.setEstado(EstadoReserva.ANULADA);
        reserva.setMotivoAnulacion(motivo.trim());
        reserva.setFechaAnulacion(LocalDateTime.now());
        reserva.setAnuladaPorId(admin.id());
        reservaRepository.saveAndFlush(reserva);

        liberarVehiculo(reserva.getVehiculoId());
        eventos.publishEvent(ReservaEvento.anulada(reserva));
        return ReservaResponse.desde(reserva);
    }

    /**
     * RF-22: vence las reservas cuya fecha pasó. Cada una en su propia transacción: si
     * vehiculo-service no responde, esa reserva queda ACTIVA y se reintenta en la próxima pasada.
     */
    public int vencer(LocalDateTime ahora) {
        int vencidas = 0;
        for (Long id : reservaRepository.findIdsVencidas(EstadoReserva.ACTIVA, ahora)) {
            try {
                Boolean ok = transactionTemplate.execute(estado -> vencerUna(id));
                if (Boolean.TRUE.equals(ok)) {
                    vencidas++;
                }
            } catch (RuntimeException e) {
                log.warn("No se pudo vencer la reserva {}; se reintenta en la próxima pasada: {}", id, e.getMessage());
            }
        }
        return vencidas;
    }

    @Transactional(readOnly = true)
    public ReservaResponse obtener(Long id, UsuarioActual usuario) {
        Reserva reserva = buscar(id);
        if (usuario.esCliente() && !reserva.getClienteId().equals(clienteClient.delUsuario(usuario).id())) {
            throw new AccesoDenegadoException("Esa reserva no es tuya");
        }
        return ReservaResponse.desde(reserva);
    }

    @Transactional(readOnly = true)
    public Page<ReservaResponse> buscar(Long clienteId, Long vehiculoId, EstadoReserva estado, Pageable pageable) {
        return reservaRepository.buscar(clienteId, vehiculoId, estado, pageable).map(ReservaResponse::desde);
    }

    @Transactional(readOnly = true)
    public Page<ReservaResponse> delCliente(UsuarioActual usuario, Pageable pageable) {
        Long clienteId = clienteClient.delUsuario(usuario).id();
        return reservaRepository.buscar(clienteId, null, null, pageable).map(ReservaResponse::desde);
    }

    private boolean vencerUna(Long id) {
        Reserva reserva = buscar(id);
        if (reserva.getEstado() != EstadoReserva.ACTIVA) {
            return false;
        }
        reserva.setEstado(EstadoReserva.VENCIDA);
        reservaRepository.saveAndFlush(reserva);
        // Si alguien ya cambió el vehículo a mano, no se toca
        VehiculoRemoto vehiculo = vehiculoClient.obtener(reserva.getVehiculoId());
        if (vehiculo.activo() && vehiculo.estado() == EstadoVehiculo.RESERVADO) {
            liberarVehiculo(vehiculo.id());
        }
        eventos.publishEvent(ReservaEvento.vencida(reserva));
        log.info("Reserva {} vencida; vehículo {} liberado", id, reserva.getVehiculoId());
        return true;
    }

    private void liberarVehiculo(Long vehiculoId) {
        vehiculoClient.cambiarEstado(vehiculoId, EstadoVehiculo.DISPONIBLE, EstadoVehiculo.RESERVADO);
        AlHacerRollback.ejecutar("volver a reservar vehículo " + vehiculoId,
                () -> vehiculoClient.cambiarEstado(vehiculoId, EstadoVehiculo.RESERVADO, EstadoVehiculo.DISPONIBLE));
    }

    private ClienteRemoto resolverCliente(Long clienteId, UsuarioActual usuario) {
        if (usuario.esCliente()) {
            ClienteRemoto propio = clienteClient.delUsuario(usuario);
            if (clienteId != null && !clienteId.equals(propio.id())) {
                throw new AccesoDenegadoException("Solo podés reservar a tu nombre");
            }
            return propio;
        }
        if (clienteId == null) {
            throw new SolicitudInvalidaException("Indicá el cliente (clienteId)");
        }
        return clienteClient.obtener(clienteId);
    }

    private void validarAnticipo(BigDecimal monto, BigDecimal precio) {
        BigDecimal minimo = ReglasPrecio.porcentaje(precio, propiedades.reserva().anticipoMinimoPorcentaje());
        if (monto.compareTo(minimo) < 0) {
            throw new SolicitudInvalidaException("La seña mínima es $" + minimo.toPlainString() + " ("
                    + propiedades.reserva().anticipoMinimoPorcentaje().stripTrailingZeros().toPlainString()
                    + " % del precio)");
        }
        if (monto.compareTo(precio) >= 0) {
            throw new SolicitudInvalidaException("La seña no puede cubrir todo el precio: registrá directamente la venta");
        }
    }

    private int diasVigencia(Integer pedidos) {
        var config = propiedades.reserva();
        int dias = pedidos != null ? pedidos : config.diasVigencia();
        if (dias > config.diasVigenciaMaximo()) {
            throw new SolicitudInvalidaException("Una reserva puede durar hasta " + config.diasVigenciaMaximo() + " días");
        }
        return dias;
    }

    public Reserva buscar(Long id) {
        return reservaRepository.findById(id)
                .orElseThrow(() -> new NoEncontradoException("Reserva " + id + " no encontrada"));
    }
}
