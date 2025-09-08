package com.wquimis.demo.onboarding.exceptions;

public class ExternalServiceException extends OnboardingException {
    
    public ExternalServiceException(String message) {
        super(message);
    }
    
    public ExternalServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
