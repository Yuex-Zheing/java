package com.wquimis.demo.cuentasmovimientos.exceptions;

public class CuentaExistenteException extends RuntimeException {
    public CuentaExistenteException(String message) {
        super(message);
    }
}
