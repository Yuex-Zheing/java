package com.wquimis.demo.personasclientes.services;

import com.wquimis.demo.personasclientes.entities.Cliente;
import java.util.List;

public interface ClienteService {
    List<Cliente> findAll();
    Cliente findById(Long id);
    Cliente findByIdentificacionPersona(String identificacionPersona);
    Cliente save(Cliente cliente);
    Cliente update(Long id, Cliente cliente);
    void delete(Long id);
}
