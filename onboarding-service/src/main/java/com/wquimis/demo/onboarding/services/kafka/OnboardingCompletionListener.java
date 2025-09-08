package com.wquimis.demo.onboarding.services.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wquimis.demo.onboarding.config.KafkaTopicConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class OnboardingCompletionListener {

    private final ObjectMapper objectMapper;

    @KafkaListener(topics = KafkaTopicConfig.COMPLETED_TOPIC)
    public void procesarCompletado(@Payload Object payload,
                                  @Header(KafkaHeaders.RECEIVED_KEY) String key,
                                  Acknowledgment acknowledgment) {
        try {
            log.info("Onboarding completado para transactionId: {}", key);
            
            CompletedEventDTO completedEvent = objectMapper.convertValue(payload, CompletedEventDTO.class);
            
            // Aquí podrías actualizar una base de datos de transacciones,
            // enviar notificaciones, etc.
            log.info("Proceso de onboarding exitoso - transactionId: {}, status: {}", 
                     completedEvent.getTransactionId(), completedEvent.getStatus());
            
            acknowledgment.acknowledge();
            
        } catch (Exception e) {
            log.error("Error al procesar evento de completado para transactionId: {}", key, e);
            acknowledgment.acknowledge();
        }
    }

    @KafkaListener(topics = KafkaTopicConfig.ROLLBACK_TOPIC)
    public void procesarRollbackFinal(@Payload Object payload,
                                     @Header(KafkaHeaders.RECEIVED_KEY) String key,
                                     Acknowledgment acknowledgment) {
        try {
            log.error("Rollback requerido para transactionId: {}", key);
            
            RollbackEventDTO rollbackEvent = objectMapper.convertValue(payload, RollbackEventDTO.class);
            
            log.error("Onboarding falló - transactionId: {}, paso fallido: {}, error: {}", 
                     rollbackEvent.getTransactionId(), 
                     rollbackEvent.getFailedStep(),
                     rollbackEvent.getErrorMessage());
            
            acknowledgment.acknowledge();
            
        } catch (Exception e) {
            log.error("Error al procesar evento de rollback para transactionId: {}", key, e);
            acknowledgment.acknowledge();
        }
    }

    // DTOs para eventos
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
