package com.wquimis.demo.personasclientes.dto;

import lombok.Data;

@Data
public class ErrorDTO {
    private String mensaje;
    private String detalle;
    private long timestamp;

    public ErrorDTO(String mensaje, String detalle) {
        this.mensaje = mensaje;
        this.detalle = detalle;
        this.timestamp = System.currentTimeMillis();
    }
}
