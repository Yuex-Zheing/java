package com.wquimis.demo.cuentasmovimientos.dto;

import lombok.Data;

@Data
public class ErrorDTO {
    private String codigo;
    private String mensaje;
    private String detalle;
    private long timestamp;

    public ErrorDTO(String codigo, String mensaje, String detalle) {
        this.codigo = codigo;
        this.mensaje = mensaje;
        this.detalle = detalle;
        this.timestamp = System.currentTimeMillis();
    }

    public ErrorDTO(String mensaje, String detalle) {
        this.mensaje = mensaje;
        this.detalle = detalle;
        this.timestamp = System.currentTimeMillis();
    }

    public static ErrorDTO of(String codigo, String mensaje, String detalle) {
        return new ErrorDTO(codigo, mensaje, detalle);
    }
}
