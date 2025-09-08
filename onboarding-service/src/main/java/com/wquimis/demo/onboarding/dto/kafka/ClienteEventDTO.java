package com.wquimis.demo.onboarding.dto.kafka;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClienteEventDTO {
    private String transactionId;
    private Long personaId;
    private String nombreUsuario;
    private String contrasena;
    private LocalDateTime timestamp;
    private Long clienteId; // Se llena después de crear el cliente
}
