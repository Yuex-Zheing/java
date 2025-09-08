package com.wquimis.demo.onboarding.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OnboardingRequestDTO {
    
    @Valid
    @NotNull(message = "Los datos de la persona son requeridos")
    private PersonaRequestDTO persona;
    
    @Valid
    @NotNull(message = "Los datos del cliente son requeridos")
    private ClienteRequestDTO cliente;
    
    @Valid
    @NotNull(message = "Los datos de la cuenta son requeridos")
    private CuentaRequestDTO cuenta;
}
