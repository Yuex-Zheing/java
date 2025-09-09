package com.wquimis.demo.onboarding.dto.kafka;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO para eventos de onboarding - contiene toda la información necesaria para el flujo
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OnboardingEventDTO {
    
    // Metadatos del flujo
    private String transactionId;
    private String currentStep;
    private LocalDateTime timestamp;
    
    // Datos de la persona
    private String identificacionpersona;
    private String nombres;
    private String genero;
    private Integer edad;
    private String direccion;
    private String telefono;
    private Long personaId; // Se llena después de crear la persona
    
    // Datos del cliente
    private String nombreUsuario;
    private String contrasena;
    private Long clienteId; // Se llena después de crear el cliente
    
    // Datos de la cuenta
    private String tipoCuenta;
    private BigDecimal saldoInicial;
    private Integer numeroCuenta;
    private Long cuentaId; // Se llena después de crear la cuenta
    
    // Datos del movimiento (si aplica)
    private Long movimientoId; // Se llena después de crear el movimiento
    
    // Estado del flujo
    private boolean completed;
    private String failureReason;
    private int retryCount;
}
