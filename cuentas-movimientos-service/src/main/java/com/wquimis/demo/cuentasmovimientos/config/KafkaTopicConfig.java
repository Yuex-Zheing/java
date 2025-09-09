package com.wquimis.demo.cuentasmovimientos.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {
    
    // Topicos para flujo de onboarding
    public static final String ONBOARDING_PERSONA = "onboarding.persona";
    public static final String ONBOARDING_CLIENTE = "onboarding.cliente"; 
    public static final String ONBOARDING_CUENTA = "onboarding.cuenta";
    public static final String ONBOARDING_MOVIMIENTO = "onboarding.movimiento";
    public static final String ONBOARDING_COMPLETED = "onboarding.completed";
    public static final String ONBOARDING_ROLLBACK = "onboarding.rollback";
    
    // Crear los topics automáticamente
    @Bean
    public NewTopic onboardingPersonaTopic() {
        return TopicBuilder.name(ONBOARDING_PERSONA)
                .partitions(3)
                .replicas(1)
                .build();
    }
    
    @Bean
    public NewTopic onboardingClienteTopic() {
        return TopicBuilder.name(ONBOARDING_CLIENTE)
                .partitions(3)
                .replicas(1)
                .build();
    }
    
    @Bean
    public NewTopic onboardingCuentaTopic() {
        return TopicBuilder.name(ONBOARDING_CUENTA)
                .partitions(3)
                .replicas(1)
                .build();
    }
    
    @Bean
    public NewTopic onboardingMovimientoTopic() {
        return TopicBuilder.name(ONBOARDING_MOVIMIENTO)
                .partitions(3)
                .replicas(1)
                .build();
    }
    
    @Bean
    public NewTopic onboardingCompletedTopic() {
        return TopicBuilder.name(ONBOARDING_COMPLETED)
                .partitions(3)
                .replicas(1)
                .build();
    }
    
    @Bean
    public NewTopic onboardingRollbackTopic() {
        return TopicBuilder.name(ONBOARDING_ROLLBACK)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
