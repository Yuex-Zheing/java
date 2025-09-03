package com.wquimis.demo.banking.exceptions;

public class OnboardingException extends RuntimeException {
    private final String codigo;
    private final String mensajeNegocio;

    public OnboardingException(String codigo, String mensaje, String mensajeNegocio) {
        super(mensaje);
        this.codigo = codigo;
        this.mensajeNegocio = mensajeNegocio;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getMensajeNegocio() {
        return mensajeNegocio;
    }
}
