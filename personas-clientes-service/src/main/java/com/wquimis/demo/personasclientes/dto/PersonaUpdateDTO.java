package com.wquimis.demo.personasclientes.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PersonaUpdateDTO {
    @NotBlank(message = "El nombre es requerido")
    private String nombres;

    @NotBlank(message = "El género es requerido")
    @Size(min = 1, max = 1, message = "El género debe ser un solo carácter (M o F)")
    private String genero;

    @Min(value = 0, message = "La edad debe ser un número positivo")
    private Integer edad;

    @NotBlank(message = "La dirección es requerida")
    private String direccion;

    @NotBlank(message = "El teléfono es requerido")
    private String telefono;

    private Boolean estado;
}
