package com.wquimis.demo.banking.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ErrorDTO {
    private String codigo;
    private String mensajeTecnico;
    private String mensajeNegocio;

    public static ErrorDTO of(String codigo, String mensajeTecnico, String mensajeNegocio) {
        return ErrorDTO.builder()
                .codigo(codigo)
                .mensajeTecnico(mensajeTecnico)
                .mensajeNegocio(mensajeNegocio)
                .build();
    }
}
