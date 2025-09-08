package com.wquimis.demo.onboarding.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

// DTO para crear cuentas - debe coincidir con CuentaDTO del servicio de cuentas-movimientos
@Data
public class CuentaDTO {
    private Integer numeroCuenta;    // Campo requerido por CuentaDTO
    private Long idCliente;          // Campo requerido por CuentaDTO
    private String tipoCuenta;       // Campo requerido por CuentaDTO
    private BigDecimal saldoInicial; // Campo requerido por CuentaDTO
    private Boolean estado;
    private BigDecimal saldoDisponible;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaCierre;
}
