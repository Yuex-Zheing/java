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
    
    @JsonProperty("descripcion")
    @NotNull(message = "La descripción es requerida")
    @Size(max = 300, message = "La descripción no puede exceder los 300 caracteres")
    private String descripcion;
    
    @JsonProperty("tipo")
    @NotNull(message = "El tipo de movimiento es requerido")
    @Pattern(regexp = "^(RETIRO|DEPOSITO)$", message = "El tipo de movimiento debe ser RETIRO o DEPOSITO")
    private String tipo;
    
    @JsonProperty("valor")
    @NotNull(message = "El valor es requerido")
    @Pattern(regexp = "^-?\\d{1,6}(\\.\\d{1,4})?$", message = "El valor debe tener máximo 6 dígitos enteros y 4 decimales")
    private String valor;
    
    @JsonProperty("saldo")
    private BigDecimal saldo;
    
    // Campo interno para procesamiento
    @JsonIgnore
    private Integer numeroCuenta;
}
