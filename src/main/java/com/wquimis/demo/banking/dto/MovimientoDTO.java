package com.wquimis.demo.banking.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class MovimientoDTO {
    @NotNull(message = "El número de cuenta es requerido")
    private Integer numeroCuenta;

    @NotNull(message = "El tipo de movimiento es requerido")
    private String tipoMovimiento;

    @NotNull(message = "El monto es requerido")
    @DecimalMin(value = "0.0001", message = "El monto debe ser mayor a cero")
    @Digits(integer = 6, fraction = 4, message = "El monto debe tener máximo 6 dígitos enteros y 4 decimales")
    private BigDecimal monto;
}
