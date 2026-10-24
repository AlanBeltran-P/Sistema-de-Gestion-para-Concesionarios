package com.concesionaria.auth.autenticacion.dto;

import jakarta.validation.constraints.NotBlank;

/** RF-03 */
public record LoginRequest(@NotBlank String email, @NotBlank String password) {
}
