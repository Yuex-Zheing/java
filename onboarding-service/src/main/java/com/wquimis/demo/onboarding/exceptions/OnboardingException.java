package com.wquimis.demo.onboarding.exceptions;

public class OnboardingException extends RuntimeException {
    
    public OnboardingException(String message) {
        super(message);
    }
    
    public OnboardingException(String message, Throwable cause) {
        super(message, cause);
    }
}
