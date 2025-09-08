package com.wquimis.demo.onboarding.dto;

import lombok.Data;
import java.math.BigDecimal;

// DTO para crear movimientos - debe coincidir con MovimientoDTO del servicio de cuentas-movimientos
@Data
public class MovimientoDTO {
    private Long id;
    private String fecha;
    private String hora;
    private String movimientodescripcion; // Campo requerido por MovimientoDTO
    private String tipomovimiento;        // Campo requerido por MovimientoDTO
    private BigDecimal montomovimiento;   // Campo requerido por MovimientoDTO
    private BigDecimal saldo;
    private Boolean esReverso;
    private Integer numeroCuenta;
}
