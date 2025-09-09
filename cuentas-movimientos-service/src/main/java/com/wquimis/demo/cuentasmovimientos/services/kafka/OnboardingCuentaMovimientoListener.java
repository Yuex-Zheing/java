package com.wquimis.demo.cuentasmovimientos.services.kafka;

import com.wquimis.demo.cuentasmovimientos.config.KafkaTopicConfig;
import com.wquimis.demo.cuentasmovimientos.dto.kafka.OnboardingEventDTO;
import com.wquimis.demo.cuentasmovimientos.entities.Cuenta;
import com.wquimis.demo.cuentasmovimientos.entities.Movimiento;
import com.wquimis.demo.cuentasmovimientos.exceptions.CuentaExistenteException;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class OnboardingCuentaMovimientoListener {

    private final CuentaService cuentaService;
    private final MovimientoService movimientoService;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 5000; // 5 segundos

    /**
     * Procesa eventos de creación de cuenta con manejo transaccional
     */
    @KafkaListener(topics = KafkaTopicConfig.ONBOARDING_CUENTA, groupId = "cuentas-movimientos-cuenta-group")
    @Transactional
    public void procesarCreacionCuenta(@Payload OnboardingEventDTO event,
                                     @Header(KafkaHeaders.RECEIVED_KEY) String transactionId,
                                     Acknowledgment acknowledgment) {
        
        log.info("[CUENTA] Procesando creación de cuenta para transactionId: {}", transactionId);
        
        try {
            // Validar que el evento sea del tipo correcto
            if (!"CUENTA".equals(event.getEventType())) {
                log.warn("[CUENTA] Evento recibido no es de tipo CUENTA: {} para transactionId: {}", 
                         event.getEventType(), transactionId);
                acknowledgment.acknowledge();
                return;
            }
            
            // Verificar si ya se procesó este evento (idempotencia)
            Integer numeroCuentaInt = Integer.valueOf(event.getNumeroCuenta());
            if (verificarCuentaExistente(numeroCuentaInt)) {
                log.info("[CUENTA] Cuenta ya existe con número: {} para transactionId: {}", 
                         event.getNumeroCuenta(), transactionId);
                
                // Si la cuenta ya existe, pasar al siguiente paso (movimiento)
                if (event.getSaldoInicial() != null && event.getSaldoInicial().compareTo(BigDecimal.ZERO) > 0) {
                    enviarEventoMovimientoInicial(event);
                } else {
                    enviarEventoCompletado(transactionId);
                }
                acknowledgment.acknowledge();
                return;
            }
            
            // Crear nueva cuenta
            Cuenta nuevaCuenta = crearNuevaCuenta(event);
            Cuenta cuentaCreada = cuentaService.save(nuevaCuenta);
            
            log.info("[CUENTA] Cuenta creada exitosamente con número: {} para transactionId: {}", 
                     cuentaCreada.getNumerocuenta(), transactionId);
            
            // Si hay saldo inicial, crear movimiento
            if (event.getSaldoInicial() != null && event.getSaldoInicial().compareTo(BigDecimal.ZERO) > 0) {
                OnboardingEventDTO movimientoEvent = OnboardingEventDTO.createMovimientoEvent(
                    transactionId,
                    event.getNumeroCuenta(),
                    "DEPOSITO",
                    event.getSaldoInicial(),
                    "Depósito inicial - Apertura de cuenta"
                );
                
                enviarMensajeKafka(KafkaTopicConfig.ONBOARDING_MOVIMIENTO, transactionId, movimientoEvent);
                log.info("[CUENTA] Evento de movimiento inicial enviado para transactionId: {}", transactionId);
            } else {
                // Sin movimiento inicial, completar onboarding
                enviarEventoCompletado(transactionId);
            }
            
            acknowledgment.acknowledge();
            
        } catch (CuentaExistenteException e) {
            log.warn("[CUENTA] Cuenta ya existe para transactionId: {}", transactionId);
            acknowledgment.acknowledge();
            
        } catch (Exception e) {
            log.error("[CUENTA] Error al procesar creación de cuenta para transactionId: {}", transactionId, e);
            
            if (event.hasExceededMaxRetries(MAX_RETRIES)) {
                log.error("[CUENTA] Máximo de reintentos excedido para transactionId: {}, enviando rollback", transactionId);
                enviarEventoRollback(transactionId, "CUENTA", e.getMessage());
                acknowledgment.acknowledge();
            } else {
                // Incrementar contador y reenviar
                event.incrementRetryCount();
                event.markAsFailed(e.getMessage());
                
                try {
                    Thread.sleep(RETRY_DELAY_MS * event.getRetryCount()); // Backoff exponencial
                    enviarMensajeKafka(KafkaTopicConfig.ONBOARDING_CUENTA, transactionId, event);
                    log.info("[CUENTA] Reintento {} programado para transactionId: {}", 
                             event.getRetryCount(), transactionId);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    log.error("[CUENTA] Interrupción durante reintento para transactionId: {}", transactionId);
                }
                
                acknowledgment.acknowledge();
            }
        }
    }

    /**
     * Procesa eventos de creación de movimiento con manejo transaccional
     */
    @KafkaListener(topics = KafkaTopicConfig.ONBOARDING_MOVIMIENTO, groupId = "cuentas-movimientos-movimiento-group")
    @Transactional
    public void procesarCreacionMovimiento(@Payload OnboardingEventDTO event,
                                         @Header(KafkaHeaders.RECEIVED_KEY) String transactionId,
                                         Acknowledgment acknowledgment) {
        
        log.info("[MOVIMIENTO] Procesando creación de movimiento para transactionId: {}", transactionId);
        
        try {
            // Validar que el evento sea del tipo correcto
            if (!"MOVIMIENTO".equals(event.getEventType())) {
                log.warn("[MOVIMIENTO] Evento recibido no es de tipo MOVIMIENTO: {} para transactionId: {}", 
                         event.getEventType(), transactionId);
                acknowledgment.acknowledge();
                return;
            }
            
            // Obtener la cuenta
            Integer numeroCuentaInt = Integer.valueOf(event.getNumeroCuenta());
            Cuenta cuenta = cuentaService.findByNumeroCuenta(numeroCuentaInt);
            if (cuenta == null) {
                throw new RuntimeException("Cuenta no encontrada: " + event.getNumeroCuenta());
            }
            
            // Crear el movimiento
            Movimiento nuevoMovimiento = crearNuevoMovimiento(event, cuenta);
            Movimiento movimientoCreado = movimientoService.realizarMovimiento(nuevoMovimiento);
            
            log.info("[MOVIMIENTO] Movimiento creado exitosamente con ID: {} para transactionId: {}", 
                     movimientoCreado.getIdmovimiento(), transactionId);
            
            // Completar el proceso de onboarding
            enviarEventoCompletado(transactionId);
            acknowledgment.acknowledge();
            
        } catch (Exception e) {
            log.error("[MOVIMIENTO] Error al procesar movimiento para transactionId: {}", transactionId, e);
            
            if (event.hasExceededMaxRetries(MAX_RETRIES)) {
                log.error("[MOVIMIENTO] Máximo de reintentos excedido para transactionId: {}, cerrando cuenta", transactionId);
                
                try {
                    // Cerrar la cuenta como medida de compensación
                    cerrarCuentaPorError(event.getNumeroCuenta(), transactionId);
                } catch (Exception ce) {
                    log.error("[MOVIMIENTO] Error al cerrar cuenta durante compensación para transactionId: {}", transactionId, ce);
                }
                
                enviarEventoRollback(transactionId, "MOVIMIENTO", e.getMessage());
                acknowledgment.acknowledge();
                
            } else {
                // Incrementar contador y reenviar
                event.incrementRetryCount();
                event.markAsFailed(e.getMessage());
                
                try {
                    Thread.sleep(RETRY_DELAY_MS * event.getRetryCount()); // Backoff exponencial
                    enviarMensajeKafka(KafkaTopicConfig.ONBOARDING_MOVIMIENTO, transactionId, event);
                    log.info("[MOVIMIENTO] Reintento {} programado para transactionId: {}", 
                             event.getRetryCount(), transactionId);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    log.error("[MOVIMIENTO] Interrupción durante reintento para transactionId: {}", transactionId);
                }
                
                acknowledgment.acknowledge();
            }
        }
    }

    /**
     * Procesa eventos de rollback para compensar transacciones fallidas
     */
    @KafkaListener(topics = KafkaTopicConfig.ONBOARDING_ROLLBACK, groupId = "cuentas-movimientos-rollback-group")
    @Transactional
    public void procesarRollback(@Payload OnboardingEventDTO event,
                               @Header(KafkaHeaders.RECEIVED_KEY) String transactionId,
                               Acknowledgment acknowledgment) {
        
        log.info("[ROLLBACK] Procesando rollback para transactionId: {}, paso fallido: {}", 
                 transactionId, event.getFailedStep());
        
        try {
            // Solo procesar rollbacks relacionados con cuentas y movimientos
            if ("CUENTA".equals(event.getFailedStep()) || "MOVIMIENTO".equals(event.getFailedStep())) {
                
                // Intentar compensar creando una cuenta cerrada o cerrando cuenta existente
                if (event.getNumeroCuenta() != null) {
                    try {
                        cerrarCuentaPorError(event.getNumeroCuenta(), transactionId);
                        log.info("[ROLLBACK] Cuenta {} marcada como cerrada para transactionId: {}", 
                                 event.getNumeroCuenta(), transactionId);
                    } catch (Exception e) {
                        log.warn("[ROLLBACK] No se pudo cerrar cuenta {} para transactionId: {} - Error: {}", 
                                 event.getNumeroCuenta(), transactionId, e.getMessage());
                    }
                }
            }
            
            acknowledgment.acknowledge();
            
        } catch (Exception e) {
            log.error("[ROLLBACK] Error al procesar rollback para transactionId: {}", transactionId, e);
            acknowledgment.acknowledge(); // Acknowledge para evitar loops infinitos
        }
    }

    // Métodos auxiliares privados
    
    private boolean verificarCuentaExistente(Integer numeroCuenta) {
        try {
            Cuenta cuenta = cuentaService.findByNumeroCuenta(numeroCuenta);
            return cuenta != null;
        } catch (Exception e) {
            return false;
        }
    }
    
    private Cuenta crearNuevaCuenta(OnboardingEventDTO event) {
        Cuenta nuevaCuenta = new Cuenta();
        nuevaCuenta.setNumerocuenta(Integer.valueOf(event.getNumeroCuenta()));
        nuevaCuenta.setIdcliente(event.getClienteId());
        nuevaCuenta.setTipocuenta(Cuenta.TipoCuenta.valueOf(event.getTipoCuenta()));
        nuevaCuenta.setSaldoinicial(event.getSaldoInicial());
        nuevaCuenta.setSaldodisponible(event.getSaldoInicial());
        nuevaCuenta.setEstado(true);
        nuevaCuenta.setFechacreacion(LocalDateTime.now());
        
        return nuevaCuenta;
    }
    
    private Movimiento crearNuevoMovimiento(OnboardingEventDTO event, Cuenta cuenta) {
        Movimiento nuevoMovimiento = new Movimiento();
        nuevoMovimiento.setCuenta(cuenta);
        nuevoMovimiento.setTipomovimiento(Movimiento.TipoMovimiento.valueOf(event.getTipoMovimiento()));
        nuevoMovimiento.setMontomovimiento(event.getMontoMovimiento());
        nuevoMovimiento.setMovimientodescripcion(event.getMovimientoDescripcion());
        nuevoMovimiento.setEstado(true);
        
        return nuevoMovimiento;
    }
    
    private void cerrarCuentaPorError(String numeroCuentaStr, String transactionId) {
        try {
            Integer numeroCuenta = Integer.valueOf(numeroCuentaStr);
            Cuenta cuenta = cuentaService.findByNumeroCuenta(numeroCuenta);
            if (cuenta != null) {
                cuenta.setEstado(false);
                cuenta.setFechacierre(LocalDateTime.now());
                cuentaService.saveOrUpdate(cuenta);
                log.info("[COMPENSACION] Cuenta {} cerrada por error en transactionId: {}", numeroCuenta, transactionId);
            }
        } catch (Exception e) {
            log.error("[COMPENSACION] Error al cerrar cuenta {} para transactionId: {}", numeroCuentaStr, transactionId, e);
        }
    }
    
    private void enviarEventoMovimientoInicial(OnboardingEventDTO cuentaEvent) {
        OnboardingEventDTO movimientoEvent = OnboardingEventDTO.createMovimientoEvent(
            cuentaEvent.getTransactionId(),
            cuentaEvent.getNumeroCuenta(),
            "DEPOSITO",
            cuentaEvent.getSaldoInicial(),
            "Depósito inicial - Apertura de cuenta"
        );
        
        enviarMensajeKafka(KafkaTopicConfig.ONBOARDING_MOVIMIENTO, cuentaEvent.getTransactionId(), movimientoEvent);
    }
    
    private void enviarEventoCompletado(String transactionId) {
        OnboardingEventDTO completedEvent = OnboardingEventDTO.createCompletedEvent(transactionId);
        enviarMensajeKafka(KafkaTopicConfig.ONBOARDING_COMPLETED, transactionId, completedEvent);
        log.info("[COMPLETED] Onboarding completado exitosamente para transactionId: {}", transactionId);
    }
    
    private void enviarEventoRollback(String transactionId, String failedStep, String errorMessage) {
        OnboardingEventDTO rollbackEvent = OnboardingEventDTO.createRollbackEvent(
            transactionId, failedStep, errorMessage, "DISABLE"
        );
        enviarMensajeKafka(KafkaTopicConfig.ONBOARDING_ROLLBACK, transactionId, rollbackEvent);
        log.info("[ROLLBACK] Mensaje de rollback enviado para transactionId: {}", transactionId);
    }
    
    private void enviarMensajeKafka(String topic, String key, OnboardingEventDTO event) {
        try {
            kafkaTemplate.send(topic, key, event).get(30, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("Error al enviar mensaje a tópico {}: {}", topic, e.getMessage(), e);
            throw new RuntimeException("Error al enviar mensaje Kafka", e);
        }
    }
}
