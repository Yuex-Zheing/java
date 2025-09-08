package com.wquimis.demo.personasclientes.controller;

import com.wquimis.demo.personasclientes.dto.ErrorDTO;
import com.wquimis.demo.personasclientes.dto.PersonaDTO;
import com.wquimis.demo.personasclientes.dto.PersonaUpdateDTO;
import com.wquimis.demo.personasclientes.dto.ClienteDTO;
import com.wquimis.demo.personasclientes.dto.CreateClienteDTO;
import com.wquimis.demo.personasclientes.dto.UpdateClienteDTO;
import com.wquimis.demo.personasclientes.exceptions.ClienteExistenteException;
import com.wquimis.demo.personasclientes.entities.Persona;
import com.wquimis.demo.personasclientes.entities.Cliente;
import com.wquimis.demo.personasclientes.services.PersonaService;
import com.wquimis.demo.personasclientes.services.ClienteService;
import com.wquimis.demo.personasclientes.utils.DtoConverter;
import org.springframework.http.HttpStatus;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@Tag(name = "Gestión de Personas y Clientes", description = "APIs para gestionar personas y clientes")
public class PersonaClienteController {

    private final PersonaService personaService;
    private final ClienteService clienteService;
    private final DtoConverter dtoConverter;

    public PersonaClienteController(PersonaService personaService, ClienteService clienteService, DtoConverter dtoConverter) {
        this.personaService = personaService;
        this.clienteService = clienteService;
        this.dtoConverter = dtoConverter;
    }

    // Endpoints para Personas
    @Operation(summary = "Obtener todas las personas activas")
    @GetMapping("/personas")
    public ResponseEntity<List<PersonaDTO>> getAllPersonas() {
        List<Persona> personas = personaService.findAll();
        List<PersonaDTO> personasDTO = personas.stream()
                .filter(p -> Boolean.TRUE.equals(p.getEstado()))
                .map(dtoConverter::toPersonaDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(personasDTO);
    }

    @Operation(summary = "Obtener una persona por ID")
    @GetMapping("/personas/{id}")
    public ResponseEntity<PersonaDTO> getPersonaById(
            @Parameter(description = "ID de la persona") @PathVariable Long id) {
        Persona persona = personaService.findById(id);
        if (persona != null) {
            return ResponseEntity.ok(dtoConverter.toPersonaDTO(persona));
        }
        return ResponseEntity.notFound().build();
    }

    @Operation(summary = "Crear una nueva persona")
    @PostMapping("/personas")
    public ResponseEntity<PersonaDTO> createPersona(
            @Parameter(description = "Datos de la persona") @Valid @RequestBody PersonaDTO personaDTO) {
        Persona persona = dtoConverter.toPersona(personaDTO);
        Persona savedPersona = personaService.save(persona);
        return ResponseEntity.ok(dtoConverter.toPersonaDTO(savedPersona));
    }

    @Operation(summary = "Actualizar una persona existente")
    @PutMapping("/personas/{id}")
    public ResponseEntity<?> updatePersona(
            @Parameter(description = "ID de la persona") @PathVariable Long id,
            @Parameter(description = "Datos actualizados de la persona") @Valid @RequestBody PersonaUpdateDTO personaUpdateDTO) {
        try {
            Persona existingPersona = personaService.findById(id);
            Persona updatedPersona = dtoConverter.updatePersonaFromDTO(personaUpdateDTO, existingPersona);
            Persona savedPersona = personaService.update(id, updatedPersona);
            return ResponseEntity.ok(dtoConverter.toPersonaDTO(savedPersona));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorDTO("Persona no encontrada", e.getMessage()));
        }
    }

    @Operation(summary = "Eliminar una persona")
    @DeleteMapping("/personas/{id}")
    public ResponseEntity<?> deletePersona(
            @Parameter(description = "ID de la persona") @PathVariable Long id) {
        try {
            personaService.delete(id);
            return ResponseEntity.ok().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorDTO("Persona no encontrada", e.getMessage()));
        }
    }

    // Endpoints para Clientes
    @Operation(summary = "Obtener todos los clientes activos")
    @GetMapping("/clientes")
    public ResponseEntity<List<ClienteDTO>> getAllClientes() {
        List<Cliente> clientes = clienteService.findAll();
        List<ClienteDTO> clientesDTO = clientes.stream()
                .filter(c -> Boolean.TRUE.equals(c.getEstado()))
                .map(dtoConverter::toClienteDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(clientesDTO);
    }

    @Operation(summary = "Obtener un cliente por ID")
    @GetMapping("/clientes/{id}")
    public ResponseEntity<ClienteDTO> getClienteById(
            @Parameter(description = "ID del cliente") @PathVariable Long id) {
        Cliente cliente = clienteService.findById(id);
        if (cliente != null) {
            return ResponseEntity.ok(dtoConverter.toClienteDTO(cliente));
        }
        return ResponseEntity.notFound().build();
    }

    @Operation(summary = "Crear un nuevo cliente")
    @PostMapping("/clientes")
    public ResponseEntity<?> createCliente(
            @Parameter(description = "Datos del cliente") @Valid @RequestBody CreateClienteDTO createClienteDTO) {
        try {
            Persona persona = personaService.findById(createClienteDTO.getPersonaId());
            Cliente cliente = dtoConverter.createClienteFromDTO(createClienteDTO);
            cliente.setPersona(persona);
            Cliente savedCliente = clienteService.save(cliente);
            return ResponseEntity.ok(dtoConverter.toClienteDTO(savedCliente));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorDTO("Persona no encontrada", e.getMessage()));
        } catch (ClienteExistenteException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ErrorDTO("Cliente ya existe", e.getMessage()));
        }
    }

    @Operation(summary = "Actualizar un cliente existente")
    @PutMapping("/clientes/{id}")
    public ResponseEntity<?> updateCliente(
            @Parameter(description = "ID del cliente") @PathVariable Long id,
            @Parameter(description = "Datos actualizados del cliente") @Valid @RequestBody UpdateClienteDTO updateClienteDTO) {
        try {
            Cliente existingCliente = clienteService.findById(id);
            existingCliente.setNombreusuario(updateClienteDTO.getNombreUsuario());
            existingCliente.setContrasena(updateClienteDTO.getContrasena());
            existingCliente.setEstado(updateClienteDTO.getEstado());
            Cliente savedCliente = clienteService.update(id, existingCliente);
            return ResponseEntity.ok(dtoConverter.toClienteDTO(savedCliente));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorDTO("Cliente no encontrado", e.getMessage()));
        }
    }

    @Operation(summary = "Eliminar un cliente")
    @DeleteMapping("/clientes/{id}")
    public ResponseEntity<?> deleteCliente(
            @Parameter(description = "ID del cliente") @PathVariable Long id) {
        try {
            clienteService.delete(id);
            return ResponseEntity.ok().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorDTO("Cliente no encontrado", e.getMessage()));
        }
    }
}
