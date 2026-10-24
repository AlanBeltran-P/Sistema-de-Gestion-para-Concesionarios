package com.concesionaria.auth.autenticacion.dto;

import jakarta.validation.constraints.NotBlank;

/** Para /refresh y /logout. */
public record RefreshTokenRequest(@NotBlank String refreshToken) {
}
