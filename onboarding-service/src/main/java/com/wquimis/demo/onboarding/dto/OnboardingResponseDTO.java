package com.wquimis.demo.onboarding.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OnboardingResponseDTO {
    
    private String transactionId;
    private String estado;
    private String mensaje;
    private LocalDateTime timestamp;
    
    // Datos de respuesta cuando se completa
    private Long personaId;
    private String personaNombres;
    private String personaIdentificacion;
    
    private Long clienteId;
    private String clienteNombreUsuario;
    
    private Integer numeroCuenta;
    private String tipoCuenta;
    private String saldoDisponible;
    private LocalDateTime fechaCreacion;
}
