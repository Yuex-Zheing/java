package com.wquimis.demo.onboarding.services;

import com.wquimis.demo.onboarding.config.KafkaTopicConfig;
import com.wquimis.demo.onboarding.dto.OnboardingRequestDTO;
import com.wquimis.demo.onboarding.dto.OnboardingResponseDTO;
import com.wquimis.demo.onboarding.dto.kafka.OnboardingEventDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

/**
 * Servicio principal de onboarding que inicia el flujo transaccional
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OnboardingOrchestratorService {

    private final KafkaTemplate<String, Object> transactionalKafkaTemplate;
    private final Random random = new Random();

    /**
     * Inicia el flujo de onboarding enviando el evento inicial
     */
    @Transactional("kafkaTransactionManager")
    public OnboardingResponseDTO iniciarOnboarding(OnboardingRequestDTO request) {
        String transactionId = UUID.randomUUID().toString();
        
        try {
            log.info("Iniciando flujo de onboarding con transactionId: {}", transactionId);
            
            // Crear evento de onboarding con todos los datos
            OnboardingEventDTO event = crearEventoInicial(transactionId, request);
            
            // Enviar evento al primer paso del flujo
            transactionalKafkaTemplate.send(
                KafkaTopicConfig.ONBOARDING_PERSONA_TOPIC, 
                transactionId, 
                event
            );
            
            log.info("Evento de onboarding enviado exitosamente para transactionId: {}", transactionId);
            
            return OnboardingResponseDTO.builder()
                .transactionId(transactionId)
                .mensaje("Proceso de onboarding iniciado exitosamente")
                .estado("INICIADO")
                .timestamp(LocalDateTime.now())
                .build();
                
        } catch (Exception e) {
            log.error("Error al iniciar onboarding para transactionId: {}", transactionId, e);
            throw new RuntimeException("Error al iniciar proceso de onboarding: " + e.getMessage(), e);
        }
    }

    /**
     * Crea el evento inicial de onboarding con todos los datos necesarios
     */
    private OnboardingEventDTO crearEventoInicial(String transactionId, OnboardingRequestDTO request) {
        OnboardingEventDTO event = new OnboardingEventDTO();
        
        // Metadatos del flujo
        event.setTransactionId(transactionId);
        event.setCurrentStep("PERSONA");
        event.setTimestamp(LocalDateTime.now());
        event.setRetryCount(0);
        
        // Datos de la persona
        event.setIdentificacionpersona(request.getPersona().getIdentificacionpersona());
        event.setNombres(request.getPersona().getNombres());
        event.setGenero(request.getPersona().getGenero());
        event.setEdad(request.getPersona().getEdad());
        event.setDireccion(request.getPersona().getDireccion());
        event.setTelefono(request.getPersona().getTelefono());
        
        // Datos del cliente
        event.setNombreUsuario(request.getCliente().getNombreUsuario());
        event.setContrasena(request.getCliente().getContrasena());
        
        // Datos de la cuenta
        event.setTipoCuenta(request.getCuenta().getTipoCuenta());
        event.setSaldoInicial(request.getCuenta().getSaldoInicial());
        event.setNumeroCuenta(generarNumeroCuentaOnboarding());
        
        return event;
    }

    /**
     * Genera un número de cuenta único para onboarding (prefijo 99)
     */
    private Integer generarNumeroCuentaOnboarding() {
        // Generar número aleatorio de 4 dígitos (1000-9999)
        int sufijo = random.nextInt(9000) + 1000;
        // Concatenar con prefijo 99
        return Integer.parseInt("99" + sufijo);
    }
}
