package com.concesionaria.financiamiento.financiamiento.dto;

import com.concesionaria.financiamiento.cuota.Cuota;
import com.concesionaria.financiamiento.cuota.dto.CuotaResponse;
import com.concesionaria.financiamiento.cuota.enums.EstadoCuota;
import com.concesionaria.financiamiento.financiamiento.Financiamiento;
import com.concesionaria.financiamiento.pago.dto.PagoResponse;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

/** RF-32: estado de cuenta de un financiamiento, con todas las cuotas y pagos. */
public record EstadoCuentaResponse(
        FinanciamientoResumenResponse financiamiento,
        Resumen resumen,
        List<CuotaResponse> cuotas,
        List<PagoResponse> pagos
) {

    /**
     * @param saldoPendiente todo lo que falta pagar (cuotas impagas + mora)
     * @param deudaVencida   la parte del saldo que ya está vencida
     */
    public record Resumen(
            BigDecimal montoAnticipo,
            BigDecimal anticipoPagado,
            BigDecimal anticipoPendiente,
            int cuotasPagadas,
            int cuotasPendientes,
            int cuotasVencidas,
            BigDecimal totalCuotas,
            BigDecimal totalPagadoCuotas,
            BigDecimal moraAcumulada,
            BigDecimal saldoPendiente,
            BigDecimal deudaVencida,
            CuotaResponse proximaCuota
    ) {
    }

    public static EstadoCuentaResponse desde(Financiamiento f) {
        List<Cuota> cuotas = f.getCuotas();
        int pagadas = 0;
        int pendientes = 0;
        int vencidas = 0;
        BigDecimal total = BigDecimal.ZERO;
        BigDecimal pagado = BigDecimal.ZERO;
        BigDecimal mora = BigDecimal.ZERO;
        BigDecimal saldo = BigDecimal.ZERO;
        BigDecimal vencido = BigDecimal.ZERO;
        for (Cuota c : cuotas) {
            if (c.getEstado() == EstadoCuota.ANULADA) {
                continue;
            }
            total = total.add(c.getMonto());
            pagado = pagado.add(c.getMontoPagado());
            mora = mora.add(c.getMora());
            BigDecimal saldoCuota = CuotaResponse.saldo(c);
            saldo = saldo.add(saldoCuota);
            switch (c.getEstado()) {
                case PAGADA -> pagadas++;
                case VENCIDA -> {
                    vencidas++;
                    vencido = vencido.add(saldoCuota);
                }
                default -> pendientes++;
            }
        }
        CuotaResponse proxima = cuotas.stream()
                .filter(c -> c.getEstado().impaga())
                .min(Comparator.comparing(Cuota::getNumero))
                .map(CuotaResponse::desde)
                .orElse(null);

        Resumen resumen = new Resumen(
                f.getMontoAnticipo(),
                f.getMontoAnticipoPagado(),
                f.getMontoAnticipo().subtract(f.getMontoAnticipoPagado()).max(BigDecimal.ZERO),
                pagadas, pendientes, vencidas,
                total, pagado, mora, saldo, vencido, proxima);

        return new EstadoCuentaResponse(
                FinanciamientoResumenResponse.desde(f),
                resumen,
                cuotas.stream().map(CuotaResponse::desde).toList(),
                f.getPagos().stream().map(PagoResponse::desde).toList());
    }
}
