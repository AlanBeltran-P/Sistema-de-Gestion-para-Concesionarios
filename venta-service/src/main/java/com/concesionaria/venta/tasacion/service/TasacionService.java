package com.concesionaria.venta.tasacion.service;

import com.concesionaria.common.exception.ConflictoException;
import com.concesionaria.common.exception.NoEncontradoException;
import com.concesionaria.common.exception.SolicitudInvalidaException;
import com.concesionaria.common.security.UsuarioActual;
import com.concesionaria.venta.cliente.ClienteClient;
import com.concesionaria.venta.config.VentasProperties;
import com.concesionaria.venta.tasacion.TasacionTradeIn;
import com.concesionaria.venta.tasacion.dto.TasacionRequest;
import com.concesionaria.venta.tasacion.dto.TasacionResponse;
import com.concesionaria.venta.tasacion.enums.EstadoTasacion;
import com.concesionaria.venta.tasacion.repository.TasacionRepository;
import com.concesionaria.venta.vehiculo.VehiculoClient;
import com.concesionaria.venta.vehiculo.dto.ModeloRemoto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Year;

/**
 * RF-20. El vendedor tasa el auto usado del cliente; queda PENDIENTE hasta que el cliente
 * acepta o rechaza el valor. Una tasación vigente se puede usar en una cotización o en una venta.
 */
@Service
@RequiredArgsConstructor
public class TasacionService {

    private final TasacionRepository tasacionRepository;
    private final ClienteClient clienteClient;
    private final VehiculoClient vehiculoClient;
    private final VentasProperties propiedades;

    @Transactional
    public TasacionResponse crear(TasacionRequest request, UsuarioActual vendedor) {
        clienteClient.obtener(request.clienteId());
        ModeloRemoto modelo = vehiculoClient.obtenerModelo(request.modeloId());
        int maximo = Year.now().getValue() + 1;
        if (request.anio() > maximo) {
            throw new SolicitudInvalidaException("El año no puede ser mayor a " + maximo);
        }
        int dias = request.diasValidez() != null ? request.diasValidez() : propiedades.tasacion().diasValidez();

        TasacionTradeIn tasacion = tasacionRepository.save(TasacionTradeIn.builder()
                .clienteId(request.clienteId())
                .vendedorId(vendedor.id())
                .vin(request.vin().toUpperCase())
                .modeloId(modelo.id())
                .modeloDescripcion(modelo.descripcion())
                .anio(request.anio())
                .color(request.color().trim())
                .kilometraje(request.kilometraje())
                .observaciones(request.observaciones())
                .valorTasado(request.valorTasado())
                .fechaValidez(LocalDate.now().plusDays(dias))
                .build());
        return TasacionResponse.desde(tasacion);
    }

    /** El cliente acepta o rechaza el valor. Una tasación ya usada en una venta no cambia. */
    @Transactional
    public TasacionResponse cambiarEstado(Long id, EstadoTasacion nuevo) {
        TasacionTradeIn tasacion = buscar(id);
        if (nuevo != EstadoTasacion.ACEPTADA && nuevo != EstadoTasacion.RECHAZADA) {
            throw new SolicitudInvalidaException("El estado tiene que ser ACEPTADA o RECHAZADA");
        }
        if (tasacion.getEstado() == EstadoTasacion.UTILIZADA || tasacion.getEstado() == EstadoTasacion.RECHAZADA) {
            throw new ConflictoException("La tasación está " + tasacion.getEstado());
        }
        tasacion.setEstado(nuevo);
        return TasacionResponse.desde(tasacion);
    }

    @Transactional(readOnly = true)
    public TasacionResponse obtener(Long id) {
        return TasacionResponse.desde(buscar(id));
    }

    @Transactional(readOnly = true)
    public Page<TasacionResponse> buscar(Long clienteId, EstadoTasacion estado, Pageable pageable) {
        return tasacionRepository.buscar(clienteId, estado, pageable).map(TasacionResponse::desde);
    }

    /** Para cotizaciones y ventas: tiene que ser del mismo cliente, estar vigente y no haberse usado. */
    @Transactional(readOnly = true)
    public TasacionTradeIn buscarUtilizable(Long id, Long clienteId) {
        TasacionTradeIn tasacion = buscar(id);
        validarUtilizable(tasacion, clienteId);
        return tasacion;
    }

    public static void validarUtilizable(TasacionTradeIn tasacion, Long clienteId) {
        if (!tasacion.getClienteId().equals(clienteId)) {
            throw new SolicitudInvalidaException("La tasación " + tasacion.getId() + " es de otro cliente");
        }
        if (tasacion.getEstado() != EstadoTasacion.PENDIENTE && tasacion.getEstado() != EstadoTasacion.ACEPTADA) {
            throw new ConflictoException("La tasación " + tasacion.getId() + " está " + tasacion.getEstado());
        }
        if (tasacion.getFechaValidez() != null && tasacion.getFechaValidez().isBefore(LocalDate.now())) {
            throw new ConflictoException("La tasación " + tasacion.getId() + " venció el " + tasacion.getFechaValidez()
                    + "; hay que volver a tasar el auto");
        }
    }

    private TasacionTradeIn buscar(Long id) {
        return tasacionRepository.findById(id)
                .orElseThrow(() -> new NoEncontradoException("Tasación " + id + " no encontrada"));
    }
}
