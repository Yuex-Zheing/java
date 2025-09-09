package com.wquimis.demo.personasclientes.services.kafka;

import com.wquimis.demo.personasclientes.config.KafkaTopicConfig;
import com.wquimis.demo.personasclientes.dto.kafka.OnboardingEventDTO;
import com.wquimis.demo.personasclientes.entities.Cliente;
import com.wquimis.demo.personasclientes.entities.Persona;
import com.wquimis.demo.personasclientes.services.ClienteService;
import com.wquimis.demo.personasclientes.services.PersonaService;
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

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class OnboardingPersonaClienteListener {

    private final PersonaService personaService;
    private final ClienteService clienteService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * Procesa eventos de creación de persona con manejo transaccional
     */
    @KafkaListener(topics = KafkaTopicConfig.ONBOARDING_PERSONA_TOPIC, groupId = "personas-clientes-group")
    @Transactional
    public void procesarCreacionPersona(@Payload OnboardingEventDTO event,
                                      @Header(KafkaHeaders.RECEIVED_KEY) String transactionId,
                                      Acknowledgment acknowledgment) {
        
        log.info("[PERSONA] Procesando creación de persona para transactionId: {}", transactionId);
        
        try {
            // Validar que el evento tenga los datos necesarios de persona
            String identificacion = getPersonaIdentificacion(event);
            String nombre = getPersonaNombre(event);
            
            if (identificacion == null || identificacion.trim().isEmpty()) {
                log.warn("[PERSONA] Evento recibido sin identificación de persona para transactionId: {}", transactionId);
                acknowledgment.acknowledge();
                return;
            }
            
            // Verificar si la persona ya existe (idempotencia REAL)
            try {
                Persona personaExistente = personaService.findByIdentificacion(identificacion);
                log.info("[PERSONA] Persona ya existe con ID: {} para transactionId: {}", 
                        personaExistente.getIdpersona(), transactionId);
                
                // Solo continuar al siguiente paso, no crear duplicado
                enviarEventoCliente(transactionId, identificacion, nombre);
                acknowledgment.acknowledge();
                return;
                
            } catch (Exception e) {
                // Persona no existe, continuar con creación
                log.info("[PERSONA] Persona no existe, creando nueva para transactionId: {}", transactionId);
            }
            
            // Crear nueva persona solo si no existe
            log.info("[PERSONA] Creando nueva persona para transactionId: {}", transactionId);
            Persona nuevaPersona = crearNuevaPersonaFromEvent(event);
            Persona personaCreada = personaService.save(nuevaPersona);
            
            log.info("[PERSONA] Persona creada exitosamente con ID: {} para transactionId: {}", 
                    personaCreada.getIdpersona(), transactionId);
            
            // Enviar evento para crear cliente
            enviarEventoCliente(transactionId, identificacion, nombre);
            acknowledgment.acknowledge();
            
        } catch (Exception e) {
            log.error("[PERSONA] Error al procesar persona para transactionId: {}", transactionId, e);
            
            // NO reintentar - fallar la transacción inmediatamente para evitar bucles
            enviarEventoRollback(transactionId, "PERSONA", e.getMessage());
            acknowledgment.acknowledge();
        }
    }

    /**
     * Procesa eventos de creación de cliente con manejo transaccional
     */
    @KafkaListener(topics = KafkaTopicConfig.ONBOARDING_CLIENTE_TOPIC, groupId = "personas-clientes-group")
    @Transactional
    public void procesarCreacionCliente(@Payload OnboardingEventDTO event,
                                      @Header(KafkaHeaders.RECEIVED_KEY) String transactionId,
                                      Acknowledgment acknowledgment) {
        
        log.info("[CLIENTE] Procesando creación de cliente para transactionId: {}", transactionId);
        
        try {
            // Validar que el evento sea del tipo correcto
            if (!"CLIENTE".equals(event.getEventType())) {
                log.warn("[CLIENTE] Evento recibido no es de tipo CLIENTE: {} para transactionId: {}", 
                         event.getEventType(), transactionId);
                acknowledgment.acknowledge();
                return;
            }
            
            String identificacionPersona = getPersonaIdentificacion(event);
            if (identificacionPersona == null || identificacionPersona.trim().isEmpty()) {
                log.error("[CLIENTE] Evento sin identificación de persona para transactionId: {}", transactionId);
                enviarEventoRollback(transactionId, "CLIENTE", "Identificación de persona requerida");
                acknowledgment.acknowledge();
                return;
            }
            
            // Verificar si el cliente ya existe (idempotencia REAL)
            try {
                Cliente clienteExistente = clienteService.findByIdentificacionPersona(identificacionPersona);
                log.info("[CLIENTE] Cliente ya existe con ID: {} para transactionId: {}", 
                        clienteExistente.getIdcliente(), transactionId);
                
                // Solo continuar al siguiente paso, no crear duplicado
                enviarEventoCuenta(transactionId, clienteExistente.getIdcliente());
                acknowledgment.acknowledge();
                return;
                
            } catch (Exception e) {
                // Cliente no existe, continuar con creación
                log.info("[CLIENTE] Cliente no existe, creando nuevo para transactionId: {}", transactionId);
            }
            
            // Buscar la persona para establecer la relación
            Persona persona;
            try {
                persona = personaService.findByIdentificacion(identificacionPersona);
                log.info("[CLIENTE] Persona encontrada con ID: {} para transactionId: {}", 
                        persona.getIdpersona(), transactionId);
            } catch (Exception e) {
                log.error("[CLIENTE] Persona no encontrada con identificación: {} para transactionId: {}", 
                         identificacionPersona, transactionId);
                enviarEventoRollback(transactionId, "CLIENTE", "Persona no encontrada: " + identificacionPersona);
                acknowledgment.acknowledge();
                return;
            }
            
            // Crear nuevo cliente
            log.info("[CLIENTE] Creando nuevo cliente para transactionId: {}", transactionId);
            Cliente nuevoCliente = crearNuevoCliente(event, persona);
            Cliente clienteCreado = clienteService.save(nuevoCliente);
            
            log.info("[CLIENTE] Cliente creado exitosamente con ID: {} para transactionId: {}", 
                    clienteCreado.getIdcliente(), transactionId);
            
            // Enviar evento para crear cuenta
            enviarEventoCuenta(transactionId, clienteCreado.getIdcliente());
            acknowledgment.acknowledge();
            
        } catch (Exception e) {
            log.error("[CLIENTE] Error al procesar cliente para transactionId: {}", transactionId, e);
            
            // NO reintentar - fallar la transacción inmediatamente para evitar bucles
            enviarEventoRollback(transactionId, "CLIENTE", e.getMessage());
            acknowledgment.acknowledge();
        }
    }

    /**
     * Procesa eventos de rollback para compensar transacciones fallidas
     */
    @KafkaListener(topics = KafkaTopicConfig.ONBOARDING_ROLLBACK_TOPIC, groupId = "personas-clientes-rollback-group")
    @Transactional
    public void procesarRollback(@Payload OnboardingEventDTO event,
                               @Header(KafkaHeaders.RECEIVED_KEY) String transactionId,
                               Acknowledgment acknowledgment) {
        
        log.info("[ROLLBACK] Procesando rollback para transactionId: {}, paso fallido: {}", 
                 transactionId, event.getFailedStep());
        
        try {
            // Solo procesar rollbacks relacionados con personas y clientes
            if ("PERSONA".equals(event.getFailedStep()) || "CLIENTE".equals(event.getFailedStep())) {
                
                if (event.getPersonaIdentificacion() != null) {
                    try {
                        // Deshabilitar cliente si existe
                        Cliente cliente = clienteService.findByIdentificacionPersona(event.getPersonaIdentificacion());
                        if (cliente != null) {
                            cliente.setEstado(false);
                            clienteService.save(cliente);
                            log.info("[ROLLBACK] Cliente {} deshabilitado para transactionId: {}", 
                                     cliente.getIdcliente(), transactionId);
                        }
                        
                        // Deshabilitar persona si existe
                        Persona persona = personaService.findByIdentificacion(event.getPersonaIdentificacion());
                        if (persona != null) {
                            persona.setEstado(false);
                            personaService.save(persona);
                            log.info("[ROLLBACK] Persona {} deshabilitada para transactionId: {}", 
                                     persona.getIdpersona(), transactionId);
                        }
                    } catch (Exception e) {
                        log.warn("[ROLLBACK] No se pudieron deshabilitar entidades para transactionId: {} - Error: {}", 
                                 transactionId, e.getMessage());
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
    
    // Métodos auxiliares para simplificar el código
    
    private void enviarEventoCliente(String transactionId, String identificacion, String nombre) {
        OnboardingEventDTO clienteEvent = OnboardingEventDTO.createClienteEvent(
            transactionId,
            identificacion,
            generateClientePassword(nombre),
            true
        );
        
        enviarMensajeKafka(KafkaTopicConfig.ONBOARDING_CLIENTE_TOPIC, transactionId, clienteEvent);
        log.info("[PERSONA] Evento de cliente enviado para transactionId: {}", transactionId);
    }
    
    private void enviarEventoCuenta(String transactionId, Long clienteId) {
        OnboardingEventDTO cuentaEvent = OnboardingEventDTO.createCuentaEvent(
            transactionId,
            clienteId,
            generateNumeroCuenta(),
            "AHORROS",
            java.math.BigDecimal.valueOf(100.00) // Saldo inicial por defecto
        );
        
        enviarMensajeKafka(KafkaTopicConfig.ONBOARDING_CUENTA_TOPIC, transactionId, cuentaEvent);
        log.info("[CLIENTE] Evento de cuenta enviado para transactionId: {}", transactionId);
    }
    
    /**
     * Extrae la identificación de persona del evento, maneja ambos formatos
     */
    private String getPersonaIdentificacion(OnboardingEventDTO event) {
        if (event.getPersonaIdentificacion() != null && !event.getPersonaIdentificacion().trim().isEmpty()) {
            return event.getPersonaIdentificacion();
        }
        if (event.getIdentificacionpersona() != null && !event.getIdentificacionpersona().trim().isEmpty()) {
            return event.getIdentificacionpersona();
        }
        return null;
    }
    
    /**
     * Extrae el nombre de persona del evento, maneja ambos formatos
     */
    private String getPersonaNombre(OnboardingEventDTO event) {
        if (event.getPersonaNombre() != null && !event.getPersonaNombre().trim().isEmpty()) {
            return event.getPersonaNombre();
        }
        if (event.getNombres() != null && !event.getNombres().trim().isEmpty()) {
            return event.getNombres();
        }
        return null;
    }
    
    /**
     * Convierte el evento recibido al formato OnboardingEventDTO esperado
     */
    private OnboardingEventDTO convertirEvento(Object rawEvent) {
        if (rawEvent instanceof OnboardingEventDTO) {
            return (OnboardingEventDTO) rawEvent;
        }
        
        // Si el evento viene en formato de Map (JSON deserializado)
        if (rawEvent instanceof java.util.Map) {
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> eventMap = (java.util.Map<String, Object>) rawEvent;
            
            OnboardingEventDTO event = new OnboardingEventDTO();
            event.setTransactionId((String) eventMap.get("transactionId"));
            
            // Mapear campos de persona desde el formato del onboarding service
            event.setPersonaIdentificacion((String) eventMap.get("identificacionpersona"));
            event.setPersonaNombre((String) eventMap.get("nombres"));
            event.setPersonaGenero((String) eventMap.get("genero"));
            event.setPersonaEdad((Integer) eventMap.get("edad"));
            event.setPersonaDireccion((String) eventMap.get("direccion"));
            event.setPersonaTelefono((String) eventMap.get("telefono"));
            
            // También mantener compatibilidad con el formato original por si acaso
            if (event.getPersonaIdentificacion() == null) {
                event.setPersonaIdentificacion((String) eventMap.get("personaIdentificacion"));
            }
            if (event.getPersonaNombre() == null) {
                event.setPersonaNombre((String) eventMap.get("personaNombre"));
            }
            
            // Campos adicionales para compatibilidad
            event.setIdentificacionpersona((String) eventMap.get("identificacionpersona"));
            event.setNombres((String) eventMap.get("nombres"));
            
            return event;
        }
        
        // Si no se puede convertir, lanzar excepción
        throw new IllegalArgumentException("No se puede convertir el evento recibido: " + rawEvent.getClass());
    }
    
    private Persona crearNuevaPersonaFromEvent(OnboardingEventDTO event) {
        Persona nuevaPersona = new Persona();
        
        String identificacion = getPersonaIdentificacion(event);
        String nombre = getPersonaNombre(event);
        String genero = event.getPersonaGenero() != null ? event.getPersonaGenero() : event.getGenero();
        Integer edad = event.getPersonaEdad() != null ? event.getPersonaEdad() : event.getEdad();
        String direccion = event.getPersonaDireccion() != null ? event.getPersonaDireccion() : event.getDireccion();
        String telefono = event.getPersonaTelefono() != null ? event.getPersonaTelefono() : event.getTelefono();
        
        nuevaPersona.setIdentificacionpersona(identificacion);
        nuevaPersona.setNombres(nombre);
        nuevaPersona.setGenero(genero);
        nuevaPersona.setEdad(edad);
        nuevaPersona.setDireccion(direccion);
        nuevaPersona.setTelefono(telefono);
        nuevaPersona.setEstado(true);
        
        return nuevaPersona;
    }
    
    private Persona crearNuevaPersona(OnboardingEventDTO event) {
        Persona nuevaPersona = new Persona();
        nuevaPersona.setIdentificacionpersona(event.getPersonaIdentificacion());
        nuevaPersona.setNombres(event.getPersonaNombre());
        nuevaPersona.setGenero(event.getPersonaGenero());
        nuevaPersona.setEdad(event.getPersonaEdad());
        nuevaPersona.setDireccion(event.getPersonaDireccion());
        nuevaPersona.setTelefono(event.getPersonaTelefono());
        nuevaPersona.setEstado(true);
        
        return nuevaPersona;
    }
    
    private Cliente crearNuevoCliente(OnboardingEventDTO event, Persona persona) {
        Cliente nuevoCliente = new Cliente();
        nuevoCliente.setPersona(persona);
        nuevoCliente.setNombreusuario(generateClienteUsuario(persona.getIdentificacionpersona()));
        nuevoCliente.setContrasena(event.getClientePassword());
        nuevoCliente.setEstado(true);
        
        return nuevoCliente;
    }
    
    private String generateClientePassword(String nombrePersona) {
        // Generar password basado en el nombre + timestamp
        return nombrePersona.toLowerCase().replaceAll("\\s+", "") + System.currentTimeMillis() % 1000;
    }
    
    private String generateClienteUsuario(String identificacion) {
        // Generar usuario basado en la identificación
        return "user_" + identificacion;
    }
    
    private String generateNumeroCuenta() {
        // Generar número de cuenta único (simplificado)
        return String.valueOf(System.currentTimeMillis() % 1000000000);
    }
    
    private void enviarEventoRollback(String transactionId, String failedStep, String errorMessage) {
        OnboardingEventDTO rollbackEvent = OnboardingEventDTO.createRollbackEvent(
            transactionId, failedStep, errorMessage, "DISABLE"
        );
        enviarMensajeKafka(KafkaTopicConfig.ONBOARDING_ROLLBACK_TOPIC, transactionId, rollbackEvent);
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
