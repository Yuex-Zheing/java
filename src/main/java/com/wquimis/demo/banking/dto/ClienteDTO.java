package com.wquimis.demo.banking.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ClienteDTO {
    @NotBlank(message = "La identificación de la persona es requerida")
    @Size(max = 10, message = "La identificación debe tener máximo 10 caracteres")
    private String identificacionPersona;

    @NotBlank(message = "El nombre de usuario es requerido")
    @Size(min = 4, max = 50, message = "El nombre de usuario debe tener entre 4 y 50 caracteres")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "El nombre de usuario solo puede contener letras, números, puntos, guiones y guiones bajos")
    private String nombreUsuario;

    @NotBlank(message = "La contraseña es requerida")
    @Size(min = 6, max = 100, message = "La contraseña debe tener entre 6 y 100 caracteres")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{6,}$",
            message = "La contraseña debe contener al menos un número, una letra mayúscula, una minúscula y un carácter especial")
    private String contrasena;
}
