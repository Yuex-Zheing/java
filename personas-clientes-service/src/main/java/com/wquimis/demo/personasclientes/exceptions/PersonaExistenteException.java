package com.wquimis.demo.personasclientes.exceptions;

public class PersonaExistenteException extends RuntimeException {
    public PersonaExistenteException(String identificacion) {
        super("Ya existe una persona con la identificación: " + identificacion);
    }
}
