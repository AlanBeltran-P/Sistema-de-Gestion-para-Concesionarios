package com.concesionaria.venta.venta.service;

import com.concesionaria.common.exception.AccesoDenegadoException;
import com.concesionaria.common.exception.ConflictoException;
import com.concesionaria.common.exception.NoEncontradoException;
import com.concesionaria.common.exception.SolicitudInvalidaException;
import com.concesionaria.common.security.UsuarioActual;
import com.concesionaria.common.transaccion.AlHacerRollback;
import com.concesionaria.venta.cliente.ClienteClient;
import com.concesionaria.venta.cliente.dto.ClienteRemoto;
import com.concesionaria.venta.comision.enums.EstadoComision;
import com.concesionaria.venta.comision.service.ComisionService;
import com.concesionaria.venta.config.VentasProperties;
import com.concesionaria.venta.cotizacion.Cotizacion;
import com.concesionaria.venta.cotizacion.enums.EstadoCotizacion;
import com.concesionaria.venta.cotizacion.service.CotizacionService;
import com.concesionaria.venta.evento.VentaEvento;
import com.concesionaria.venta.plan.PlanClient;
import com.concesionaria.venta.plan.dto.SimulacionRemota;
import com.concesionaria.venta.plan.dto.SimulacionRemotaRequest;
import com.concesionaria.venta.reserva.Reserva;
import com.concesionaria.venta.reserva.enums.EstadoReserva;
import com.concesionaria.venta.reserva.repository.ReservaRepository;
import com.concesionaria.venta.reserva.service.ReservaService;
import com.concesionaria.venta.tasacion.TasacionTradeIn;
import com.concesionaria.venta.tasacion.enums.EstadoTasacion;
import com.concesionaria.venta.tasacion.service.TasacionService;
import com.concesionaria.venta.vehiculo.VehiculoClient;
import com.concesionaria.venta.vehiculo.dto.CrearVehiculoRemotoRequest;
import com.concesionaria.venta.vehiculo.dto.EstadoVehiculo;
import com.concesionaria.venta.vehiculo.dto.VehiculoRemoto;
import com.concesionaria.venta.venta.Venta;
import com.concesionaria.venta.venta.dto.RegistrarVentaRequest;
import com.concesionaria.venta.venta.dto.VentaResponse;
import com.concesionaria.venta.venta.enums.EstadoVenta;
import com.concesionaria.venta.venta.enums.FormaPago;
import com.concesionaria.venta.venta.repository.VentaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * RF-23 a RF-26.
 * Primero se valida todo localmente y recién después se tocan los otros servicios
 * (alta del trade-in, vehículo a VENDIDO). Si la transacción falla después de eso,
 * AlHacerRollback deshace los cambios remotos.
 */
@Service
@RequiredArgsConstructor
public class VentaService {

    private final VentaRepository ventaRepository;
    private final ReservaRepository reservaRepository;
    private final ReservaService reservaService;
    private final CotizacionService cotizacionService;
    private final TasacionService tasacionService;
    private final ComisionService comisionService;
    private final ReglasPrecio reglasPrecio;
    private final ClienteClient clienteClient;
    private final VehiculoClient vehiculoClient;
    private final PlanClient planClient;
    private final VentasProperties propiedades;
    private final ApplicationEventPublisher eventos;

