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
     * Procesa eventos de creación de cuenta usando deserialización flexible
     */
    @KafkaListener(topics = KafkaTopicConfig.ONBOARDING_CUENTA, groupId = "cuentas-movimientos-cuenta-group")
    public void procesarCreacionCuenta(@Payload Object rawEvent,
                                     @Header(KafkaHeaders.RECEIVED_KEY) String transactionId,
                                     Acknowledgment acknowledgment) {
        
        log.info("[CUENTA] Procesando creación de cuenta para transactionId: {}", transactionId);
        
        try {
            // Convertir el evento recibido al formato interno
            OnboardingEventDTO onboardingEvent = convertirEventoFlexible(rawEvent, transactionId);
            
            if (onboardingEvent == null) {
                log.error("[CUENTA] No se pudo convertir el evento para transactionId: {}", transactionId);
                enviarEventoRollback(transactionId, "CUENTA", "Formato de evento no válido");
                acknowledgment.acknowledge();
                return;
            }
            
            // Asegurar que tenga tipo de evento correcto
            if (onboardingEvent.getEventType() == null) {
                onboardingEvent.setEventType("CUENTA");
            }
            
            // Delegar al método interno
            procesarCreacionCuentaInterno(onboardingEvent, transactionId, acknowledgment);
            
        } catch (Exception e) {
            log.error("[CUENTA] Error al procesar evento para transactionId: {}", transactionId, e);
            enviarEventoRollback(transactionId, "CUENTA", e.getMessage());
            acknowledgment.acknowledge();
        }
    }



    /**
     * Procesa eventos de creación de movimiento usando deserialización flexible
     */
    @KafkaListener(topics = KafkaTopicConfig.ONBOARDING_MOVIMIENTO, groupId = "cuentas-movimientos-movimiento-group")
    public void procesarCreacionMovimiento(@Payload Object rawEvent,
                                         @Header(KafkaHeaders.RECEIVED_KEY) String transactionId,
                                         Acknowledgment acknowledgment) {
        
        log.info("[MOVIMIENTO] Procesando creación de movimiento para transactionId: {}", transactionId);
        
        try {
            // Convertir el evento recibido al formato interno
            OnboardingEventDTO onboardingEvent = convertirEventoFlexible(rawEvent, transactionId);
            
            if (onboardingEvent == null) {
                log.error("[MOVIMIENTO] No se pudo convertir el evento para transactionId: {}", transactionId);
                enviarEventoRollback(transactionId, "MOVIMIENTO", "Formato de evento no válido");
                acknowledgment.acknowledge();
                return;
            }
            
            // Asegurar que tenga tipo de evento correcto
            if (onboardingEvent.getEventType() == null) {
                onboardingEvent.setEventType("MOVIMIENTO");
            }
            
            // Delegar al método interno de movimiento
            procesarCreacionMovimientoInterno(onboardingEvent, transactionId, acknowledgment);
            
        } catch (Exception e) {
            log.error("[MOVIMIENTO] Error al procesar evento para transactionId: {}", transactionId, e);
            enviarEventoRollback(transactionId, "MOVIMIENTO", e.getMessage());
            acknowledgment.acknowledge();
        }
    }

    /**
     * Procesa eventos de rollback usando deserialización flexible
     */
    @KafkaListener(topics = KafkaTopicConfig.ONBOARDING_ROLLBACK, groupId = "cuentas-movimientos-rollback-group")
    public void procesarRollback(@Payload Object rawEvent,
                               @Header(KafkaHeaders.RECEIVED_KEY) String transactionId,
                               Acknowledgment acknowledgment) {
        
        log.info("[ROLLBACK] Procesando rollback para transactionId: {}", transactionId);
        
        try {
            // Convertir el evento recibido al formato interno
            OnboardingEventDTO event = convertirEventoFlexible(rawEvent, transactionId);
            
            // Solo procesar rollbacks relacionados con cuentas y movimientos
            if (event != null && event.getNumeroCuenta() != null && !event.getNumeroCuenta().trim().isEmpty()) {
                try {
                    cerrarCuentaPorError(event.getNumeroCuenta(), transactionId);
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
            log.info("[CUENTA] Iniciando creación de cuenta - Cliente: {}, Número: {}, Tipo: {}, Saldo: {} (TransactionId: {})", 
                     event.getClienteId(), event.getNumeroCuenta(), event.getTipoCuenta(), event.getSaldoInicial(), transactionId);
            Cuenta cuentaCreada = crearCuentaTransaccional(event);
            
            if (cuentaCreada != null) {
                log.info("[CUENTA] ✓ Cuenta procesada exitosamente - Número: {}, Estado: {}, Saldo disponible: {} (TransactionId: {})", 
                         cuentaCreada.getNumerocuenta(), cuentaCreada.getEstado() ? "ACTIVA" : "INACTIVA", 
                         cuentaCreada.getSaldodisponible(), transactionId);
                
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
    
    /**
     * Convierte eventos de diferentes tipos a OnboardingEventDTO
     */
    private OnboardingEventDTO convertirEventoFlexible(Object rawEvent, String transactionId) {
        try {
            if (rawEvent instanceof OnboardingEventDTO) {
                // Ya es el tipo correcto
                return (OnboardingEventDTO) rawEvent;
            }
            
            if (rawEvent instanceof CuentaEventDTO) {
                // Convertir desde CuentaEventDTO
                return convertirCuentaEventDTO((CuentaEventDTO) rawEvent);
            }
            
            if (rawEvent instanceof java.util.LinkedHashMap) {
                // Deserializar desde Map (común cuando se recibe JSON genérico)
                return convertirDesdeMap((java.util.LinkedHashMap<?, ?>) rawEvent, transactionId);
            }
            
            log.warn("[CONVERSION] Tipo de evento no reconocido: {} para transactionId: {}", 
                    rawEvent.getClass().getSimpleName(), transactionId);
            return null;
            
        } catch (Exception e) {
            log.error("[CONVERSION] Error al convertir evento para transactionId: {}", transactionId, e);
            return null;
        }
    }
    
    /**
     * Convierte un Map (JSON deserializado) a OnboardingEventDTO
     */
    private OnboardingEventDTO convertirDesdeMap(java.util.LinkedHashMap<?, ?> map, String transactionId) {
        try {
            OnboardingEventDTO event = new OnboardingEventDTO();
            
            // Campos básicos
            event.setTransactionId(getMapValue(map, "transactionId", String.class, transactionId));
            event.setEventType(getMapValue(map, "eventType", String.class, "CUENTA"));
            event.setStatus(getMapValue(map, "status", String.class, "PENDING"));
            event.setRetryCount(getMapValue(map, "retryCount", Integer.class, 0));
            
            // Timestamp
            Object timestampObj = map.get("timestamp");
            if (timestampObj != null) {
                if (timestampObj instanceof String) {
                    event.setTimestamp(LocalDateTime.parse((String) timestampObj));
                } else {
                    event.setTimestamp(LocalDateTime.now());
                }
            } else {
                event.setTimestamp(LocalDateTime.now());
            }
            
            // Datos de cliente
            event.setClienteId(getMapValue(map, "clienteId", Long.class, null));
            
            // Datos de cuenta
            Object numeroCuentaObj = map.get("numeroCuenta");
            if (numeroCuentaObj != null) {
                if (numeroCuentaObj instanceof Integer) {
                    event.setNumeroCuenta(numeroCuentaObj.toString());
                } else if (numeroCuentaObj instanceof String) {
                    event.setNumeroCuenta((String) numeroCuentaObj);
                }
            }
            
            event.setTipoCuenta(getMapValue(map, "tipoCuenta", String.class, null));
            event.setSaldoInicial(getMapValue(map, "saldoInicial", java.math.BigDecimal.class, null));
            event.setCuentaEstado(getMapValue(map, "cuentaEstado", Boolean.class, true));
            
            // Datos de movimiento
            event.setTipoMovimiento(getMapValue(map, "tipoMovimiento", String.class, null));
            event.setMontoMovimiento(getMapValue(map, "montoMovimiento", java.math.BigDecimal.class, null));
            event.setMovimientoDescripcion(getMapValue(map, "movimientoDescripcion", String.class, null));
            
            return event;
            
        } catch (Exception e) {
            log.error("[CONVERSION] Error al convertir desde Map para transactionId: {}", transactionId, e);
            return null;
        }
    }
    
    /**
     * Extrae un valor del Map con el tipo esperado
     */
    @SuppressWarnings("unchecked")
    private <T> T getMapValue(java.util.LinkedHashMap<?, ?> map, String key, Class<T> type, T defaultValue) {
        try {
            Object value = map.get(key);
            if (value == null) {
                return defaultValue;
            }
            
            if (type.isInstance(value)) {
                return (T) value;
            }
            
            // Conversiones especiales
            if (type == Long.class && value instanceof Integer) {
                return (T) Long.valueOf(((Integer) value).longValue());
            }
            if (type == Integer.class && value instanceof Long) {
                return (T) Integer.valueOf(((Long) value).intValue());
            }
            if (type == java.math.BigDecimal.class && value instanceof Number) {
                return (T) new java.math.BigDecimal(value.toString());
            }
            
            return defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }
    
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
            // Validaciones de entrada mejoradas
            if (event.getNumeroCuenta() == null || event.getNumeroCuenta().trim().isEmpty()) {
                throw new IllegalArgumentException("Número de cuenta es requerido");
            }
            if (event.getClienteId() == null) {
                throw new IllegalArgumentException("ID de cliente es requerido");
            }
            if (event.getTipoCuenta() == null || event.getTipoCuenta().trim().isEmpty()) {
                throw new IllegalArgumentException("Tipo de cuenta es requerido");
            }
            
            Integer numeroCuentaInt = Integer.valueOf(event.getNumeroCuenta());
            
            // Verificación atómica dentro de la transacción
            try {
                Cuenta cuentaExistente = cuentaService.findByNumeroCuenta(numeroCuentaInt);
                if (cuentaExistente != null) {
                    log.info("[CUENTA] Cuenta ya existe con número: {} para cliente: {} - usando existente", 
                            numeroCuentaInt, event.getClienteId());
                    return cuentaExistente;
                }
            } catch (jakarta.persistence.EntityNotFoundException e) {
                // Cuenta no existe, continuamos con la creación
                log.debug("[CUENTA] Cuenta no existe, procediendo a crear para número: {}", numeroCuentaInt);
            }
            
            // Crear nueva cuenta con validaciones mejoradas
            Cuenta nuevaCuenta = new Cuenta();
            nuevaCuenta.setNumerocuenta(numeroCuentaInt);
            nuevaCuenta.setIdcliente(event.getClienteId());
            
            // Validar tipo de cuenta
            try {
                nuevaCuenta.setTipocuenta(Cuenta.TipoCuenta.valueOf(event.getTipoCuenta().toUpperCase()));
            } catch (IllegalArgumentException e) {
                log.error("[CUENTA] Tipo de cuenta inválido: {} para transactionId", event.getTipoCuenta());
                throw new IllegalArgumentException("Tipo de cuenta inválido: " + event.getTipoCuenta());
            }
            
            BigDecimal saldoInicial = event.getSaldoInicial() != null ? event.getSaldoInicial() : BigDecimal.ZERO;
            if (saldoInicial.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("El saldo inicial no puede ser negativo");
            }
            
            nuevaCuenta.setSaldoinicial(saldoInicial);
            nuevaCuenta.setSaldodisponible(saldoInicial);
            nuevaCuenta.setEstado(true);
            nuevaCuenta.setFechacreacion(LocalDateTime.now());
            
            Cuenta cuentaCreada = cuentaService.save(nuevaCuenta);
            log.info("[CUENTA] Cuenta creada exitosamente - Número: {}, Cliente: {}, Saldo: {}", 
                    cuentaCreada.getNumerocuenta(), event.getClienteId(), saldoInicial);
            
            return cuentaCreada;
            
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            // Si hay violación de integridad, probablemente la cuenta ya existe
            log.warn("[CUENTA] Posible cuenta duplicada para número: {}, verificando...", event.getNumeroCuenta());
            try {
                Integer numeroCuentaInt = Integer.valueOf(event.getNumeroCuenta());
                Cuenta cuentaExistente = cuentaService.findByNumeroCuenta(numeroCuentaInt);
                if (cuentaExistente != null) {
                    log.info("[CUENTA] Confirmada cuenta existente tras intento de duplicación: {}", numeroCuentaInt);
                    return cuentaExistente;
                }
            } catch (Exception ex) {
                log.error("[CUENTA] Error al verificar cuenta existente tras violación de integridad", ex);
            }
            throw new RuntimeException("Error de integridad de datos al crear cuenta: " + e.getMessage(), e);
        } catch (NumberFormatException e) {
            log.error("[CUENTA] Número de cuenta inválido: {} para cliente: {}", event.getNumeroCuenta(), event.getClienteId());
            throw new IllegalArgumentException("Número de cuenta inválido: " + event.getNumeroCuenta(), e);
        } catch (Exception e) {
            log.error("[CUENTA] Error inesperado al crear cuenta para cliente {}: {}", event.getClienteId(), e.getMessage(), e);
            throw new RuntimeException("Error al crear cuenta: " + e.getMessage(), e);
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
        log.info("[COMPLETED] ✓ Proceso de cuenta finalizado exitosamente (TransactionId: {})", transactionId);
    }
    
    private void enviarEventoRollback(String transactionId, String failedStep, String errorMessage) {
        KafkaEventDTO rollbackEvent = KafkaEventDTO.createRollbackEvent(transactionId, failedStep, errorMessage);
        
        enviarMensajeKafkaUnificado(KafkaTopicConfig.ONBOARDING_ROLLBACK, transactionId, rollbackEvent);
        log.warn("[ROLLBACK] ✗ Rollback iniciado - Paso fallido: {}, Error: {} (TransactionId: {})", 
                failedStep, errorMessage, transactionId);
    }
    
    private void enviarMensajeKafka(String topic, String key, OnboardingEventDTO event) {
        try {
            kafkaTemplate.send(topic, key, event);
            log.info("[KAFKA] ✓ Mensaje enviado - Topic: {}, Tipo: {}, TransactionId: {}", 
                    topic, event.getEventType(), key);
        } catch (Exception e) {
            log.error("[KAFKA] ✗ Error al enviar mensaje - Topic: {}, TransactionId: {}", topic, key, e);
            throw new RuntimeException("Error al enviar mensaje Kafka", e);
        }
    }
    
    private void enviarMensajeKafkaUnificado(String topic, String key, KafkaEventDTO event) {
        try {
            kafkaTemplate.send(topic, key, event);
            log.info("[KAFKA] ✓ Mensaje enviado - Topic: {}, Tipo: {}, TransactionId: {}", 
                    topic, event.getEventType(), key);
        } catch (Exception e) {
            log.error("[KAFKA] ✗ Error al enviar mensaje - Topic: {}, TransactionId: {}", topic, key, e);
            throw new RuntimeException("Error al enviar mensaje Kafka", e);
        }
    }
    
    private void enviarMensajeCuentaEventDTO(String topic, String key, CuentaEventDTO event) {
        try {
            kafkaTemplate.send(topic, key, event);
            log.info("[KAFKA] ✓ Mensaje CuentaEventDTO enviado - Topic: {}, TransactionId: {}", topic, key);
        } catch (Exception e) {
            log.error("[KAFKA] ✗ Error al enviar mensaje CuentaEventDTO - Topic: {}, TransactionId: {}", topic, key, e);
            throw new RuntimeException("Error al enviar mensaje Kafka", e);
        }
    }
}
