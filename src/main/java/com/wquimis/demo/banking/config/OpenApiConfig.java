package com.wquimis.demo.banking.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI bankingOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Banking System API")
                .description("API REST para sistema bancario con gestión de clientes, cuentas y movimientos")
                .version("1.0")
                .contact(new Contact()
                    .name("William Quimis")
                    .email("wquimis@example.com")));
    }
}
