package com.wquimis.demo.personasclientes.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class OnboardingRequestDTO {
    private PersonaOnboardingDTO persona;
    private ClienteOnboardingDTO cliente;
    private CuentaOnboardingDTO cuenta;

    @Data
    public static class PersonaOnboardingDTO {
        private String identificacionpersona;
        private String nombres;
        private String genero;
        private Integer edad;
        private String direccion;
        private String telefono;
    }

    @Data
    public static class ClienteOnboardingDTO {
        private String nombreUsuario;
        private String contrasena;
    }

    @Data
    public static class CuentaOnboardingDTO {
        private String tipoCuenta; // Cambio a String para evitar dependencia de entidad
        private BigDecimal saldoInicial;
    }
}
