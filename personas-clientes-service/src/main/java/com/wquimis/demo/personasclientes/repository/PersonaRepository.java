package com.wquimis.demo.personasclientes.repository;

import com.wquimis.demo.personasclientes.entities.Persona;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PersonaRepository extends JpaRepository<Persona, Long> {
    Optional<Persona> findByIdentificacionpersona(String identificacionpersona);
}
