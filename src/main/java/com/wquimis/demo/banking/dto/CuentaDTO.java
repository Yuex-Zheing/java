package com.wquimis.demo.banking.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import com.wquimis.demo.banking.entities.Cuenta.TipoCuenta;

@Data
public class CuentaDTO {
    @NotNull(message = "El número de cuenta es requerido")
    @Min(value = 100000, message = "El número de cuenta debe ser de 6 dígitos")
    @Max(value = 999999, message = "El número de cuenta debe ser de 6 dígitos")
    private Integer numeroCuenta;

    @NotNull(message = "El ID del cliente es requerido")
    private Long idCliente;

    @NotNull(message = "El tipo de cuenta es requerido")
    private TipoCuenta tipoCuenta;

    @NotNull(message = "El saldo inicial es requerido")
    @DecimalMin(value = "0.0", message = "El saldo inicial no puede ser negativo")
    @Digits(integer = 6, fraction = 4, message = "El saldo debe tener máximo 6 dígitos enteros y 4 decimales")
    private BigDecimal saldoInicial;
}
