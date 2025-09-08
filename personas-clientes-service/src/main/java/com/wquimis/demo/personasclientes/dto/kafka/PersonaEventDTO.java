package com.wquimis.demo.personasclientes.dto.kafka;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PersonaEventDTO {
    private String transactionId;
    private String identificacionpersona;
    private String nombres;
    private String genero;
    private Integer edad;
    private String direccion;
    private String telefono;
    private LocalDateTime timestamp;
    private Long personaId; // Se llena después de crear la persona
    
    // Datos del cliente para el siguiente paso
    private String clienteNombreUsuario;
    private String clienteContrasena;
    
    // Datos de la cuenta para pasos posteriores
    private String cuentaTipoCuenta;
    private BigDecimal cuentaSaldoInicial;
    private Integer cuentaNumeroCuenta;
}