    /** RF-23: venta de contado o financiada, con o sin trade-in. */
    @Transactional
    public VentaResponse registrar(RegistrarVentaRequest request, UsuarioActual vendedor) {
        ClienteRemoto cliente = clienteClient.obtener(request.clienteId());
        if (cliente.numeroDocumento() == null) {
            throw new SolicitudInvalidaException("El cliente no tiene documento cargado; completalo antes de "
                    + "registrar la venta (va en el contrato)");
        }
        VehiculoRemoto vehiculo = vehiculoClient.obtener(request.vehiculoId());
        if (!vehiculo.activo()) {
            throw new ConflictoException("El vehículo está dado de baja");
        }
        if (ventaRepository.existsByVehiculoIdAndEstado(vehiculo.id(), EstadoVenta.CONFIRMADA)) {
            throw new ConflictoException("El vehículo ya fue vendido");
        }

        Reserva reserva = resolverReserva(request, cliente.id(), vehiculo.id());
        EstadoVehiculo estadoEsperado = reserva != null ? EstadoVehiculo.RESERVADO : EstadoVehiculo.DISPONIBLE;
        if (vehiculo.estado() != estadoEsperado) {
            throw new ConflictoException("El vehículo está " + vehiculo.estado());
        }

        // ---- Precio, descuento y trade-in
        Cotizacion cotizacion = null;
        BigDecimal precio;
        BigDecimal descuento;
        TasacionTradeIn tasacion;
        if (request.cotizacionId() != null) {
            if (request.descuento() != null || request.tasacionId() != null) {
                throw new SolicitudInvalidaException("Con cotización, el descuento y el trade-in salen de ella: "
                        + "no mandes descuento ni tasacionId");
            }
            cotizacion = cotizacionService.buscarVigente(request.cotizacionId(), cliente.id(), vehiculo.id());
            precio = cotizacion.getPrecioLista();
            descuento = cotizacion.getDescuento();
            tasacion = cotizacion.getTasacion();
            if (tasacion != null) {
                TasacionService.validarUtilizable(tasacion, cliente.id());
            }
        } else {
            precio = vehiculo.precio();
            descuento = request.descuento() != null ? request.descuento() : BigDecimal.ZERO;
            reglasPrecio.validarDescuento(descuento, precio, vendedor);
            tasacion = request.tasacionId() != null
                    ? tasacionService.buscarUtilizable(request.tasacionId(), cliente.id())
                    : null;
        }
        BigDecimal precioFinal = precio.subtract(descuento);
        BigDecimal valorTradeIn = tasacion != null ? tasacion.getValorTasado() : BigDecimal.ZERO;
        BigDecimal sena = reserva != null ? reserva.getMontoAnticipo() : BigDecimal.ZERO;
        BigDecimal saldo = precioFinal.subtract(valorTradeIn).subtract(sena);
        if (saldo.signum() < 0) {
            throw new SolicitudInvalidaException("El trade-in y la seña superan el precio final");
        }

        // ---- Financiación (financiamiento-service valida plan, plazo y anticipo mínimo)
        SimulacionRemota simulacion = validarFinanciacion(request, precioFinal, saldo);

        // ---- Guardar
        Venta venta = ventaRepository.saveAndFlush(Venta.builder()
                .numeroComprobante("TMP-" + UUID.randomUUID().toString().substring(0, 20))
                .clienteId(cliente.id())
                .clienteNombre(cliente.nombreCompleto())
                .vehiculoId(vehiculo.id())
                .vehiculoDescripcion(vehiculo.descripcion())
                .vendedorId(vendedor.id())
                .vendedorNombre(vendedor.nombre())
                .cotizacion(cotizacion)
                .reserva(reserva)
                .tasacion(tasacion)
                .formaPago(request.formaPago())
                .precioVehiculo(precio)
                .descuento(descuento)
                .precioFinal(precioFinal)
                .valorTradeIn(valorTradeIn)
                .montoAnticipoReserva(sena)
                .saldoAPagar(saldo)
                .planFinanciamientoId(simulacion != null ? simulacion.planId() : null)
                .planNombre(simulacion != null ? simulacion.plan() : null)
                .cantidadCuotas(simulacion != null ? simulacion.cantidadCuotas() : null)
                .montoFinanciado(simulacion != null ? simulacion.montoFinanciado() : null)
                .valorCuota(simulacion != null ? simulacion.valorCuota() : null)
                .build());
        venta.setNumeroComprobante("V-%d-%06d".formatted(LocalDate.now().getYear(), venta.getId()));
        venta.asignarComision(comisionService.calcular(venta, vehiculo.condicion()));
        if (reserva != null) {
            reserva.setEstado(EstadoReserva.CONVERTIDA);
        }
        if (cotizacion != null) {
            cotizacion.setEstado(EstadoCotizacion.ACEPTADA);
        }

        // ---- Otros servicios (con compensación si después falla el commit)
        if (tasacion != null) {
            ingresarTradeIn(tasacion, venta);
        }
        Long vehiculoId = vehiculo.id();
        vehiculoClient.cambiarEstado(vehiculoId, EstadoVehiculo.VENDIDO, estadoEsperado);
        AlHacerRollback.ejecutar("devolver vehículo " + vehiculoId + " a " + estadoEsperado, () -> {
            vehiculoClient.cambiarEstado(vehiculoId, EstadoVehiculo.DISPONIBLE, EstadoVehiculo.VENDIDO);
            if (estadoEsperado == EstadoVehiculo.RESERVADO) {
                vehiculoClient.cambiarEstado(vehiculoId, EstadoVehiculo.RESERVADO, EstadoVehiculo.DISPONIBLE);
            }
        });
        ventaRepository.saveAndFlush(venta);

        eventos.publishEvent(VentaEvento.confirmada(venta, cliente.email(), datos(vehiculo)));
        return VentaResponse.desde(venta, true);
    }

