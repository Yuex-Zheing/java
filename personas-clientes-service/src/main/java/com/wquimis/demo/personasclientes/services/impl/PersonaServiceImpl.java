package com.wquimis.demo.personasclientes.services.impl;

import com.wquimis.demo.personasclientes.entities.Persona;
import com.wquimis.demo.personasclientes.exceptions.PersonaExistenteException;
import com.wquimis.demo.personasclientes.repository.PersonaRepository;
import com.wquimis.demo.personasclientes.services.PersonaService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;

@Service
public class PersonaServiceImpl implements PersonaService {

    private final PersonaRepository personaRepository;

    public PersonaServiceImpl(PersonaRepository personaRepository) {
        this.personaRepository = personaRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Persona> findAll() {
        return personaRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Persona findById(Long id) {
        return personaRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Persona no encontrada con ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Persona findByIdentificacion(String identificacion) {
        return personaRepository.findByIdentificacionpersona(identificacion)
            .orElseThrow(() -> new EntityNotFoundException("Persona no encontrada con identificación: " + identificacion));
    }

    @Override
    @Transactional
    public Persona save(Persona persona) {
        // Verificar si ya existe una persona con la misma identificación
        personaRepository.findByIdentificacionpersona(persona.getIdentificacionpersona())
            .ifPresent(p -> {
                throw new PersonaExistenteException(persona.getIdentificacionpersona());
            });

        if (persona.getEstado() == null) {
            persona.setEstado(true);
        }
        return personaRepository.save(persona);
    }

    @Override
    @Transactional
    public Persona update(Long id, Persona persona) {
        Persona existingPersona = findById(id);
        existingPersona.setNombres(persona.getNombres());
        existingPersona.setGenero(persona.getGenero());
        existingPersona.setEdad(persona.getEdad());
        existingPersona.setDireccion(persona.getDireccion());
        existingPersona.setTelefono(persona.getTelefono());
        existingPersona.setEstado(persona.getEstado());
        return personaRepository.save(existingPersona);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Persona persona = findById(id);
        persona.setEstado(false);
        personaRepository.save(persona);
    }
}
