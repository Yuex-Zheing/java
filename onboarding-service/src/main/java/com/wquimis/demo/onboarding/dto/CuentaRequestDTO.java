package com.wquimis.demo.onboarding.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class CuentaRequestDTO {
    
    @NotBlank(message = "El tipo de cuenta es requerido")
    @Pattern(regexp = "^(AHORROS|CORRIENTE)$", message = "El tipo de cuenta debe ser AHORROS o CORRIENTE")
    private String tipoCuenta;
    
    @NotNull(message = "El saldo inicial es requerido")
    @DecimalMin(value = "0.0", inclusive = true, message = "El saldo inicial no puede ser negativo")
    @Digits(integer = 10, fraction = 2, message = "El saldo inicial debe tener máximo 10 enteros y 2 decimales")
    private BigDecimal saldoInicial;
}
