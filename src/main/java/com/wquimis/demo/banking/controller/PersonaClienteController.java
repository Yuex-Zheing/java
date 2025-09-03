package com.wquimis.demo.banking.controller;

import com.wquimis.demo.banking.dto.ErrorDTO;
import com.wquimis.demo.banking.dto.PersonaDTO;
import com.wquimis.demo.banking.dto.PersonaUpdateDTO;
import com.wquimis.demo.banking.dto.ClienteDTO;
import com.wquimis.demo.banking.entities.Persona;
import com.wquimis.demo.banking.entities.Cliente;
import com.wquimis.demo.banking.services.PersonaService;
import com.wquimis.demo.banking.services.ClienteService;
import com.wquimis.demo.banking.utils.DtoConverter;
import org.springframework.http.HttpStatus;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@Tag(name = "Gestión de Personas y Clientes", description = "APIs para gestionar personas y clientes")
public class PersonaClienteController {

    @Autowired
    private PersonaService personaService;

    @Autowired
    private ClienteService clienteService;

    // Endpoints para Personas
    @Operation(summary = "Obtener todas las personas activas")
    @GetMapping("/personas")
    public ResponseEntity<List<PersonaDTO>> getAllPersonas() {
        List<Persona> personas = personaService.findAll();
        List<PersonaDTO> personasDTO = personas.stream()
                .filter(p -> Boolean.TRUE.equals(p.getEstado()))
                .map(DtoConverter::toPersonaDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(personasDTO);
    }

    @Operation(summary = "Obtener una persona por ID")
    @GetMapping("/personas/{id}")
    public ResponseEntity<PersonaDTO> getPersonaById(
            @Parameter(description = "ID de la persona") @PathVariable Long id) {
        Persona persona = personaService.findById(id);
        if (persona != null) {
            return ResponseEntity.ok(DtoConverter.toPersonaDTO(persona));
        }
        return ResponseEntity.notFound().build();
    }

    @Operation(summary = "Crear una nueva persona")
    @PostMapping("/personas")
    public ResponseEntity<PersonaDTO> createPersona(
            @Parameter(description = "Datos de la persona") @Valid @RequestBody PersonaDTO personaDTO) {
        Persona persona = DtoConverter.toPersona(personaDTO);
        Persona savedPersona = personaService.save(persona);
        return ResponseEntity.ok(DtoConverter.toPersonaDTO(savedPersona));
    }

    @Operation(summary = "Actualizar una persona existente")
    @PutMapping("/personas/{id}")
    public ResponseEntity<?> updatePersona(
            @Parameter(description = "ID de la persona") @PathVariable Long id,
            @Parameter(description = "Datos actualizados de la persona") @Valid @RequestBody PersonaUpdateDTO personaUpdateDTO) {
        try {
            Persona existingPersona = personaService.findById(id);
            Persona persona = DtoConverter.updatePersonaFromDTO(personaUpdateDTO, existingPersona);
            Persona updatedPersona = personaService.update(id, persona);
            return ResponseEntity.ok(DtoConverter.toPersonaDTO(updatedPersona));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorDTO.of("ERR_001", 
                    "Persona no encontrada con ID: " + id,
                    "La persona que intenta actualizar no existe"));
        }
    }

    @Operation(summary = "Eliminar una persona")
    @DeleteMapping("/personas/{id}")
    public ResponseEntity<?> deletePersona(
            @Parameter(description = "ID de la persona") @PathVariable Long id) {
        Persona persona = personaService.findById(id);
        if (persona == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorDTO.of("ERR_001", 
                    "Persona no encontrada con ID: " + id,
                    "La persona que intenta eliminar no existe"));
        }
        personaService.delete(id);
        return ResponseEntity.ok(ErrorDTO.of("SUCCESS", 
            "Persona eliminada correctamente", 
            "La persona ha sido eliminada exitosamente"));
    }

    // Endpoints para Clientes
    @Operation(summary = "Obtener todos los clientes activos")
    @GetMapping("/clientes")
    public ResponseEntity<List<ClienteDTO>> getAllClientes() {
        List<Cliente> clientes = clienteService.findAll();
        List<ClienteDTO> clientesDTO = clientes.stream()
                .filter(c -> Boolean.TRUE.equals(c.getEstado()))
                .map(DtoConverter::toClienteDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(clientesDTO);
    }

    @Operation(summary = "Obtener un cliente por ID")
    @GetMapping("/clientes/{id}")
    public ResponseEntity<ClienteDTO> getClienteById(
            @Parameter(description = "ID del cliente") @PathVariable Long id) {
        Cliente cliente = clienteService.findById(id);
        if (cliente != null) {
            return ResponseEntity.ok(DtoConverter.toClienteDTO(cliente));
        }
        return ResponseEntity.notFound().build();
    }

    @Operation(summary = "Crear un nuevo cliente")
    @PostMapping("/clientes")
    public ResponseEntity<ClienteDTO> createCliente(
            @Parameter(description = "Datos del cliente") @Valid @RequestBody ClienteDTO clienteDTO) {
        // Verificar que exista la persona
        Persona persona = personaService.findById(clienteDTO.getPersonaId());
        if (persona == null) {
            return ResponseEntity.badRequest().build();
        }

        Cliente cliente = DtoConverter.toCliente(clienteDTO);
        cliente.setPersona(persona);
        cliente.setEstado(true);
        Cliente savedCliente = clienteService.save(cliente);
        return ResponseEntity.ok(DtoConverter.toClienteDTO(savedCliente));
    }

    @Operation(summary = "Actualizar un cliente existente")
    @PutMapping("/clientes/{id}")
    public ResponseEntity<ClienteDTO> updateCliente(
            @Parameter(description = "ID del cliente") @PathVariable Long id,
            @Parameter(description = "Datos actualizados del cliente") @Valid @RequestBody ClienteDTO clienteDTO) {
        Cliente existingCliente = clienteService.findById(id);
        if (existingCliente == null) {
            return ResponseEntity.notFound().build();
        }

        // Verificar que exista la persona si se está actualizando
        if (clienteDTO.getPersonaId() != null) {
            Persona persona = personaService.findById(clienteDTO.getPersonaId());
            if (persona == null) {
                return ResponseEntity.badRequest().build();
            }
            existingCliente.setPersona(persona);
        }

        existingCliente.setNombreusuario(clienteDTO.getNombreUsuario());
        if (clienteDTO.getContrasena() != null && !clienteDTO.getContrasena().isEmpty()) {
            existingCliente.setContrasena(clienteDTO.getContrasena());
        }

        Cliente updatedCliente = clienteService.save(existingCliente);
        return ResponseEntity.ok(DtoConverter.toClienteDTO(updatedCliente));
    }

    @Operation(summary = "Eliminar un cliente")
    @DeleteMapping("/clientes/{id}")
    public ResponseEntity<Void> deleteCliente(
            @Parameter(description = "ID del cliente") @PathVariable Long id) {
        Cliente cliente = clienteService.findById(id);
        if (cliente == null) {
            return ResponseEntity.notFound().build();
        }
        clienteService.delete(id);
        return ResponseEntity.ok().build();
    }
}
