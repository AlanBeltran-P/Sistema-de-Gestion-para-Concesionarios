package com.concesionaria.financiamiento.cuota.enums;

/** VENCIDA la marca el job programado (RF-33). ANULADA: se anuló la venta y la cuota no se cobra. */
public enum EstadoCuota {
    PENDIENTE,
    PAGADA_PARCIAL,
    PAGADA,
    VENCIDA,
    ANULADA;

    /** Todavía se le debe algo. */
    public boolean impaga() {
        return this == PENDIENTE || this == PAGADA_PARCIAL || this == VENCIDA;
    }
}
