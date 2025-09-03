package com.wquimis.demo.banking.exceptions;

public class PersonaExistenteException extends RuntimeException {
    public PersonaExistenteException(String identificacion) {
        super("Persona ya existe con identificación: " + identificacion);
    }
}
