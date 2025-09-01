package com.wquimis.demo.banking.controller;

import com.wquimis.demo.banking.dto.PersonaDTO;
import com.wquimis.demo.banking.dto.ClienteDTO;
import com.wquimis.demo.banking.entities.Persona;
import com.wquimis.demo.banking.entities.Cliente;
import com.wquimis.demo.banking.services.PersonaService;
import com.wquimis.demo.banking.services.ClienteService;
import com.wquimis.demo.banking.utils.DtoConverter;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class PersonaClienteController {

    @Autowired
    private PersonaService personaService;

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private DtoConverter dtoConverter;

    // Endpoints para Persona
    @GetMapping("/personas")
    public ResponseEntity<List<PersonaDTO>> getAllPersonas() {
        List<PersonaDTO> personas = personaService.findAll().stream()
            .map(dtoConverter::toDto)
            .collect(Collectors.toList());
        return ResponseEntity.ok(personas);
    }

    @GetMapping("/personas/{id}")
    public ResponseEntity<PersonaDTO> getPersonaById(@PathVariable Long id) {
        return ResponseEntity.ok(dtoConverter.toDto(personaService.findById(id)));
    }

    @GetMapping("/personas/identificacion/{identificacion}")
    public ResponseEntity<PersonaDTO> getPersonaByIdentificacion(@PathVariable String identificacion) {
        return ResponseEntity.ok(dtoConverter.toDto(personaService.findByIdentificacion(identificacion)));
    }

    @PostMapping("/personas")
    public ResponseEntity<PersonaDTO> createPersona(@Valid @RequestBody PersonaDTO personaDto) {
        Persona persona = dtoConverter.toEntity(personaDto);
        return ResponseEntity.ok(dtoConverter.toDto(personaService.save(persona)));
    }

    @PutMapping("/personas/{id}")
    public ResponseEntity<PersonaDTO> updatePersona(@PathVariable Long id, @Valid @RequestBody PersonaDTO personaDto) {
        Persona persona = dtoConverter.toEntity(personaDto);
        return ResponseEntity.ok(dtoConverter.toDto(personaService.update(id, persona)));
    }

    @DeleteMapping("/personas/{id}")
    public ResponseEntity<Void> deletePersona(@PathVariable Long id) {
        personaService.delete(id);
        return ResponseEntity.ok().build();
    }

    // Endpoints para Cliente
    @GetMapping("/clientes")
    public ResponseEntity<List<ClienteDTO>> getAllClientes() {
        List<ClienteDTO> clientes = clienteService.findAll().stream()
            .map(dtoConverter::toDto)
            .collect(Collectors.toList());
        return ResponseEntity.ok(clientes);
    }

    @GetMapping("/clientes/{id}")
    public ResponseEntity<ClienteDTO> getClienteById(@PathVariable Long id) {
        return ResponseEntity.ok(dtoConverter.toDto(clienteService.findById(id)));
    }

    @GetMapping("/clientes/identificacion/{identificacion}")
    public ResponseEntity<ClienteDTO> getClienteByIdentificacion(@PathVariable String identificacion) {
        return ResponseEntity.ok(dtoConverter.toDto(clienteService.findByIdentificacionPersona(identificacion)));
    }

    @PostMapping("/clientes")
    public ResponseEntity<ClienteDTO> createCliente(@Valid @RequestBody ClienteDTO clienteDto) {
        Persona persona = personaService.findByIdentificacion(clienteDto.getIdentificacionPersona());
        Cliente cliente = dtoConverter.toEntity(clienteDto, persona);
        return ResponseEntity.ok(dtoConverter.toDto(clienteService.save(cliente)));
    }

    @PutMapping("/clientes/{id}")
    public ResponseEntity<ClienteDTO> updateCliente(@PathVariable Long id, @Valid @RequestBody ClienteDTO clienteDto) {
        Cliente cliente = clienteService.findById(id);
        Cliente clienteActualizado = dtoConverter.toEntity(clienteDto, cliente.getPersona());
        return ResponseEntity.ok(dtoConverter.toDto(clienteService.update(id, clienteActualizado)));
    }

    @DeleteMapping("/clientes/{id}")
    public ResponseEntity<Void> deleteCliente(@PathVariable Long id) {
        clienteService.delete(id);
        return ResponseEntity.ok().build();
    }
}
