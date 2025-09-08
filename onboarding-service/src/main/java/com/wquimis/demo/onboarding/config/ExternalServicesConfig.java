package com.wquimis.demo.onboarding.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "external.services")
@Data
public class ExternalServicesConfig {
    
    private PersonasClientes personasClientes = new PersonasClientes();
    private CuentasMovimientos cuentasMovimientos = new CuentasMovimientos();
    
    @Data
    public static class PersonasClientes {
        private String baseUrl = "http://localhost:8081";
        
        public String getPersonasUrl() {
            return baseUrl + "/api/personas";
        }
        
        public String getClientesUrl() {
            return baseUrl + "/api/clientes";
        }
    }
    
    @Data
    public static class CuentasMovimientos {
        private String baseUrl = "http://localhost:8082";
        
        public String getCuentasUrl() {
            return baseUrl + "/api/cuentas";
        }
        
        public String getMovimientosUrl() {
            return baseUrl + "/api/movimientos";
        }
    }
}
