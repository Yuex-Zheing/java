package com.wquimis.demo.cuentasmovimientos.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CuentaDTO {
    @NotNull(message = "El número de cuenta es requerido")
    @Min(value = 100000, message = "El número de cuenta debe ser de 6 dígitos")
    @Max(value = 999999, message = "El número de cuenta debe ser de 6 dígitos")
    private Integer numeroCuenta;

    @NotNull(message = "El ID del cliente es requerido")
    private Long idCliente;

    @NotNull(message = "El tipo de cuenta es requerido")
    @Pattern(regexp = "^(AHORROS|CORRIENTE)$", message = "El tipo de cuenta debe ser AHORROS o CORRIENTE")
    private String tipoCuenta;

    @NotNull(message = "El saldo inicial es requerido")
    @DecimalMin(value = "0.0", message = "El saldo inicial no puede ser negativo")
    @Digits(integer = 6, fraction = 4, message = "El saldo debe tener máximo 6 dígitos enteros y 4 decimales")
    private BigDecimal saldoInicial;

    private Boolean estado;
    
    private BigDecimal saldoDisponible;
    
    private LocalDateTime fechaCreacion;
    
    private LocalDateTime fechaCierre;
}
