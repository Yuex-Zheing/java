package com.wquimis.demo.banking.exceptions;

public class CuentaExistenteException extends RuntimeException {
    public CuentaExistenteException(String message) {
        super(message);
    }
}
