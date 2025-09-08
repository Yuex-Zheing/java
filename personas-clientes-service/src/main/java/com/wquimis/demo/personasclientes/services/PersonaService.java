package com.wquimis.demo.personasclientes.services;

import com.wquimis.demo.personasclientes.entities.Persona;
import java.util.List;

public interface PersonaService {
    List<Persona> findAll();
    Persona findById(Long id);
    Persona findByIdentificacion(String identificacion);
    Persona findByIdentificacionOptional(String identificacion);
    Persona save(Persona persona);
    Persona update(Long id, Persona persona);
    void delete(Long id);
}
