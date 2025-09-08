package com.wquimis.demo.onboarding.dto;

import lombok.Data;

// DTO para comunicación con personas-clientes-service  
// Debe coincidir exactamente con PersonaDTO del servicio de personas-clientes
@Data
public class PersonaDTO {
    private Long id;
    private String identificacionpersona;
    private String nombres;
    private String genero;
    private Integer edad;
    private String direccion;
    private String telefono;
    private Boolean estado;
}
