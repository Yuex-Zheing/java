package com.wquimis.demo.cuentasmovimientos.services.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wquimis.demo.cuentasmovimientos.config.KafkaTopicConfig;
import com.wquimis.demo.cuentasmovimientos.dto.kafka.CuentaEventDTO;
import com.wquimis.demo.cuentasmovimientos.dto.kafka.MovimientoEventDTO;
import com.wquimis.demo.cuentasmovimientos.entities.Cuenta;
import com.wquimis.demo.cuentasmovimientos.entities.Movimiento;
import com.wquimis.demo.cuentasmovimientos.services.CuentaService;
import com.wquimis.demo.cuentasmovimientos.services.MovimientoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class OnboardingEventListener {

    private final CuentaService cuentaService;
    private final MovimientoService movimientoService;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = KafkaTopicConfig.CUENTA_TOPIC)
    @Transactional
    public void procesarCuenta(@Payload Object payload,
                              @Header(KafkaHeaders.RECEIVED_KEY) String key,
                              Acknowledgment acknowledgment) {
        try {
            log.info("Procesando creación de cuenta con transactionId: {}", key);
            
            // Convertir payload a CuentaEventDTO
            CuentaEventDTO cuentaEvent = objectMapper.convertValue(payload, CuentaEventDTO.class);
            
            // Verificar si ya existe una cuenta para este cliente
            boolean cuentaExiste = false;
            try {
                cuentaService.findByNumeroCuenta(cuentaEvent.getNumeroCuenta());
                cuentaExiste = true;
                log.info("Cuenta ya existe con número: {} para transactionId: {}", 
                        cuentaEvent.getNumeroCuenta(), key);
                        
            } catch (Exception e) {
                // Cuenta no existe, crear nueva
                log.info("Creando nueva cuenta para transactionId: {}", key);
                
                Cuenta nuevaCuenta = new Cuenta();
                nuevaCuenta.setNumerocuenta(cuentaEvent.getNumeroCuenta());
                nuevaCuenta.setIdcliente(cuentaEvent.getClienteId());
                nuevaCuenta.setTipocuenta(Cuenta.TipoCuenta.valueOf(cuentaEvent.getTipoCuenta()));
                nuevaCuenta.setSaldoinicial(cuentaEvent.getSaldoInicial());
                nuevaCuenta.setSaldodisponible(cuentaEvent.getSaldoInicial());
                nuevaCuenta.setEstado(true);
                nuevaCuenta.setFechacreacion(LocalDateTime.now());
                
                Cuenta cuentaCreada = cuentaService.save(nuevaCuenta);
                log.info("Cuenta creada exitosamente con número: {} para transactionId: {}", 
                        cuentaCreada.getNumerocuenta(), key);
            }
            
            // Si el saldo inicial es mayor que cero, crear movimiento inicial
            if (cuentaEvent.getSaldoInicial().compareTo(BigDecimal.ZERO) > 0) {
                MovimientoEventDTO movimientoEvent = new MovimientoEventDTO();
                movimientoEvent.setTransactionId(cuentaEvent.getTransactionId());
                movimientoEvent.setNumeroCuenta(cuentaEvent.getNumeroCuenta());
                movimientoEvent.setTipoMovimiento("DEPOSITO");
                movimientoEvent.setMonto(cuentaEvent.getSaldoInicial());
                movimientoEvent.setDescripcion("Depósito inicial - Apertura de cuenta");
                movimientoEvent.setTimestamp(LocalDateTime.now());
                movimientoEvent.setRetryCount(0);
                
                // Enviar mensaje para crear movimiento inicial
                CompletableFuture<org.springframework.kafka.support.SendResult<String, Object>> future = 
                    kafkaTemplate.send(KafkaTopicConfig.MOVIMIENTO_TOPIC, key, movimientoEvent);
                
                future.whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("Mensaje de movimiento inicial enviado exitosamente para transactionId: {}", key);
                        acknowledgment.acknowledge();
                    } else {
                        log.error("Error al enviar mensaje de movimiento inicial para transactionId: {}", key, ex);
                        // No hacer acknowledge para que se reintente
                    }
                });
            } else {
                // No hay movimiento inicial, completar el onboarding
                enviarMensajeCompletado(key);
                acknowledgment.acknowledge();
            }
            
        } catch (Exception e) {
            log.error("Error al procesar cuenta para transactionId: {}", key, e);
            // Enviar mensaje de rollback
            enviarMensajeRollback(key, "CUENTA", e.getMessage());
            acknowledgment.acknowledge();
        }
    }

    @KafkaListener(topics = KafkaTopicConfig.MOVIMIENTO_TOPIC)
    @Transactional
    @Retryable(maxAttempts = 5, backoff = @Backoff(delay = 300000)) // 5 minutos
    public void procesarMovimiento(@Payload Object payload,
                                  @Header(KafkaHeaders.RECEIVED_KEY) String key,
                                  Acknowledgment acknowledgment) {
        MovimientoEventDTO movimientoEvent = null;
        try {
            log.info("Procesando creación de movimiento con transactionId: {}", key);
            
            // Convertir payload a MovimientoEventDTO
            movimientoEvent = objectMapper.convertValue(payload, MovimientoEventDTO.class);
            
            // Obtener la cuenta
            Cuenta cuenta = cuentaService.findByNumeroCuenta(movimientoEvent.getNumeroCuenta());
            
            // Crear el movimiento inicial
            Movimiento nuevoMovimiento = new Movimiento();
            nuevoMovimiento.setCuenta(cuenta);
            nuevoMovimiento.setTipomovimiento(Movimiento.TipoMovimiento.valueOf(movimientoEvent.getTipoMovimiento()));
            nuevoMovimiento.setMontomovimiento(movimientoEvent.getMonto());
            nuevoMovimiento.setMovimientodescripcion(movimientoEvent.getDescripcion());
            nuevoMovimiento.setEstado(true);
            
            // Realizar el movimiento usando el servicio
            Movimiento movimientoCreado = movimientoService.realizarMovimiento(nuevoMovimiento);
            
            log.info("Movimiento inicial creado exitosamente con ID: {} para transactionId: {}", 
                    movimientoCreado.getIdmovimiento(), key);
            
            // Completar el onboarding
            enviarMensajeCompletado(key);
            acknowledgment.acknowledge();
            
        } catch (Exception e) {
            if (movimientoEvent == null) {
                movimientoEvent = objectMapper.convertValue(payload, MovimientoEventDTO.class);
            }
            
            log.error("Error al procesar movimiento para transactionId: {} (intento: {})", 
                     key, movimientoEvent.getRetryCount() + 1, e);
            
            movimientoEvent.setRetryCount(movimientoEvent.getRetryCount() + 1);
            
            if (movimientoEvent.getRetryCount() >= 5) {
                // Después de 5 intentos, marcar la cuenta como cerrada
                try {
                    Cuenta cuenta = cuentaService.findByNumeroCuenta(movimientoEvent.getNumeroCuenta());
                    cuenta.setEstado(false);
                    cuenta.setFechacierre(LocalDateTime.now());
                    cuentaService.saveOrUpdate(cuenta);
                    
                    log.error("Cuenta {} marcada como cerrada después de 5 intentos fallidos de movimiento para transactionId: {}", 
                             movimientoEvent.getNumeroCuenta(), key);
                    
                    acknowledgment.acknowledge();
                } catch (Exception ex) {
                    log.error("Error al marcar cuenta como cerrada para transactionId: {}", key, ex);
                    acknowledgment.acknowledge();
                }
            } else {
                // Reenviar para reintento después de 5 minutos
                programarReintento(key, movimientoEvent);
                acknowledgment.acknowledge();
            }
        }
    }

    @Async
    private void programarReintento(String key, MovimientoEventDTO movimientoEvent) {
        try {
            // Esperar 5 minutos antes de reenviar
            TimeUnit.MINUTES.sleep(5);
            
            kafkaTemplate.send(KafkaTopicConfig.MOVIMIENTO_TOPIC, key, movimientoEvent);
            log.info("Reintento {} programado para movimiento con transactionId: {}", 
                     movimientoEvent.getRetryCount(), key);
                     
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Interrupción durante el reintento para transactionId: {}", key, e);
        } catch (Exception e) {
            log.error("Error al programar reintento para transactionId: {}", key, e);
        }
    }

    @KafkaListener(topics = KafkaTopicConfig.ROLLBACK_TOPIC)
    public void procesarRollback(@Payload Object payload,
                                @Header(KafkaHeaders.RECEIVED_KEY) String key,
                                Acknowledgment acknowledgment) {
        try {
            log.info("Procesando rollback para transactionId: {}", key);
            
            RollbackEventDTO rollbackEvent = objectMapper.convertValue(payload, RollbackEventDTO.class);
            
            // Implementar lógica de rollback específica para cuentas y movimientos
            if ("CUENTA".equals(rollbackEvent.getFailedStep()) || 
                "MOVIMIENTO".equals(rollbackEvent.getFailedStep())) {
                
                // Intentar eliminar cuenta si existe
                try {
                    // Por simplicidad, marcar como cerrada en lugar de eliminar
                    log.warn("Rollback requerido para cuentas/movimientos - transactionId: {}", key);
                } catch (Exception e) {
                    log.error("Error durante rollback de cuenta para transactionId: {}", key, e);
                }
            }
            
            acknowledgment.acknowledge();
            
        } catch (Exception e) {
            log.error("Error al procesar rollback para transactionId: {}", key, e);
            acknowledgment.acknowledge();
        }
    }

    private void enviarMensajeCompletado(String transactionId) {
        try {
            CompletedEventDTO completedEvent = new CompletedEventDTO();
            completedEvent.setTransactionId(transactionId);
            completedEvent.setStatus("SUCCESS");
            completedEvent.setTimestamp(LocalDateTime.now());
            
            kafkaTemplate.send(KafkaTopicConfig.COMPLETED_TOPIC, transactionId, completedEvent);
            log.info("Onboarding completado exitosamente para transactionId: {}", transactionId);
            
        } catch (Exception e) {
            log.error("Error al enviar mensaje de completado para transactionId: {}", transactionId, e);
        }
    }

    private void enviarMensajeRollback(String transactionId, String failedStep, String errorMessage) {
        try {
            RollbackEventDTO rollbackEvent = new RollbackEventDTO();
            rollbackEvent.setTransactionId(transactionId);
            rollbackEvent.setFailedStep(failedStep);
            rollbackEvent.setErrorMessage(errorMessage);
            rollbackEvent.setTimestamp(LocalDateTime.now());
            
            kafkaTemplate.send(KafkaTopicConfig.ROLLBACK_TOPIC, transactionId, rollbackEvent);
            log.info("Mensaje de rollback enviado para transactionId: {}", transactionId);
            
        } catch (Exception e) {
            log.error("Error al enviar mensaje de rollback para transactionId: {}", transactionId, e);
        }
    }

    // DTOs para eventos internos
    public static class RollbackEventDTO {
        private String transactionId;
        private String failedStep;
        private String errorMessage;
        private LocalDateTime timestamp;

        // Getters y setters
        public String getTransactionId() { return transactionId; }
        public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
        public String getFailedStep() { return failedStep; }
        public void setFailedStep(String failedStep) { this.failedStep = failedStep; }
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    }

    public static class CompletedEventDTO {
        private String transactionId;
        private String status;
        private LocalDateTime timestamp;

        // Getters y setters
        public String getTransactionId() { return transactionId; }
        public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    }
}
