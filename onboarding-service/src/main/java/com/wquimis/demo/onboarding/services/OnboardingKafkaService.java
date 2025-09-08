package com.wquimis.demo.onboarding.services;

import com.wquimis.demo.onboarding.dto.kafka.PersonaEventDTO;
import com.wquimis.demo.onboarding.dto.kafka.ClienteEventDTO;
import com.wquimis.demo.onboarding.dto.kafka.CuentaEventDTO;
import com.wquimis.demo.onboarding.dto.OnboardingRequestDTO;
import com.wquimis.demo.onboarding.dto.OnboardingResponseDTO;
import com.wquimis.demo.onboarding.config.KafkaTopicConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
@RequiredArgsConstructor
public class OnboardingKafkaService {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final Random random = new Random();

    @Transactional("kafkaTransactionManager")
    public String iniciarOnboarding(OnboardingRequestDTO request) {
        String transactionId = UUID.randomUUID().toString();
        log.info("Iniciando proceso de onboarding con transactionId: {}", transactionId);

        try {
            // Crear evento de persona con todos los datos necesarios para el flujo completo
            PersonaEventDTO personaEvent = new PersonaEventDTO();
            personaEvent.setTransactionId(transactionId);
            personaEvent.setIdentificacionpersona(request.getPersona().getIdentificacionpersona());
            personaEvent.setNombres(request.getPersona().getNombres());
            personaEvent.setGenero(request.getPersona().getGenero());
            personaEvent.setEdad(request.getPersona().getEdad());
            personaEvent.setDireccion(request.getPersona().getDireccion());
            personaEvent.setTelefono(request.getPersona().getTelefono());
            personaEvent.setTimestamp(LocalDateTime.now());

            // Almacenar datos del cliente para uso posterior
            personaEvent.setClienteNombreUsuario(request.getCliente().getNombreUsuario());
            personaEvent.setClienteContrasena(request.getCliente().getContrasena());
            
            // Almacenar datos de la cuenta para uso posterior
            personaEvent.setCuentaTipoCuenta(request.getCuenta().getTipoCuenta());
            personaEvent.setCuentaSaldoInicial(request.getCuenta().getSaldoInicial());
            personaEvent.setCuentaNumeroCuenta(generarNumeroCuentaOnboarding());

            // Enviar mensaje para crear persona
            CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(
                KafkaTopicConfig.PERSONA_TOPIC, 
                transactionId, 
                personaEvent
            );

            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Mensaje de persona enviado exitosamente para transactionId: {} con offset: {}", 
                             transactionId, result.getRecordMetadata().offset());
                } else {
                    log.error("Error al enviar mensaje de persona para transactionId: {}", transactionId, ex);
                }
            });

            return transactionId;

        } catch (Exception e) {
            log.error("Error al iniciar onboarding para transactionId: {}", transactionId, e);
            throw new RuntimeException("Error al iniciar proceso de onboarding", e);
        }
    }

    /**
     * Genera un número de cuenta para onboarding con prefijo 99
     * y máximo 6 dígitos (99XXXX)
     */
    private Integer generarNumeroCuentaOnboarding() {
        // Generar número aleatorio de 4 dígitos (1000-9999)
        int sufijo = random.nextInt(9000) + 1000;
        // Concatenar con prefijo 99
        return Integer.parseInt("99" + sufijo);
    }
}
