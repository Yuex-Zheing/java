package com.wquimis.demo.banking.exceptions;

public class ClienteExistenteException extends RuntimeException {
    public ClienteExistenteException(Long personaId) {
        super("Ya existe un cliente para la persona con ID: " + personaId);
    }
}
