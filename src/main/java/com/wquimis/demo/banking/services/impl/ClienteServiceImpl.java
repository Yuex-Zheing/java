package com.wquimis.demo.banking.services.impl;

import com.wquimis.demo.banking.entities.Cliente;
import com.wquimis.demo.banking.exceptions.ClienteExistenteException;
import com.wquimis.demo.banking.repository.ClienteRepository;
import com.wquimis.demo.banking.services.ClienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;

@Service
public class ClienteServiceImpl implements ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Cliente> findAll() {
        return clienteRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Cliente findById(Long id) {
        return clienteRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Cliente no encontrado con ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Cliente findByIdentificacionPersona(String identificacionPersona) {
        return clienteRepository.findByPersonaIdentificacionpersona(identificacionPersona)
            .orElseThrow(() -> new EntityNotFoundException("Cliente no encontrado con identificación: " + identificacionPersona));
    }

    @Override
    @Transactional
    public Cliente save(Cliente cliente) {
        // Verificar si ya existe un cliente para esta persona
        clienteRepository.findByPersonaIdpersona(cliente.getPersona().getIdpersona())
            .ifPresent(c -> {
                throw new ClienteExistenteException(cliente.getPersona().getIdpersona());
            });

        if (cliente.getEstado() == null) {
            cliente.setEstado(true);
        }
        return clienteRepository.save(cliente);
    }

    @Override
    @Transactional
    public Cliente update(Long id, Cliente cliente) {
        Cliente existingCliente = findById(id);
        existingCliente.setNombreusuario(cliente.getNombreusuario());
        existingCliente.setContrasena(cliente.getContrasena());
        // Mantener el estado actual si no se proporciona uno nuevo
        if (cliente.getEstado() != null) {
            existingCliente.setEstado(cliente.getEstado());
        }
        return clienteRepository.save(existingCliente);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Cliente cliente = findById(id);
        cliente.setEstado(false);
        clienteRepository.save(cliente);
    }
}
