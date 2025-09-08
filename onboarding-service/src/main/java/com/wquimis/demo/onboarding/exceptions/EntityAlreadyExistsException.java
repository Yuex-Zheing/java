package com.wquimis.demo.onboarding.exceptions;

public class EntityAlreadyExistsException extends OnboardingException {
    
    private final String entityType;
    private final String identifier;
    
    public EntityAlreadyExistsException(String entityType, String identifier) {
        super(String.format("La entidad %s con identificador '%s' ya existe", entityType, identifier));
        this.entityType = entityType;
        this.identifier = identifier;
    }
    
    public EntityAlreadyExistsException(String entityType, String identifier, String customMessage) {
        super(customMessage);
        this.entityType = entityType;
        this.identifier = identifier;
    }
    
    public String getEntityType() {
        return entityType;
    }
    
    public String getIdentifier() {
        return identifier;
    }
}
