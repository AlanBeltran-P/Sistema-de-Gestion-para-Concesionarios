package com.concesionaria.cliente.cliente.dto;

import com.concesionaria.cliente.cliente.Direccion;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DireccionDto(
        @NotBlank @Size(max = 120) String calle,
        @NotBlank @Size(max = 10) String numero,
        @Size(max = 20) String pisoDepto,
        @NotBlank @Size(max = 80) String ciudad,
        @NotBlank @Size(max = 80) String provincia,
        @Size(max = 10) String codigoPostal
) {

    public Direccion aEntidad() {
        return Direccion.builder()
                .calle(calle.trim())
                .numero(numero.trim())
                .pisoDepto(pisoDepto)
                .ciudad(ciudad.trim())
                .provincia(provincia.trim())
                .codigoPostal(codigoPostal)
                .build();
    }

    public static DireccionDto desde(Direccion d) {
        if (d == null || d.getCalle() == null) {
            return null;
        }
        return new DireccionDto(d.getCalle(), d.getNumero(), d.getPisoDepto(),
                d.getCiudad(), d.getProvincia(), d.getCodigoPostal());
    }
}
