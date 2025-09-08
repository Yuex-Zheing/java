package com.wquimis.demo.onboarding.dto;

import lombok.Data;

// DTO de respuesta al crear un cliente - coincide con ClienteDTO del servicio de personas-clientes
@Data
public class ClienteResponseDTO {
    private Long id;
    private String nombreUsuario;
    private Boolean estado;
    // Información de la persona asociada podría estar incluida
}
