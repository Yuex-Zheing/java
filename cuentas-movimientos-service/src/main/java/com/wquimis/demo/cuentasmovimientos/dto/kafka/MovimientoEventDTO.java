package com.wquimis.demo.cuentasmovimientos.dto.kafka;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MovimientoEventDTO {
    private String transactionId;
    private Integer numeroCuenta;
    private String tipoMovimiento; // DEPOSITO
    private BigDecimal monto;
    private String descripcion;
    private LocalDateTime timestamp;
    private int retryCount;
}
