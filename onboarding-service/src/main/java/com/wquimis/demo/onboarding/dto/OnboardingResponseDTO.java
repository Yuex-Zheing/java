package com.wquimis.demo.onboarding.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class OnboardingResponseDTO {
    
    private Long personaId;
    private String personaNombres;
    private String personaIdentificacion;
    
    private Long clienteId;
    private String clienteNombreUsuario;
    
    private Integer numeroCuenta;
    private String tipoCuenta;
    private String saldoDisponible;
    
    private String mensaje;
    private LocalDateTime fechaCreacion;
    
    public OnboardingResponseDTO() {
        this.fechaCreacion = LocalDateTime.now();
    }
}
