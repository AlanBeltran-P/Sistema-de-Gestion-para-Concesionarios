package com.concesionaria.cliente.historial.service;

import com.concesionaria.cliente.cliente.dto.ClienteResponse;
import com.concesionaria.cliente.cliente.service.ClienteService;
import com.concesionaria.cliente.historial.FinanciamientoClient;
import com.concesionaria.cliente.historial.VentaClient;
import com.concesionaria.cliente.historial.dto.HistorialResponse;
import com.concesionaria.cliente.historial.dto.PaginaRemota;
import com.concesionaria.common.security.UsuarioActual;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * RF-17: arma el historial consultando a venta-service (cotizaciones, reservas, compras) y a
 * financiamiento-service (créditos y pagos). Si uno no responde, esa parte queda vacía con un aviso.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HistorialService {

    private final ClienteService clienteService;
    private final VentaClient ventaClient;
    private final FinanciamientoClient financiamientoClient;

    /** Para un vendedor o ADMIN. */
    public HistorialResponse deCliente(Long clienteId) {
        return armar(clienteService.obtener(clienteId), false);
    }

    /** Para el cliente logueado: no ve las comisiones de sus compras. */
    public HistorialResponse propio(UsuarioActual usuario) {
        return armar(clienteService.perfilPropio(usuario), true);
    }

    private HistorialResponse armar(ClienteResponse cliente, boolean ocultarComisiones) {
        Long id = cliente.id();
        List<String> avisos = new ArrayList<>();
        List<Map<String, Object>> compras = cargar("las compras", () -> ventaClient.ventas(id), avisos);
        if (ocultarComisiones) {
            compras = compras.stream().map(c -> {
                Map<String, Object> copia = new LinkedHashMap<>(c);
                copia.remove("comision");
                return copia;
            }).toList();
        }
        return new HistorialResponse(
                cliente,
                cargar("las cotizaciones", () -> ventaClient.cotizaciones(id), avisos),
                cargar("las reservas", () -> ventaClient.reservas(id), avisos),
                compras,
                cargar("los financiamientos", () -> financiamientoClient.financiamientos(id), avisos),
                cargar("los pagos", () -> financiamientoClient.pagos(id), avisos),
                avisos);
    }

    private static List<Map<String, Object>> cargar(String que, Supplier<PaginaRemota> llamada, List<String> avisos) {
        try {
            PaginaRemota pagina = llamada.get();
            return pagina != null && pagina.content() != null ? pagina.content() : List.of();
        } catch (RuntimeException e) {
            log.warn("No se pudieron cargar {} para el historial: {}", que, e.getMessage());
            avisos.add("No se pudieron cargar " + que + " (el servicio no responde). Intentá de nuevo en unos minutos.");
            return List.of();
        }
    }
}
