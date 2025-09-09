package com.wquimis.demo.personasclientes.config;

/**
 * Configuración centralizada de topics de Kafka
 */
public class KafkaTopicConfig {
    
    // Topics para el flujo de onboarding
    public static final String ONBOARDING_PERSONA_TOPIC = "onboarding.persona";
    public static final String ONBOARDING_CLIENTE_TOPIC = "onboarding.cliente";
    public static final String ONBOARDING_CUENTA_TOPIC = "onboarding.cuenta";
    public static final String ONBOARDING_MOVIMIENTO_TOPIC = "onboarding.movimiento";
    public static final String ONBOARDING_ROLLBACK_TOPIC = "onboarding.rollback";
    public static final String ONBOARDING_COMPLETED_TOPIC = "onboarding.completed";
}