    /**
     * RF-26: el ADMIN anula una venta. El vehículo vuelve a estar disponible, la comisión se anula
     * y el auto del trade-in sale del inventario (se le devuelve al cliente). Si ese auto ya se
     * reservó o vendió a otra persona, no se puede anular.
     */
    @Transactional
    public VentaResponse anular(Long id, String motivo, UsuarioActual admin) {
        Venta venta = buscar(id);
        if (venta.getEstado() == EstadoVenta.ANULADA) {
            throw new ConflictoException("La venta ya está anulada");
        }
        TasacionTradeIn tasacion = venta.getTasacion();
        VehiculoRemoto tradeIn = null;
        if (tasacion != null && tasacion.getVehiculoIngresadoId() != null) {
            tradeIn = vehiculoClient.obtener(tasacion.getVehiculoIngresadoId());
            if (tradeIn.activo() && tradeIn.estado() != EstadoVehiculo.DISPONIBLE) {
                throw new ConflictoException("El auto recibido como parte de pago ya está " + tradeIn.estado()
                        + "; no se puede anular la venta");
            }
        }
        VehiculoRemoto vehiculo = vehiculoClient.obtener(venta.getVehiculoId());

        venta.setEstado(EstadoVenta.ANULADA);
        venta.setMotivoAnulacion(motivo.trim());
        venta.setFechaAnulacion(LocalDateTime.now());
        venta.setAnuladaPorId(admin.id());
        if (venta.getComision() != null) {
            venta.getComision().setEstado(EstadoComision.ANULADA);
        }
        if (venta.getReserva() != null) {
            venta.getReserva().setEstado(EstadoReserva.ANULADA);
            venta.getReserva().setMotivoAnulacion("Venta " + venta.getNumeroComprobante() + " anulada");
            venta.getReserva().setFechaAnulacion(LocalDateTime.now());
        }
        if (tasacion != null) {
            tasacion.setEstado(EstadoTasacion.ACEPTADA);
            tasacion.setVehiculoIngresadoId(null);
        }
        ventaRepository.saveAndFlush(venta);

        Long vehiculoId = venta.getVehiculoId();
        if (vehiculo.activo() && vehiculo.estado() == EstadoVehiculo.VENDIDO) {
            vehiculoClient.cambiarEstado(vehiculoId, EstadoVehiculo.DISPONIBLE, EstadoVehiculo.VENDIDO);
            AlHacerRollback.ejecutar("volver a marcar vendido el vehículo " + vehiculoId,
                    () -> vehiculoClient.cambiarEstado(vehiculoId, EstadoVehiculo.VENDIDO, EstadoVehiculo.DISPONIBLE));
        }
        if (tradeIn != null && tradeIn.activo()) {
            Long tradeInId = tradeIn.id();
            vehiculoClient.darDeBaja(tradeInId);
            AlHacerRollback.ejecutar("reactivar trade-in " + tradeInId, () -> vehiculoClient.reactivar(tradeInId));
        }

        eventos.publishEvent(VentaEvento.anulada(venta, datos(vehiculo)));
        return VentaResponse.desde(venta, true);
    }

    @Transactional(readOnly = true)
    public VentaResponse obtener(Long id, UsuarioActual usuario) {
        Venta venta = buscar(id);
        validarAcceso(venta, usuario);
        return VentaResponse.desde(venta, !usuario.esCliente());
    }

