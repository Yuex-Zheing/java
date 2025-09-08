package com.wquimis.demo.cuentasmovimientos.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaTopicConfig {
    
    public static final String PERSONA_TOPIC = "onboarding-persona";
    public static final String CLIENTE_TOPIC = "onboarding-cliente";
    public static final String CUENTA_TOPIC = "onboarding-cuenta";
    public static final String MOVIMIENTO_TOPIC = "onboarding-movimiento";
    public static final String ROLLBACK_TOPIC = "onboarding-rollback";
    public static final String COMPLETED_TOPIC = "onboarding-completed";
}
