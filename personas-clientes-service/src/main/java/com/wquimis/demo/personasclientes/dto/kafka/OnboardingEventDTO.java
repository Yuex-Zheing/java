package com.wquimis.demo.personasclientes.dto.kafka;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class OnboardingEventDTO {
    
    // Metadata de transacción
    private String transactionId;
    private String eventType; // PERSONA, CLIENTE, CUENTA, MOVIMIENTO
    private String status; // PENDING, SUCCESS, FAILED
    private LocalDateTime timestamp;
    private int retryCount;
    private String errorMessage;
    
    // Datos de Persona (formato personas-clientes service)
    private String personaIdentificacion;
    private String personaNombre;
    private String personaGenero;
    private Integer personaEdad;
    private String personaDireccion;
    private String personaTelefono;
    
    // Datos de Persona (formato onboarding service para compatibilidad)
    private String identificacionpersona;
    private String nombres;
    private String genero;
    private Integer edad;
    private String direccion;
    private String telefono;
    private String currentStep; // Para compatibilidad con onboarding service
    
    // Datos de Cliente
    private Long clienteId;
    private String clientePassword;
    private Boolean clienteEstado;
    
    // Datos de Cuenta
    private String numeroCuenta;
    private String tipoCuenta; // AHORROS, CORRIENTE
    private BigDecimal saldoInicial;
    private BigDecimal saldoDisponible;
    private Boolean cuentaEstado;
    
    // Datos de Movimiento
    private Long movimientoId;
    private String tipoMovimiento; // DEPOSITO, RETIRO
    private BigDecimal montoMovimiento;
    private String movimientoDescripcion;
    private LocalDateTime fechaMovimiento;
    
    // Datos de rollback/compensación
    private String failedStep; // PERSONA, CLIENTE, CUENTA, MOVIMIENTO
    private String compensationAction; // DELETE, DISABLE, REVERT
    
    // Metodos de conveniencia para crear eventos específicos
    public static OnboardingEventDTO createPersonaEvent(String transactionId, String identificacion, 
                                                       String nombre, String genero, Integer edad, 
                                                       String direccion, String telefono) {
        OnboardingEventDTO event = new OnboardingEventDTO();
        event.setTransactionId(transactionId);
        event.setEventType("PERSONA");
        event.setStatus("PENDING");
        event.setTimestamp(LocalDateTime.now());
        event.setRetryCount(0);
        
        event.setPersonaIdentificacion(identificacion);
        event.setPersonaNombre(nombre);
        event.setPersonaGenero(genero);
        event.setPersonaEdad(edad);
        event.setPersonaDireccion(direccion);
        event.setPersonaTelefono(telefono);
        
        return event;
    }
    
    public static OnboardingEventDTO createClienteEvent(String transactionId, String identificacion, 
                                                       String password, Boolean estado) {
        OnboardingEventDTO event = new OnboardingEventDTO();
        event.setTransactionId(transactionId);
        event.setEventType("CLIENTE");
        event.setStatus("PENDING");
        event.setTimestamp(LocalDateTime.now());
        event.setRetryCount(0);
        
        event.setPersonaIdentificacion(identificacion);
        event.setClientePassword(password);
        event.setClienteEstado(estado);
        
        return event;
    }
    
    public static OnboardingEventDTO createCuentaEvent(String transactionId, Long clienteId, 
                                                      String numeroCuenta, String tipoCuenta, 
                                                      BigDecimal saldoInicial) {
        OnboardingEventDTO event = new OnboardingEventDTO();
        event.setTransactionId(transactionId);
        event.setEventType("CUENTA");
        event.setStatus("PENDING");
        event.setTimestamp(LocalDateTime.now());
        event.setRetryCount(0);
        
        event.setClienteId(clienteId);
        event.setNumeroCuenta(numeroCuenta);
        event.setTipoCuenta(tipoCuenta);
        event.setSaldoInicial(saldoInicial);
        event.setCuentaEstado(true);
        
        return event;
    }
    
    public static OnboardingEventDTO createMovimientoEvent(String transactionId, String numeroCuenta,
                                                          String tipoMovimiento, BigDecimal monto,
                                                          String descripcion) {
        OnboardingEventDTO event = new OnboardingEventDTO();
        event.setTransactionId(transactionId);
        event.setEventType("MOVIMIENTO");
        event.setStatus("PENDING");
        event.setTimestamp(LocalDateTime.now());
        event.setRetryCount(0);
        
        event.setNumeroCuenta(numeroCuenta);
        event.setTipoMovimiento(tipoMovimiento);
        event.setMontoMovimiento(monto);
        event.setMovimientoDescripcion(descripcion);
        event.setFechaMovimiento(LocalDateTime.now());
        
        return event;
    }
    
    public static OnboardingEventDTO createRollbackEvent(String transactionId, String failedStep, 
                                                        String errorMessage, String compensationAction) {
        OnboardingEventDTO event = new OnboardingEventDTO();
        event.setTransactionId(transactionId);
        event.setEventType("ROLLBACK");
        event.setStatus("PENDING");
        event.setTimestamp(LocalDateTime.now());
        event.setRetryCount(0);
        
        event.setFailedStep(failedStep);
        event.setErrorMessage(errorMessage);
        event.setCompensationAction(compensationAction);
        
        return event;
    }
    
    public static OnboardingEventDTO createCompletedEvent(String transactionId) {
        OnboardingEventDTO event = new OnboardingEventDTO();
        event.setTransactionId(transactionId);
        event.setEventType("COMPLETED");
        event.setStatus("SUCCESS");
        event.setTimestamp(LocalDateTime.now());
        event.setRetryCount(0);
        
        return event;
    }
    
    // Métodos para manejo de reintentos
    public void incrementRetryCount() {
        this.retryCount++;
    }
    
    public boolean hasExceededMaxRetries(int maxRetries) {
        return this.retryCount >= maxRetries;
    }
    
    public void markAsSuccess() {
        this.status = "SUCCESS";
        this.timestamp = LocalDateTime.now();
    }
    
    public void markAsFailed(String errorMessage) {
        this.status = "FAILED";
        this.errorMessage = errorMessage;
        this.timestamp = LocalDateTime.now();
    }
    
    // Métodos de conveniencia para manejar ambos formatos
    public String getIdentificacionpersona() {
        return identificacionpersona != null ? identificacionpersona : personaIdentificacion;
    }
    
    public String getNombres() {
        return nombres != null ? nombres : personaNombre;
    }
    
    public String getGenero() {
        return genero != null ? genero : personaGenero;
    }
    
    public Integer getEdad() {
        return edad != null ? edad : personaEdad;
    }
    
    public String getDireccion() {
        return direccion != null ? direccion : personaDireccion;
    }
    
    public String getTelefono() {
        return telefono != null ? telefono : personaTelefono;
    }
}
