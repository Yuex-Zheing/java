package com.wquimis.demo.onboarding.config;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaTopicConfig {
    
    // Topics para el flujo de onboarding
    public static final String ONBOARDING_PERSONA_TOPIC = "onboarding.persona";
    public static final String ONBOARDING_CLIENTE_TOPIC = "onboarding.cliente";
    public static final String ONBOARDING_CUENTA_TOPIC = "onboarding.cuenta";
    public static final String ONBOARDING_MOVIMIENTO_TOPIC = "onboarding.movimiento";
    public static final String ONBOARDING_ROLLBACK_TOPIC = "onboarding.rollback";
    public static final String ONBOARDING_COMPLETED_TOPIC = "onboarding.completed";

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        return new KafkaAdmin(configs);
    }

    @Bean
    public NewTopic personaTopic() {
        return new NewTopic(ONBOARDING_PERSONA_TOPIC, 3, (short) 1);
    }

    @Bean
    public NewTopic clienteTopic() {
        return new NewTopic(ONBOARDING_CLIENTE_TOPIC, 3, (short) 1);
    }

    @Bean
    public NewTopic cuentaTopic() {
        return new NewTopic(ONBOARDING_CUENTA_TOPIC, 3, (short) 1);
    }

    @Bean
    public NewTopic movimientoTopic() {
        return new NewTopic(ONBOARDING_MOVIMIENTO_TOPIC, 3, (short) 1);
    }

    @Bean
    public NewTopic rollbackTopic() {
        return new NewTopic(ONBOARDING_ROLLBACK_TOPIC, 3, (short) 1);
    }

    @Bean
    public NewTopic completedTopic() {
        return new NewTopic(ONBOARDING_COMPLETED_TOPIC, 3, (short) 1);
    }
}
