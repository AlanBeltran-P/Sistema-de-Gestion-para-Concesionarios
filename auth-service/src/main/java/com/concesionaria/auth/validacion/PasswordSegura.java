package com.concesionaria.auth.validacion;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.ReportAsSingleViolation;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Regla única de contraseña para registro, alta de empleados y cambio de contraseña:
 * entre 8 y 72 caracteres (72 es el máximo que usa BCrypt), con al menos una letra y un número.
 */
@NotBlank
@Size(min = 8, max = 72)
@Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$")
@ReportAsSingleViolation
@Constraint(validatedBy = {})
@Documented
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
public @interface PasswordSegura {

    String message() default "La contraseña debe tener entre 8 y 72 caracteres, con al menos una letra y un número";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
