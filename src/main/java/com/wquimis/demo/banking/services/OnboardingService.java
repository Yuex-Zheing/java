package com.wquimis.demo.banking.services;

import com.wquimis.demo.banking.dto.OnboardingRequestDTO;
import com.wquimis.demo.banking.entities.Cliente;
import com.wquimis.demo.banking.entities.Cuenta;
import com.wquimis.demo.banking.entities.Persona;
import com.wquimis.demo.banking.exceptions.OnboardingException;

public interface OnboardingService {
    Cuenta procesarOnboarding(OnboardingRequestDTO request) throws OnboardingException;
}
