package com.wquimis.demo.onboarding.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class PersonaRequestDTO {
    
    @NotBlank(message = "La identificación de la persona es requerida")
    @Size(min = 10, max = 13, message = "La identificación debe tener entre 10 y 13 caracteres")
    private String identificacionpersona;
    
    @NotBlank(message = "Los nombres son requeridos")
    @Size(max = 100, message = "Los nombres no pueden exceder 100 caracteres")
    private String nombres;
    
    @NotBlank(message = "El género es requerido")
    @Pattern(regexp = "^(M|F)$", message = "El género debe ser M (Masculino) o F (Femenino)")
    private String genero;
    
    @NotNull(message = "La edad es requerida")
    @Min(value = 18, message = "La edad mínima es 18 años")
    @Max(value = 120, message = "La edad máxima es 120 años")
    private Integer edad;
    
    @NotBlank(message = "La dirección es requerida")
    @Size(max = 200, message = "La dirección no puede exceder 200 caracteres")
    private String direccion;
    
    @NotBlank(message = "El teléfono es requerido")
    @Pattern(regexp = "^[0-9]{10}$", message = "El teléfono debe tener 10 dígitos")
    private String telefono;
}
