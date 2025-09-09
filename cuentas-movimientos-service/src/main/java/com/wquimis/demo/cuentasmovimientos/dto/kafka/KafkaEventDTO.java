package com.wquimis.demo.cuentasmovimientos.dto.kafka;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class KafkaEventDTO {
    
    // Metadata de transacción
    private String transactionId;
    private String eventType; // CUENTA, MOVIMIENTO, COMPLETED, ROLLBACK
    private String status; // PENDING, SUCCESS, FAILED
    private LocalDateTime timestamp;
    private int retryCount;
    private String errorMessage;
    
    // Datos de Cliente
    private Long clienteId;
    
    // Datos de Cuenta
    private String numeroCuenta;
    private Integer numeroCuentaInt; // Para compatibilidad
    private String tipoCuenta;
    private BigDecimal saldoInicial;
    private BigDecimal saldoDisponible;
    private Boolean cuentaEstado;
    
    // Datos de Movimiento
    private String tipoMovimiento;
    private BigDecimal montoMovimiento;
    private String movimientoDescripcion;
    private LocalDateTime fechaMovimiento;
    
    // Constructores de fábrica para diferentes tipos de eventos
    
    public static KafkaEventDTO fromCuentaEventDTO(CuentaEventDTO cuentaEvent) {
        KafkaEventDTO event = new KafkaEventDTO();
        event.setTransactionId(cuentaEvent.getTransactionId());
        event.setEventType("CUENTA");
        event.setStatus("PENDING");
        event.setTimestamp(cuentaEvent.getTimestamp() != null ? cuentaEvent.getTimestamp() : LocalDateTime.now());
        event.setRetryCount(0);
        
        event.setClienteId(cuentaEvent.getClienteId());
        event.setNumeroCuenta(cuentaEvent.getNumeroCuenta() != null ? cuentaEvent.getNumeroCuenta().toString() : null);
        event.setNumeroCuentaInt(cuentaEvent.getNumeroCuenta());
        event.setTipoCuenta(cuentaEvent.getTipoCuenta());
        event.setSaldoInicial(cuentaEvent.getSaldoInicial());
        event.setCuentaEstado(true);
        
        return event;
    }
    
    public static KafkaEventDTO fromOnboardingEventDTO(OnboardingEventDTO onboardingEvent) {
        KafkaEventDTO event = new KafkaEventDTO();
        event.setTransactionId(onboardingEvent.getTransactionId());
        event.setEventType(onboardingEvent.getEventType());
        event.setStatus(onboardingEvent.getStatus());
        event.setTimestamp(onboardingEvent.getTimestamp());
        event.setRetryCount(onboardingEvent.getRetryCount());
        event.setErrorMessage(onboardingEvent.getErrorMessage());
        
        event.setClienteId(onboardingEvent.getClienteId());
        event.setNumeroCuenta(onboardingEvent.getNumeroCuenta());
        if (onboardingEvent.getNumeroCuenta() != null) {
            try {
                event.setNumeroCuentaInt(Integer.valueOf(onboardingEvent.getNumeroCuenta()));
            } catch (NumberFormatException e) {
                // Ignorar si no es numérico
            }
        }
        event.setTipoCuenta(onboardingEvent.getTipoCuenta());
        event.setSaldoInicial(onboardingEvent.getSaldoInicial());
        event.setSaldoDisponible(onboardingEvent.getSaldoDisponible());
        event.setCuentaEstado(onboardingEvent.getCuentaEstado());
        
        event.setTipoMovimiento(onboardingEvent.getTipoMovimiento());
        event.setMontoMovimiento(onboardingEvent.getMontoMovimiento());
        event.setMovimientoDescripcion(onboardingEvent.getMovimientoDescripcion());
        event.setFechaMovimiento(onboardingEvent.getFechaMovimiento());
        
        return event;
    }
    
    public static KafkaEventDTO createMovimientoEvent(String transactionId, String numeroCuenta, 
                                                     String tipoMovimiento, BigDecimal monto, String descripcion) {
        KafkaEventDTO event = new KafkaEventDTO();
        event.setTransactionId(transactionId);
        event.setEventType("MOVIMIENTO");
        event.setStatus("PENDING");
        event.setTimestamp(LocalDateTime.now());
        event.setRetryCount(0);
        
        event.setNumeroCuenta(numeroCuenta);
        try {
            event.setNumeroCuentaInt(Integer.valueOf(numeroCuenta));
        } catch (NumberFormatException e) {
            // Ignorar si no es numérico
        }
        event.setTipoMovimiento(tipoMovimiento);
        event.setMontoMovimiento(monto);
        event.setMovimientoDescripcion(descripcion);
        event.setFechaMovimiento(LocalDateTime.now());
        
        return event;
    }
    
    public static KafkaEventDTO createCompletedEvent(String transactionId) {
        KafkaEventDTO event = new KafkaEventDTO();
        event.setTransactionId(transactionId);
        event.setEventType("COMPLETED");
        event.setStatus("SUCCESS");
        event.setTimestamp(LocalDateTime.now());
        event.setRetryCount(0);
        
        return event;
    }
    
    public static KafkaEventDTO createRollbackEvent(String transactionId, String failedStep, String errorMessage) {
        KafkaEventDTO event = new KafkaEventDTO();
        event.setTransactionId(transactionId);
        event.setEventType("ROLLBACK");
        event.setStatus("FAILED");
        event.setTimestamp(LocalDateTime.now());
        event.setRetryCount(0);
        event.setErrorMessage(String.format("Error en fase %s: %s", failedStep, errorMessage));
        
        return event;
    }
    
    // Métodos de conversión a OnboardingEventDTO para compatibilidad
    public OnboardingEventDTO toOnboardingEventDTO() {
        OnboardingEventDTO event = new OnboardingEventDTO();
        event.setTransactionId(this.transactionId);
        event.setEventType(this.eventType);
        event.setStatus(this.status);
        event.setTimestamp(this.timestamp);
        event.setRetryCount(this.retryCount);
        event.setErrorMessage(this.errorMessage);
        
        event.setClienteId(this.clienteId);
        event.setNumeroCuenta(this.numeroCuenta);
        event.setTipoCuenta(this.tipoCuenta);
        event.setSaldoInicial(this.saldoInicial);
        event.setSaldoDisponible(this.saldoDisponible);
        event.setCuentaEstado(this.cuentaEstado);
        
        event.setTipoMovimiento(this.tipoMovimiento);
        event.setMontoMovimiento(this.montoMovimiento);
        event.setMovimientoDescripcion(this.movimientoDescripcion);
        event.setFechaMovimiento(this.fechaMovimiento);
        
        return event;
    }
}
