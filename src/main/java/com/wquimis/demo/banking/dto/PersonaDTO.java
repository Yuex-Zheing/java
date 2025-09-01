package com.wquimis.demo.banking.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class PersonaDTO {
    private Long id;
    @NotBlank(message = "La identificación es requerida")
    @Size(max = 10, message = "La identificación debe tener máximo 10 caracteres")
    private String identificacion;

    @NotBlank(message = "El nombre es requerido")
    @Size(max = 150, message = "El nombre debe tener máximo 150 caracteres")
    private String nombres;

    @NotBlank(message = "El género es requerido")
    @Pattern(regexp = "^[MF]$", message = "El género debe ser M o F")
    private String genero;

    @NotNull(message = "La edad es requerida")
    @Min(value = 0, message = "La edad debe ser mayor o igual a 0")
    @Max(value = 150, message = "La edad debe ser menor a 150")
    private Integer edad;

    @Size(max = 300, message = "La dirección debe tener máximo 300 caracteres")
    private String direccion;

    @Pattern(regexp = "^[0-9]+$", message = "El teléfono debe contener solo números")
    @Size(max = 15, message = "El teléfono debe tener máximo 15 dígitos")
    private String telefono;
}
