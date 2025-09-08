package com.wquimis.demo.cuentasmovimientos.exceptions;

public class SaldoNoDisponibleException extends RuntimeException {
    public SaldoNoDisponibleException(String message) {
        super(message);
    }
}
