package com.wquimis.demo.cuentasmovimientos.dto.kafka;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CuentaEventDTO {
    private String transactionId;
    private Long clienteId;
    private Integer numeroCuenta;
    private String tipoCuenta;
    private BigDecimal saldoInicial;
    private LocalDateTime timestamp;
}
