package com.concesionaria.cliente.cliente;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Se guarda en las mismas columnas de la tabla clientes. */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Direccion {

    @Column(length = 120)
    private String calle;

    @Column(length = 10)
    private String numero;

    @Column(length = 20)
    private String pisoDepto;

    @Column(length = 80)
    private String ciudad;

    @Column(length = 80)
    private String provincia;

    @Column(length = 10)
    private String codigoPostal;
}
