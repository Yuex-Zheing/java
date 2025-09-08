package com.wquimis.demo.personasclientes.utils;

import com.wquimis.demo.personasclientes.dto.*;
import com.wquimis.demo.personasclientes.entities.*;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class DtoConverter {

    private final ModelMapper modelMapper;

    public DtoConverter(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public Cliente createClienteFromDTO(CreateClienteDTO dto) {
        Cliente cliente = modelMapper.map(dto, Cliente.class);
        cliente.setNombreusuario(dto.getNombreUsuario());
        cliente.setContrasena(dto.getContrasena());
        cliente.setEstado(true);
        return cliente;
    }

    public Persona updatePersonaFromDTO(PersonaUpdateDTO dto, Persona persona) {
        modelMapper.map(dto, persona);
        return persona;
    }

    public PersonaDTO toPersonaDTO(Persona persona) {
        PersonaDTO dto = modelMapper.map(persona, PersonaDTO.class);
        dto.setId(persona.getIdpersona());
        dto.setIdentificacionpersona(persona.getIdentificacionpersona());
        return dto;
    }

    public ClienteDTO toClienteDTO(Cliente cliente) {
        ClienteDTO dto = modelMapper.map(cliente, ClienteDTO.class);
        dto.setId(cliente.getIdcliente());
        dto.setNombreUsuario(cliente.getNombreusuario());
        dto.setContrasena(cliente.getContrasena());
        dto.setEstado(cliente.getEstado());
        if (cliente.getPersona() != null) {
            dto.setPersonaId(cliente.getPersona().getIdpersona());
            dto.setIdentificacionPersona(cliente.getPersona().getIdentificacionpersona());
        }
        return dto;
    }

    public Persona toPersona(PersonaDTO dto) {
        Persona persona = modelMapper.map(dto, Persona.class);
        if (dto.getId() != null) {
            persona.setIdpersona(dto.getId());
        }
        persona.setIdentificacionpersona(dto.getIdentificacionpersona());
        persona.setEstado(dto.getEstado() != null ? dto.getEstado() : true);
        return persona;
    }

    public Cliente toCliente(ClienteDTO dto) {
        Cliente cliente = modelMapper.map(dto, Cliente.class);
        if (dto.getId() != null) {
            cliente.setIdcliente(dto.getId());
        }
        cliente.setNombreusuario(dto.getNombreUsuario());
        cliente.setContrasena(dto.getContrasena());
        cliente.setEstado(true);
        return cliente;
    }

    public Persona toEntity(PersonaDTO dto) {
        Persona persona = modelMapper.map(dto, Persona.class);
        persona.setIdentificacionpersona(dto.getIdentificacionpersona());
        persona.setEstado(dto.getEstado() != null ? dto.getEstado() : true);
        return persona;
    }

    public Cliente toEntity(ClienteDTO dto, Persona persona) {
        Cliente cliente = modelMapper.map(dto, Cliente.class);
        cliente.setPersona(persona);
        cliente.setNombreusuario(dto.getNombreUsuario());
        cliente.setContrasena(dto.getContrasena());
        cliente.setEstado(true);
        return cliente;
    }

    public PersonaDTO toDto(Persona persona) {
        PersonaDTO dto = modelMapper.map(persona, PersonaDTO.class);
        dto.setId(persona.getIdpersona());
        dto.setIdentificacionpersona(persona.getIdentificacionpersona());
        return dto;
    }

    public ClienteDTO toDto(Cliente cliente) {
        ClienteDTO dto = modelMapper.map(cliente, ClienteDTO.class);
        dto.setId(cliente.getIdcliente());
        if (cliente.getPersona() != null) {
            dto.setPersonaId(cliente.getPersona().getIdpersona());
            dto.setIdentificacionPersona(cliente.getPersona().getIdentificacionpersona());
        }
        dto.setNombreUsuario(cliente.getNombreusuario());
        dto.setContrasena(cliente.getContrasena());
        dto.setEstado(cliente.getEstado());
        return dto;
    }
}
