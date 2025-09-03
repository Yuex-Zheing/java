package com.wquimis.demo.banking.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class MovimientoDTO {
    @JsonProperty("id")
    private Long id;
    
    @JsonProperty("fecha")
    private String fecha;
    
    @JsonProperty("hora")
    private String hora;
    
    @JsonProperty("movimientodescripcion")
    @NotNull(message = "La descripción del movimiento es requerida")
    @Size(max = 300, message = "La descripción no puede exceder los 300 caracteres")
    private String movimientodescripcion;
    
    @JsonProperty("tipomovimiento")
    @NotNull(message = "El tipo de movimiento es requerido")
    @Pattern(regexp = "^(RETIRO|DEPOSITO)$", message = "El tipo de movimiento debe ser RETIRO o DEPOSITO")
    private String tipomovimiento;
    
    @JsonProperty("montomovimiento")
    @NotNull(message = "El monto del movimiento es requerido")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a 0")
    @DecimalMax(value = "999999.9999", message = "El monto debe tener máximo 6 dígitos enteros y 4 decimales")
    private BigDecimal montomovimiento;
    
    @JsonProperty("saldo")
    private BigDecimal saldo;
    
    // Campo interno para procesamiento
    @JsonIgnore
    private Integer numeroCuenta;
}
