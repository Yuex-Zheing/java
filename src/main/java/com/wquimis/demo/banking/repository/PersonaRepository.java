package com.wquimis.demo.banking.repository;

import com.wquimis.demo.banking.entities.Persona;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PersonaRepository extends JpaRepository<Persona, Long> {
    Optional<Persona> findByIdentificacionpersona(String identificacionpersona);
}
