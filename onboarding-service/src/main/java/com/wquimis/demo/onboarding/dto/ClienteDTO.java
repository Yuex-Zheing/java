package com.wquimis.demo.onboarding.dto;

import lombok.Data;

// DTO para crear clientes - debe coincidir con CreateClienteDTO del servicio de personas-clientes
@Data
public class ClienteDTO {
    private Long personaId;        // Campo requerido por CreateClienteDTO
    private String nombreUsuario;  // Campo requerido por CreateClienteDTO  
    private String contrasena;     // Campo requerido por CreateClienteDTO
}