    @Transactional(readOnly = true)
    public Page<VentaResponse> buscar(Long clienteId, Long vendedorId, EstadoVenta estado, LocalDate desde,
                                      LocalDate hasta, Pageable pageable) {
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            throw new SolicitudInvalidaException("'desde' no puede ser posterior a 'hasta'");
        }
        return ventaRepository.buscar(clienteId, vendedorId, estado,
                        desde != null ? desde.atStartOfDay() : null,
                        hasta != null ? hasta.plusDays(1).atStartOfDay() : null,
                        pageable)
                .map(v -> VentaResponse.desde(v, true));
    }

    /** RF-17: las compras del cliente logueado. */
    @Transactional(readOnly = true)
    public Page<VentaResponse> delCliente(UsuarioActual usuario, Pageable pageable) {
        Long clienteId = clienteClient.delUsuario(usuario).id();
        return ventaRepository.buscar(clienteId, null, null, null, null, pageable)
                .map(v -> VentaResponse.desde(v, false));
    }

    /** Para el comprobante PDF. */
    @Transactional(readOnly = true)
    public Venta buscarParaComprobante(Long id, UsuarioActual usuario) {
        Venta venta = buscar(id);
        validarAcceso(venta, usuario);
        // Inicializa las relaciones que usa el PDF dentro de la transacción
        if (venta.getTasacion() != null) {
            venta.getTasacion().getVin();
        }
        return venta;
    }

    // ---------------------------------------------------------------- Auxiliares

    /**
     * La reserva indicada o, si no se indicó, la reserva activa del cliente sobre el vehículo.
     * Si el vehículo está reservado por otra persona no se puede vender.
     */
    private Reserva resolverReserva(RegistrarVentaRequest request, Long clienteId, Long vehiculoId) {
        if (request.reservaId() != null) {
            Reserva reserva = reservaService.buscar(request.reservaId());
            if (reserva.getEstado() != EstadoReserva.ACTIVA) {
                throw new ConflictoException("La reserva " + reserva.getId() + " está " + reserva.getEstado());
            }
            if (!reserva.getClienteId().equals(clienteId) || !reserva.getVehiculoId().equals(vehiculoId)) {
                throw new SolicitudInvalidaException("La reserva " + reserva.getId() + " es de otro cliente o vehículo");
            }
            return reserva;
        }
        return reservaRepository.findFirstByVehiculoIdAndEstado(vehiculoId, EstadoReserva.ACTIVA)
                .map(activa -> {
                    if (!activa.getClienteId().equals(clienteId)) {
                        throw new ConflictoException("El vehículo está reservado por otro cliente (reserva "
                                + activa.getId() + ")");
                    }
                    return activa;
                })
                .orElse(null);
    }

    private SimulacionRemota validarFinanciacion(RegistrarVentaRequest r, BigDecimal precioFinal, BigDecimal saldo) {
        if (r.formaPago() == FormaPago.CONTADO) {
            if (r.planFinanciamientoId() != null || r.cantidadCuotas() != null || r.montoFinanciado() != null) {
                throw new SolicitudInvalidaException("Una venta de CONTADO no lleva plan, cuotas ni monto financiado");
            }
            return null;
        }
        if (r.planFinanciamientoId() == null || r.cantidadCuotas() == null || r.montoFinanciado() == null) {
            throw new SolicitudInvalidaException("Una venta FINANCIADA necesita planFinanciamientoId, cantidadCuotas "
                    + "y montoFinanciado");
        }
        if (r.montoFinanciado().compareTo(saldo) > 0) {
            throw new SolicitudInvalidaException("No se puede financiar más que el saldo a pagar ($"
                    + saldo.toPlainString() + ")");
        }
        BigDecimal anticipo = precioFinal.subtract(r.montoFinanciado());
        return planClient.simular(new SimulacionRemotaRequest(
                r.planFinanciamientoId(), precioFinal, anticipo, r.cantidadCuotas()));
    }

    /** RF-24: el auto usado entra al inventario con origen TRADE_IN, a valor tasado + margen. */
    private void ingresarTradeIn(TasacionTradeIn tasacion, Venta venta) {
        BigDecimal precioReventa = tasacion.getValorTasado()
                .add(ReglasPrecio.porcentaje(tasacion.getValorTasado(), propiedades.margenReventaTradeIn()));
        VehiculoRemoto ingresado = vehiculoClient.crear(new CrearVehiculoRemotoRequest(
                tasacion.getVin(),
                tasacion.getModeloId(),
                tasacion.getAnio(),
                tasacion.getColor(),
                tasacion.getKilometraje(),
                precioReventa,
                "USADO",
                "TRADE_IN",
                "Recibido como parte de pago en la venta " + venta.getNumeroComprobante()
                        + (tasacion.getObservaciones() != null ? ". " + tasacion.getObservaciones() : "")));
        Long ingresadoId = ingresado.id();
        AlHacerRollback.ejecutar("dar de baja trade-in " + ingresadoId, () -> vehiculoClient.darDeBaja(ingresadoId));
        tasacion.setEstado(EstadoTasacion.UTILIZADA);
        tasacion.setVehiculoIngresadoId(ingresadoId);
    }

    private void validarAcceso(Venta venta, UsuarioActual usuario) {
        if (usuario.esCliente() && !venta.getClienteId().equals(clienteClient.delUsuario(usuario).id())) {
            throw new AccesoDenegadoException("Esa venta no es tuya");
        }
    }

    private static VentaEvento.DatosVehiculo datos(VehiculoRemoto v) {
        return new VentaEvento.DatosVehiculo(v.modeloId(), v.marca(), v.modelo(), v.categoria(), v.anio(), v.condicion());
    }

    private Venta buscar(Long id) {
        return ventaRepository.findDetalleById(id)
                .orElseThrow(() -> new NoEncontradoException("Venta " + id + " no encontrada"));
    }
}
