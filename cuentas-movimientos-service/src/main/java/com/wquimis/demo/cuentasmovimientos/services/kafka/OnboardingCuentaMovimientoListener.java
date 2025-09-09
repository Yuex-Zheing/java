package com.wquimis.demo.cuentasmovimientos.services.kafka;

import com.wquimis.demo.cuentasmovimientos.config.KafkaTopicConfig;
import com.wquimis.demo.cuentasmovimientos.dto.kafka.OnboardingEventDTO;
import com.wquimis.demo.cuentasmovimientos.dto.kafka.CuentaEventDTO;
import com.wquimis.demo.cuentasmovimientos.dto.kafka.KafkaEventDTO;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class OnboardingCuentaMovimientoListener {

    private final CuentaService cuentaService;
    private final MovimientoService movimientoService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * Procesa eventos de creación de cuenta usando CuentaEventDTO
     */
    @KafkaListener(topics = KafkaTopicConfig.ONBOARDING_CUENTA, groupId = "cuentas-movimientos-cuenta-group")
    public void procesarCreacionCuenta(@Payload CuentaEventDTO event,
                                     @Header(KafkaHeaders.RECEIVED_KEY) String transactionId,
                                     Acknowledgment acknowledgment) {
        
        log.info("[CUENTA] Procesando creación de cuenta para transactionId: {}", transactionId);
        
        try {
            // Convertir CuentaEventDTO a OnboardingEventDTO para compatibilidad con métodos existentes
            OnboardingEventDTO onboardingEvent = convertirCuentaEventDTO(event);
            
            // Asegurar que tenga tipo de evento correcto
            if (onboardingEvent.getEventType() == null) {
                onboardingEvent.setEventType("CUENTA");
            }
            
            // Delegar al método interno
            procesarCreacionCuentaInterno(onboardingEvent, transactionId, acknowledgment);
            
        } catch (Exception e) {
            log.error("[CUENTA] Error al procesar KafkaEventDTO para transactionId: {}", transactionId, e);
            enviarEventoRollback(transactionId, "CUENTA", e.getMessage());
            acknowledgment.acknowledge();
        }
    }

    /**
     * Procesa eventos de creación de cuenta usando CuentaEventDTO (compatibilidad)
     */
    @KafkaListener(topics = KafkaTopicConfig.ONBOARDING_CUENTA, groupId = "cuentas-movimientos-cuenta-legacy-group")
    public void procesarCreacionCuentaLegacy(@Payload CuentaEventDTO event,
                                           @Header(KafkaHeaders.RECEIVED_KEY) String transactionId,
                                           Acknowledgment acknowledgment) {
        
        log.info("[CUENTA-LEGACY] Procesando creación de cuenta con CuentaEventDTO para transactionId: {}", transactionId);
        
        try {
            // Convertir CuentaEventDTO a OnboardingEventDTO
            OnboardingEventDTO onboardingEvent = convertirCuentaEventDTO(event);
            procesarCreacionCuentaInterno(onboardingEvent, transactionId, acknowledgment);
            
        } catch (Exception e) {
            log.error("[CUENTA-LEGACY] Error al procesar creación de cuenta para transactionId: {}", transactionId, e);
            enviarEventoRollback(transactionId, "CUENTA", e.getMessage());
            acknowledgment.acknowledge();
        }
    }

    /**
     * Procesa eventos de creación de movimiento usando CuentaEventDTO
     */
    @KafkaListener(topics = KafkaTopicConfig.ONBOARDING_MOVIMIENTO, groupId = "cuentas-movimientos-movimiento-group")
    public void procesarCreacionMovimiento(@Payload CuentaEventDTO event,
                                         @Header(KafkaHeaders.RECEIVED_KEY) String transactionId,
                                         Acknowledgment acknowledgment) {
        
        log.info("[MOVIMIENTO] Procesando creación de movimiento para transactionId: {}", transactionId);
        
        try {
            // Convertir CuentaEventDTO a OnboardingEventDTO para compatibilidad con métodos existentes
            OnboardingEventDTO onboardingEvent = convertirCuentaEventDTO(event);
            
            // Asegurar que tenga tipo de evento correcto
            if (onboardingEvent.getEventType() == null) {
                onboardingEvent.setEventType("MOVIMIENTO");
            }
            
            // Delegar al método interno de movimiento
            procesarCreacionMovimientoInterno(onboardingEvent, transactionId, acknowledgment);
            
        } catch (Exception e) {
            log.error("[MOVIMIENTO] Error al procesar KafkaEventDTO para transactionId: {}", transactionId, e);
            enviarEventoRollback(transactionId, "MOVIMIENTO", e.getMessage());
            acknowledgment.acknowledge();
        }
    }

    /**
     * Procesa eventos de rollback usando CuentaEventDTO
     */
    @KafkaListener(topics = KafkaTopicConfig.ONBOARDING_ROLLBACK, groupId = "cuentas-movimientos-rollback-group")
    public void procesarRollback(@Payload CuentaEventDTO event,
                               @Header(KafkaHeaders.RECEIVED_KEY) String transactionId,
                               Acknowledgment acknowledgment) {
        
        log.info("[ROLLBACK] Procesando rollback para transactionId: {}", transactionId);
        
        try {
            // Solo procesar rollbacks relacionados con cuentas y movimientos
            if (event.getNumeroCuenta() != null) {
                try {
                    cerrarCuentaPorError(event.getNumeroCuenta().toString(), transactionId);
                    log.info("[ROLLBACK] Cuenta {} marcada como cerrada para transactionId: {}", 
                             event.getNumeroCuenta(), transactionId);
                } catch (Exception e) {
                    log.warn("[ROLLBACK] No se pudo cerrar cuenta {} para transactionId: {} - Error: {}", 
                             event.getNumeroCuenta(), transactionId, e.getMessage());
                }
            }
            
            acknowledgment.acknowledge();
            
        } catch (Exception e) {
            log.error("[ROLLBACK] Error al procesar rollback para transactionId: {}", transactionId, e);
            acknowledgment.acknowledge(); // Acknowledge para evitar loops infinitos
        }
    }

    /**
     * Método interno que procesa la creación de movimiento
     */
    private void procesarCreacionMovimientoInterno(OnboardingEventDTO event, String transactionId, Acknowledgment acknowledgment) {
        // Validar que el evento sea del tipo correcto
        if (!"MOVIMIENTO".equals(event.getEventType())) {
            log.warn("[MOVIMIENTO] Evento recibido no es de tipo MOVIMIENTO: {} para transactionId: {}", 
                     event.getEventType(), transactionId);
            acknowledgment.acknowledge();
            return;
        }
        
        // Validar datos necesarios
        if (event.getNumeroCuenta() == null || event.getNumeroCuenta().trim().isEmpty()) {
            log.error("[MOVIMIENTO] Evento sin número de cuenta para transactionId: {}", transactionId);
            enviarEventoRollback(transactionId, "MOVIMIENTO", "Número de cuenta requerido");
            acknowledgment.acknowledge();
            return;
        }
        
        if (event.getMontoMovimiento() == null || event.getMontoMovimiento().compareTo(BigDecimal.ZERO) <= 0) {
            log.error("[MOVIMIENTO] Evento sin monto válido para transactionId: {}", transactionId);
            enviarEventoRollback(transactionId, "MOVIMIENTO", "Monto de movimiento requerido");
            acknowledgment.acknowledge();
            return;
        }
        
        // Verificar que la cuenta exista
        if (!verificarCuentaExistente(event.getNumeroCuenta())) {
            log.error("[MOVIMIENTO] Cuenta no existe: {} para transactionId: {}", 
                     event.getNumeroCuenta(), transactionId);
            enviarEventoRollback(transactionId, "MOVIMIENTO", "Cuenta no encontrada");
            acknowledgment.acknowledge();
            return;
        }
        
        // Crear movimiento
        log.info("[MOVIMIENTO] Creando movimiento para cuenta: {} con monto: {} para transactionId: {}", 
                 event.getNumeroCuenta(), event.getMontoMovimiento(), transactionId);
        
        Cuenta cuenta = buscarCuentaPorNumero(event.getNumeroCuenta());
        if (cuenta != null) {
            Movimiento movimientoCreado = crearMovimientoTransaccional(event, cuenta);
            
            if (movimientoCreado != null) {
                log.info("[MOVIMIENTO] Movimiento creado exitosamente con ID: {} para transactionId: {}", 
                         movimientoCreado.getIdmovimiento(), transactionId);
                
                // Enviar evento de completado exitoso
                enviarEventoCompletado(transactionId);
            } else {
                log.error("[MOVIMIENTO] Error al crear movimiento para transactionId: {}", transactionId);
                enviarEventoRollback(transactionId, "MOVIMIENTO", "Error en la creación del movimiento");
            }
        } else {
            log.error("[MOVIMIENTO] No se pudo encontrar la cuenta para transactionId: {}", transactionId);
            enviarEventoRollback(transactionId, "MOVIMIENTO", "Cuenta no encontrada en el procesamiento");
        }
        acknowledgment.acknowledge();
    }
    
    /**
     * Método interno que procesa la creación de cuenta
    /**
     * Método interno que procesa la creación de cuenta
     */
    private void procesarCreacionCuentaInterno(OnboardingEventDTO event,
                                             String transactionId,
                                             Acknowledgment acknowledgment) {
        
        try {
            // Validar que el evento sea del tipo correcto (para OnboardingEventDTO)
            if (event.getEventType() != null && !"CUENTA".equals(event.getEventType())) {
                log.warn("[CUENTA] Evento recibido no es de tipo CUENTA: {} para transactionId: {}", 
                         event.getEventType(), transactionId);
                acknowledgment.acknowledge();
                return;
            }
            
            // Validar datos necesarios
            if (event.getNumeroCuenta() == null || event.getNumeroCuenta().trim().isEmpty()) {
                log.error("[CUENTA] Evento sin número de cuenta para transactionId: {}", transactionId);
                enviarEventoRollback(transactionId, "CUENTA", "Número de cuenta requerido");
                acknowledgment.acknowledge();
                return;
            }
            
            if (event.getClienteId() == null) {
                log.error("[CUENTA] Evento sin cliente ID para transactionId: {}", transactionId);
                enviarEventoRollback(transactionId, "CUENTA", "Cliente ID requerido");
                acknowledgment.acknowledge();
                return;
            }
            
            // Crear cuenta (con verificación interna atómica)
            log.info("[CUENTA] Procesando creación de cuenta para transactionId: {}", transactionId);
            Cuenta cuentaCreada = crearCuentaTransaccional(event);
            
            if (cuentaCreada != null) {
                log.info("[CUENTA] Cuenta procesada exitosamente con número: {} para transactionId: {}", 
                         cuentaCreada.getNumerocuenta(), transactionId);
                
                // Crear movimiento inicial solo si la cuenta tiene saldo inicial
                if (cuentaCreada.getSaldoinicial() != null && cuentaCreada.getSaldoinicial().compareTo(BigDecimal.ZERO) > 0) {
                    try {
                        crearMovimientoInicial(cuentaCreada, event);
                        log.info("[CUENTA] Movimiento inicial creado para cuenta: {} con saldo: {}", 
                                cuentaCreada.getNumerocuenta(), cuentaCreada.getSaldoinicial());
                    } catch (Exception e) {
                        log.error("[CUENTA] Error al crear movimiento inicial para cuenta: {} - {}", 
                                cuentaCreada.getNumerocuenta(), e.getMessage());
                        // No hacer rollback de la cuenta por error en movimiento inicial
                    }
                }
                
                enviarEventoCompletado(transactionId);
            } else {
                log.error("[CUENTA] Error al procesar cuenta para transactionId: {}", transactionId);
                enviarEventoRollback(transactionId, "CUENTA", "Error en la creación de la cuenta");
            }
            
            acknowledgment.acknowledge();
            
        } catch (Exception e) {
            log.error("[CUENTA] Error al procesar creación de cuenta para transactionId: {}", transactionId, e);
            enviarEventoRollback(transactionId, "CUENTA", e.getMessage());
            acknowledgment.acknowledge();
        }
    }



    // ========== MÉTODOS DE CONVERSIÓN ==========
    
    private OnboardingEventDTO convertirCuentaEventDTO(CuentaEventDTO cuentaEvent) {
        OnboardingEventDTO event = new OnboardingEventDTO();
        event.setTransactionId(cuentaEvent.getTransactionId());
        event.setEventType("CUENTA");
        event.setStatus("PENDING");
        event.setTimestamp(cuentaEvent.getTimestamp() != null ? cuentaEvent.getTimestamp() : LocalDateTime.now());
        event.setRetryCount(0);
        
        event.setClienteId(cuentaEvent.getClienteId());
        event.setNumeroCuenta(cuentaEvent.getNumeroCuenta() != null ? cuentaEvent.getNumeroCuenta().toString() : null);
        event.setTipoCuenta(cuentaEvent.getTipoCuenta());
        event.setSaldoInicial(cuentaEvent.getSaldoInicial());
        event.setCuentaEstado(true);
        
        return event;
    }

    // ========== MÉTODOS AUXILIARES ==========
    
    private boolean verificarCuentaExistente(String numeroCuenta) {
        try {
            Integer numeroCuentaInt = Integer.valueOf(numeroCuenta);
            Cuenta cuenta = cuentaService.findByNumeroCuenta(numeroCuentaInt);
            boolean existe = cuenta != null;
            log.debug("[CUENTA] Verificación de cuenta {}: existe={}", numeroCuenta, existe);
            return existe;
        } catch (NumberFormatException e) {
            log.error("[CUENTA] Error al convertir número de cuenta '{}': {}", numeroCuenta, e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("[CUENTA] Error al verificar existencia de cuenta {}: {}", numeroCuenta, e.getMessage());
            return false;
        }
    }
    
    @Transactional
    public Cuenta crearCuentaTransaccional(OnboardingEventDTO event) {
        try {
            Integer numeroCuentaInt = Integer.valueOf(event.getNumeroCuenta());
            
            // Verificación atómica dentro de la transacción
            Cuenta cuentaExistente = cuentaService.findByNumeroCuenta(numeroCuentaInt);
            if (cuentaExistente != null) {
                log.info("[CUENTA] Cuenta ya existe con número: {} para cliente: {}", 
                        numeroCuentaInt, event.getClienteId());
                return cuentaExistente;
            }
            
            // Crear nueva cuenta
            Cuenta nuevaCuenta = new Cuenta();
            nuevaCuenta.setNumerocuenta(numeroCuentaInt);
            nuevaCuenta.setIdcliente(event.getClienteId());
            nuevaCuenta.setTipocuenta(Cuenta.TipoCuenta.valueOf(event.getTipoCuenta()));
            nuevaCuenta.setSaldoinicial(event.getSaldoInicial() != null ? event.getSaldoInicial() : BigDecimal.ZERO);
            nuevaCuenta.setSaldodisponible(event.getSaldoInicial() != null ? event.getSaldoInicial() : BigDecimal.ZERO);
            nuevaCuenta.setEstado(true);
            nuevaCuenta.setFechacreacion(LocalDateTime.now());
            
            Cuenta cuentaCreada = cuentaService.save(nuevaCuenta);
            log.info("[CUENTA] Cuenta creada exitosamente con ID: {} para cliente: {}", 
                    cuentaCreada.getNumerocuenta(), event.getClienteId());
            
            return cuentaCreada;
            
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            // Si hay violación de integridad, probablemente la cuenta ya existe
            log.warn("[CUENTA] Cuenta duplicada detectada para número: {}, verificando existencia...", event.getNumeroCuenta());
            try {
                Integer numeroCuentaInt = Integer.valueOf(event.getNumeroCuenta());
                Cuenta cuentaExistente = cuentaService.findByNumeroCuenta(numeroCuentaInt);
                if (cuentaExistente != null) {
                    log.info("[CUENTA] Cuenta confirmada como existente: {} para cliente: {}", 
                            numeroCuentaInt, event.getClienteId());
                    return cuentaExistente;
                }
            } catch (Exception ex) {
                log.error("[CUENTA] Error al verificar cuenta existente tras violación de integridad: {}", ex.getMessage());
            }
            throw e;
        } catch (Exception e) {
            log.error("[CUENTA] Error al crear cuenta para cliente {}: {}", event.getClienteId(), e.getMessage());
            throw e;
        }
    }
    
    /**
     * Crea un movimiento inicial para una cuenta recién creada
     */
    @Transactional
    private void crearMovimientoInicial(Cuenta cuenta, OnboardingEventDTO event) {
        try {
            Movimiento movimientoInicial = new Movimiento();
            movimientoInicial.setCuenta(cuenta);
            movimientoInicial.setTipomovimiento(Movimiento.TipoMovimiento.DEPOSITO);
            movimientoInicial.setMontomovimiento(cuenta.getSaldoinicial());
            movimientoInicial.setSaldodisponible(cuenta.getSaldoinicial());
            movimientoInicial.setFechamovimiento(LocalDate.now());
            movimientoInicial.setHoramovimiento(LocalTime.now());
            movimientoInicial.setMovimientodescripcion("Depósito inicial de cuenta");
            movimientoInicial.setEstado(true);
            
            Movimiento movimientoCreado = movimientoService.save(movimientoInicial);
            log.info("[MOVIMIENTO_INICIAL] Movimiento inicial creado con ID: {} para cuenta: {} con valor: {}", 
                    movimientoCreado.getIdmovimiento(), cuenta.getNumerocuenta(), cuenta.getSaldoinicial());
                    
        } catch (Exception e) {
            log.error("[MOVIMIENTO_INICIAL] Error al crear movimiento inicial para cuenta {}: {}", 
                     cuenta.getNumerocuenta(), e.getMessage());
            throw e;
        }
    }
    
    private Cuenta buscarCuentaPorNumero(String numeroCuenta) {
        try {
            Integer numeroCuentaInt = Integer.valueOf(numeroCuenta);
            return cuentaService.findByNumeroCuenta(numeroCuentaInt);
        } catch (Exception e) {
            log.error("[CUENTA] Error al buscar cuenta {}: {}", numeroCuenta, e.getMessage());
            return null;
        }
    }
    
    @Transactional
    public Movimiento crearMovimientoTransaccional(OnboardingEventDTO event, Cuenta cuenta) {
        try {
            Movimiento nuevoMovimiento = new Movimiento();
            nuevoMovimiento.setCuenta(cuenta);
            nuevoMovimiento.setTipomovimiento(Movimiento.TipoMovimiento.valueOf(event.getTipoMovimiento()));
            nuevoMovimiento.setMontomovimiento(event.getMontoMovimiento());
            
            // Calcular nuevo saldo
            BigDecimal nuevoSaldo;
            if ("DEPOSITO".equals(event.getTipoMovimiento())) {
                nuevoSaldo = cuenta.getSaldodisponible().add(event.getMontoMovimiento());
            } else {
                nuevoSaldo = cuenta.getSaldodisponible().subtract(event.getMontoMovimiento());
            }
            
            nuevoMovimiento.setSaldodisponible(nuevoSaldo);
            nuevoMovimiento.setMovimientodescripcion(event.getMovimientoDescripcion());
            nuevoMovimiento.setFechamovimiento(LocalDate.now());
            nuevoMovimiento.setHoramovimiento(LocalTime.now());
            nuevoMovimiento.setEstado(true);
            
            // Actualizar saldo de la cuenta
            cuenta.setSaldodisponible(nuevoSaldo);
            cuentaService.save(cuenta);
            
            Movimiento movimientoCreado = movimientoService.save(nuevoMovimiento);
            log.info("[MOVIMIENTO] Movimiento creado exitosamente con ID: {} para cuenta: {}", 
                    movimientoCreado.getIdmovimiento(), cuenta.getNumerocuenta());
            
            return movimientoCreado;
            
        } catch (Exception e) {
            log.error("[MOVIMIENTO] Error al crear movimiento para cuenta {}: {}", cuenta.getNumerocuenta(), e.getMessage());
            throw e;
        }
    }
    
    private void cerrarCuentaPorError(String numeroCuentaStr, String transactionId) {
        try {
            Integer numeroCuenta = Integer.valueOf(numeroCuentaStr);
            Cuenta cuenta = cuentaService.findByNumeroCuenta(numeroCuenta);
            if (cuenta != null) {
                cuenta.setEstado(false);
                cuenta.setFechacierre(LocalDateTime.now());
                cuentaService.save(cuenta);
                log.info("[COMPENSACION] Cuenta {} cerrada por error en transactionId: {}", numeroCuenta, transactionId);
            }
        } catch (Exception e) {
            log.error("[COMPENSACION] Error al cerrar cuenta {} para transactionId: {}", numeroCuentaStr, transactionId, e);
        }
    }
    
    private void enviarEventoMovimientoInicial(KafkaEventDTO cuentaEvent) {
        // Crear evento de movimiento usando CuentaEventDTO para compatibilidad
        CuentaEventDTO movimientoEvent = new CuentaEventDTO();
        movimientoEvent.setTransactionId(cuentaEvent.getTransactionId());
        movimientoEvent.setClienteId(cuentaEvent.getClienteId());
        movimientoEvent.setNumeroCuenta(cuentaEvent.getNumeroCuenta() != null ? Integer.valueOf(cuentaEvent.getNumeroCuenta()) : null);
        movimientoEvent.setSaldoInicial(cuentaEvent.getSaldoInicial());
        movimientoEvent.setTimestamp(LocalDateTime.now());
        
        enviarMensajeCuentaEventDTO(KafkaTopicConfig.ONBOARDING_MOVIMIENTO, cuentaEvent.getTransactionId(), movimientoEvent);
    }
    
    private OnboardingEventDTO crearEventoMovimiento(String transactionId, String numeroCuenta, 
                                                   String tipoMovimiento, BigDecimal monto, String descripcion) {
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
    
    private void enviarEventoCompletado(String transactionId) {
        KafkaEventDTO completedEvent = KafkaEventDTO.createCompletedEvent(transactionId);
        
        enviarMensajeKafkaUnificado(KafkaTopicConfig.ONBOARDING_COMPLETED, transactionId, completedEvent);
        log.info("[COMPLETED] Onboarding completado exitosamente para transactionId: {}", transactionId);
    }
    
    private void enviarEventoRollback(String transactionId, String failedStep, String errorMessage) {
        KafkaEventDTO rollbackEvent = KafkaEventDTO.createRollbackEvent(transactionId, failedStep, errorMessage);
        
        enviarMensajeKafkaUnificado(KafkaTopicConfig.ONBOARDING_ROLLBACK, transactionId, rollbackEvent);
        log.info("[ROLLBACK] Mensaje de rollback enviado para transactionId: {}", transactionId);
    }
    
    private void enviarMensajeKafka(String topic, String key, OnboardingEventDTO event) {
        try {
            kafkaTemplate.send(topic, key, event);
            log.debug("[KAFKA] Mensaje enviado al topic: {} con key: {}", topic, key);
        } catch (Exception e) {
            log.error("[KAFKA] Error al enviar mensaje al topic: {} con key: {}", topic, key, e);
            throw new RuntimeException("Error al enviar mensaje Kafka", e);
        }
    }
    
    private void enviarMensajeKafkaUnificado(String topic, String key, KafkaEventDTO event) {
        try {
            kafkaTemplate.send(topic, key, event);
            log.debug("[KAFKA] Mensaje KafkaEventDTO enviado al topic: {} con key: {}", topic, key);
        } catch (Exception e) {
            log.error("[KAFKA] Error al enviar mensaje KafkaEventDTO al topic: {} con key: {}", topic, key, e);
            throw new RuntimeException("Error al enviar mensaje Kafka", e);
        }
    }
    
    private void enviarMensajeCuentaEventDTO(String topic, String key, CuentaEventDTO event) {
        try {
            kafkaTemplate.send(topic, key, event);
            log.debug("[KAFKA] Mensaje CuentaEventDTO enviado al topic: {} con key: {}", topic, key);
        } catch (Exception e) {
            log.error("[KAFKA] Error al enviar mensaje CuentaEventDTO al topic: {} con key: {}", topic, key, e);
            throw new RuntimeException("Error al enviar mensaje Kafka", e);
        }
    }
}
