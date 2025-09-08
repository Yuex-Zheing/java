package com.wquimis.demo.personasclientes.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CreateClienteDTO {
    @NotNull(message = "El ID de la persona es requerido")
    private Long personaId;

    @NotBlank(message = "El nombre de usuario es requerido")
    @Size(min = 4, max = 50, message = "El nombre de usuario debe tener entre 4 y 50 caracteres")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "El nombre de usuario solo puede contener letras, números, puntos, guiones y guiones bajos")
    private String nombreUsuario;

    @NotBlank(message = "La contraseña es requerida")
    @Size(min = 6, max = 100, message = "La contraseña debe tener entre 6 y 100 caracteres")
    private String contrasena;
}
