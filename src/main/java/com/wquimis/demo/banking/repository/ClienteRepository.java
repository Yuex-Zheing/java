package com.wquimis.demo.banking.repository;

import com.wquimis.demo.banking.entities.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    Optional<Cliente> findByNombreusuario(String nombreusuario);
    Optional<Cliente> findByPersonaIdentificacionpersona(String identificacionPersona);
}
