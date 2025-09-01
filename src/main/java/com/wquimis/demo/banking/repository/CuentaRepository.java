package com.wquimis.demo.banking.repository;

import com.wquimis.demo.banking.entities.Cuenta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CuentaRepository extends JpaRepository<Cuenta, Integer> {
    List<Cuenta> findByClienteIdcliente(Long idCliente);
    List<Cuenta> findByClientePersonaIdentificacionpersona(String identificacionPersona);
}
