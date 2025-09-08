package com.wquimis.demo.personasclientes.services.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wquimis.demo.personasclientes.config.KafkaTopicConfig;
import com.wquimis.demo.personasclientes.dto.kafka.ClienteEventDTO;
import com.wquimis.demo.personasclientes.dto.kafka.PersonaEventDTO;
import com.wquimis.demo.personasclientes.entities.Persona;
import com.wquimis.demo.personasclientes.entities.Cliente;
import com.wquimis.demo.personasclientes.services.PersonaService;
import com.wquimis.demo.personasclientes.services.ClienteService;
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
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
@RequiredArgsConstructor
public class OnboardingEventListener {

    private final PersonaService personaService;
    private final ClienteService clienteService;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = KafkaTopicConfig.PERSONA_TOPIC)
    @Transactional
    public void procesarPersona(@Payload Object payload, 
                               @Header(KafkaHeaders.RECEIVED_KEY) String key,
                               Acknowledgment acknowledgment) {
        try {
            log.info("Procesando creación de persona con transactionId: {}", key);
            
            // Convertir payload a PersonaEventDTO
            PersonaEventDTO personaEvent = objectMapper.convertValue(payload, PersonaEventDTO.class);
            
            // Verificar si la persona ya existe
            Persona personaExistente = null;
            try {
                personaExistente = personaService.findByIdentificacion(personaEvent.getIdentificacionpersona());
                log.info("Persona ya existe con ID: {} para transactionId: {}", 
                        personaExistente.getIdpersona(), key);
                personaEvent.setPersonaId(personaExistente.getIdpersona());
                
            } catch (Exception e) {
                // Persona no existe, crear nueva
                log.info("Creando nueva persona para transactionId: {}", key);
                
                Persona nuevaPersona = new Persona();
                nuevaPersona.setIdentificacionpersona(personaEvent.getIdentificacionpersona());
                nuevaPersona.setNombres(personaEvent.getNombres());
                nuevaPersona.setGenero(personaEvent.getGenero());
                nuevaPersona.setEdad(personaEvent.getEdad());
                nuevaPersona.setDireccion(personaEvent.getDireccion());
                nuevaPersona.setTelefono(personaEvent.getTelefono());
                nuevaPersona.setEstado(true);
                
                Persona personaCreada = personaService.save(nuevaPersona);
                personaEvent.setPersonaId(personaCreada.getIdpersona());
                log.info("Persona creada exitosamente con ID: {} para transactionId: {}", 
                        personaCreada.getIdpersona(), key);
            }
            
            // Crear evento de cliente para continuar el flujo
            ClienteEventDTO clienteEvent = new ClienteEventDTO();
            clienteEvent.setTransactionId(personaEvent.getTransactionId());
            clienteEvent.setPersonaId(personaEvent.getPersonaId());
            clienteEvent.setNombreUsuario(personaEvent.getClienteNombreUsuario());
            clienteEvent.setContrasena(personaEvent.getClienteContrasena());
            clienteEvent.setTimestamp(LocalDateTime.now());
            
            // Pasar datos de cuenta para pasos posteriores
            clienteEvent.setCuentaTipoCuenta(personaEvent.getCuentaTipoCuenta());
            clienteEvent.setCuentaSaldoInicial(personaEvent.getCuentaSaldoInicial());
            clienteEvent.setCuentaNumeroCuenta(personaEvent.getCuentaNumeroCuenta());
            
            // Enviar mensaje para crear cliente
            CompletableFuture<org.springframework.kafka.support.SendResult<String, Object>> future = 
                kafkaTemplate.send(KafkaTopicConfig.CLIENTE_TOPIC, key, clienteEvent);
            
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Mensaje de cliente enviado exitosamente para transactionId: {}", key);
                    acknowledgment.acknowledge(); // Confirmar procesamiento exitoso
                } else {
                    log.error("Error al enviar mensaje de cliente para transactionId: {}", key, ex);
                    // No hacer acknowledge para que se reintente
                }
            });
            
        } catch (Exception e) {
            log.error("Error al procesar persona para transactionId: {}", key, e);
            // Enviar mensaje de rollback
            enviarMensajeRollback(key, "PERSONA", e.getMessage());
            acknowledgment.acknowledge(); // Confirmar para evitar reintento infinito
        }
    }

    @KafkaListener(topics = KafkaTopicConfig.CLIENTE_TOPIC)
    @Transactional
    public void procesarCliente(@Payload Object payload,
                               @Header(KafkaHeaders.RECEIVED_KEY) String key,
                               Acknowledgment acknowledgment) {
        try {
            log.info("Procesando creación de cliente con transactionId: {}", key);
            
            // Convertir payload a ClienteEventDTO
            ClienteEventDTO clienteEvent = objectMapper.convertValue(payload, ClienteEventDTO.class);
            
            // Verificar si el cliente ya existe para esta persona
            Cliente clienteExistente = null;
            try {
                clienteExistente = clienteService.findByIdentificacionPersona(
                    personaService.findById(clienteEvent.getPersonaId()).getIdentificacionpersona()
                );
                log.info("Cliente ya existe con ID: {} para transactionId: {}", 
                        clienteExistente.getIdcliente(), key);
                clienteEvent.setClienteId(clienteExistente.getIdcliente());
                
            } catch (Exception e) {
                // Cliente no existe, crear nuevo
                log.info("Creando nuevo cliente para transactionId: {}", key);
                
                Cliente nuevoCliente = new Cliente();
                Persona persona = personaService.findById(clienteEvent.getPersonaId());
                nuevoCliente.setPersona(persona);
                nuevoCliente.setNombreusuario(clienteEvent.getNombreUsuario());
                nuevoCliente.setContrasena(clienteEvent.getContrasena());
                nuevoCliente.setEstado(true);
                
                Cliente clienteCreado = clienteService.save(nuevoCliente);
                clienteEvent.setClienteId(clienteCreado.getIdcliente());
                log.info("Cliente creado exitosamente con ID: {} para transactionId: {}", 
                        clienteCreado.getIdcliente(), key);
            }
            
            // Crear evento de cuenta para continuar el flujo
            com.wquimis.demo.personasclientes.dto.kafka.CuentaEventDTO cuentaEvent = 
                new com.wquimis.demo.personasclientes.dto.kafka.CuentaEventDTO();
            cuentaEvent.setTransactionId(clienteEvent.getTransactionId());
            cuentaEvent.setClienteId(clienteEvent.getClienteId());
            cuentaEvent.setNumeroCuenta(clienteEvent.getCuentaNumeroCuenta());
            cuentaEvent.setTipoCuenta(clienteEvent.getCuentaTipoCuenta());
            cuentaEvent.setSaldoInicial(clienteEvent.getCuentaSaldoInicial());
            cuentaEvent.setTimestamp(LocalDateTime.now());
            
            // Enviar mensaje para crear cuenta
            CompletableFuture<org.springframework.kafka.support.SendResult<String, Object>> future = 
                kafkaTemplate.send(KafkaTopicConfig.CUENTA_TOPIC, key, cuentaEvent);
            
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Mensaje de cuenta enviado exitosamente para transactionId: {}", key);
                    acknowledgment.acknowledge(); // Confirmar procesamiento exitoso
                } else {
                    log.error("Error al enviar mensaje de cuenta para transactionId: {}", key, ex);
                    // No hacer acknowledge para que se reintente
                }
            });
            
        } catch (Exception e) {
            log.error("Error al procesar cliente para transactionId: {}", key, e);
            // Enviar mensaje de rollback
            enviarMensajeRollback(key, "CLIENTE", e.getMessage());
            acknowledgment.acknowledge(); // Confirmar para evitar reintento infinito
        }
    }

    @KafkaListener(topics = KafkaTopicConfig.ROLLBACK_TOPIC)
    public void procesarRollback(@Payload Object payload,
                                @Header(KafkaHeaders.RECEIVED_KEY) String key,
                                Acknowledgment acknowledgment) {
        try {
            log.info("Procesando rollback para transactionId: {}", key);
            
            // TODO: Implementar lógica de rollback para personas y clientes
            // Por ahora solo logeamos
            log.warn("Rollback requerido para transactionId: {} - {}", key, payload);
            
            acknowledgment.acknowledge();
            
        } catch (Exception e) {
            log.error("Error al procesar rollback para transactionId: {}", key, e);
            acknowledgment.acknowledge();
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

    // DTO para mensajes de rollback
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
}
