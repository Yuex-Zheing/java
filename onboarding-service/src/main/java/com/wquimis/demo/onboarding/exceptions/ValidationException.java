package com.wquimis.demo.onboarding.exceptions;

public class ValidationException extends OnboardingException {
    
    private final String field;
    private final String value;
    
    public ValidationException(String message) {
        super(message);
        this.field = null;
        this.value = null;
    }
    
    public ValidationException(String field, String value, String message) {
        super(String.format("Error de validación en campo '%s' con valor '%s': %s", field, value, message));
        this.field = field;
        this.value = value;
    }
    
    public ValidationException(String message, Throwable cause) {
        super(message, cause);
        this.field = null;
        this.value = null;
    }
    
    public String getField() {
        return field;
    }
    
    public String getValue() {
        return value;
    }
}
