package com.wquimis.demo.banking.utils;

import com.wquimis.demo.banking.dto.*;
import com.wquimis.demo.banking.entities.*;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Component
public class DtoConverter {

    public static PersonaDTO toPersonaDTO(Persona persona) {
        PersonaDTO dto = new PersonaDTO();
        dto.setId(persona.getIdpersona());
        dto.setIdentificacion(persona.getIdentificacionpersona());
        dto.setNombres(persona.getNombres());
        dto.setGenero(persona.getGenero());
        dto.setEdad(persona.getEdad());
        dto.setDireccion(persona.getDireccion());
        dto.setTelefono(persona.getTelefono());
        return dto;
    }

    public static ClienteDTO toClienteDTO(Cliente cliente) {
        ClienteDTO dto = new ClienteDTO();
        dto.setId(cliente.getIdcliente());
        dto.setNombreUsuario(cliente.getNombreusuario());
        dto.setContrasena(cliente.getContrasena());
        if (cliente.getPersona() != null) {
            dto.setPersonaId(cliente.getPersona().getIdpersona());
        }
        return dto;
    }

    public static Persona toPersona(PersonaDTO dto) {
        Persona persona = new Persona();
        if (dto.getId() != null) {
            persona.setIdpersona(dto.getId());
        }
        persona.setIdentificacionpersona(dto.getIdentificacion());
        persona.setNombres(dto.getNombres());
        persona.setGenero(dto.getGenero());
        persona.setEdad(dto.getEdad());
        persona.setDireccion(dto.getDireccion());
        persona.setTelefono(dto.getTelefono());
        persona.setEstado(true);
        return persona;
    }

    public static Cliente toCliente(ClienteDTO dto) {
        Cliente cliente = new Cliente();
        if (dto.getId() != null) {
            cliente.setIdcliente(dto.getId());
        }
        cliente.setNombreusuario(dto.getNombreUsuario());
        cliente.setContrasena(dto.getContrasena());
        cliente.setEstado(true);
        return cliente;
    }

    public Persona toEntity(PersonaDTO dto) {
        Persona persona = new Persona();
        persona.setIdentificacionpersona(dto.getIdentificacion());
        persona.setNombres(dto.getNombres());
        persona.setGenero(dto.getGenero());
        persona.setEdad(dto.getEdad());
        persona.setDireccion(dto.getDireccion());
        persona.setTelefono(dto.getTelefono());
        persona.setEstado(true);
        return persona;
    }

    public Cliente toEntity(ClienteDTO dto, Persona persona) {
        Cliente cliente = new Cliente();
        cliente.setPersona(persona);
        cliente.setNombreusuario(dto.getNombreUsuario());
        cliente.setContrasena(dto.getContrasena());
        cliente.setEstado(true);
        return cliente;
    }

    public Cuenta toEntity(CuentaDTO dto, Cliente cliente) {
        Cuenta cuenta = new Cuenta();
        cuenta.setNumerocuenta(dto.getNumeroCuenta());
        cuenta.setCliente(cliente);
        cuenta.setTipocuenta(dto.getTipoCuenta());
        cuenta.setSaldoinicial(dto.getSaldoInicial());
        cuenta.setEstado(true);
        cuenta.setFechacreacion(LocalDateTime.now());
        return cuenta;
    }

    public Movimiento toEntity(MovimientoDTO dto, Cuenta cuenta) {
        Movimiento movimiento = new Movimiento();
        movimiento.setCuenta(cuenta);
        movimiento.setTipomovimiento(Movimiento.TipoMovimiento.valueOf(dto.getTipoMovimiento()));
        movimiento.setMontomovimiento(dto.getMonto());
        movimiento.setEstado(true);
        return movimiento;
    }

    public PersonaDTO toDto(Persona persona) {
        PersonaDTO dto = new PersonaDTO();
        dto.setIdentificacion(persona.getIdentificacionpersona());
        dto.setNombres(persona.getNombres());
        dto.setGenero(persona.getGenero());
        dto.setEdad(persona.getEdad());
        dto.setDireccion(persona.getDireccion());
        dto.setTelefono(persona.getTelefono());
        return dto;
    }

    public ClienteDTO toDto(Cliente cliente) {
        ClienteDTO dto = new ClienteDTO();
        dto.setIdentificacionPersona(cliente.getPersona().getIdentificacionpersona());
        dto.setNombreUsuario(cliente.getNombreusuario());
        return dto;
    }

    public CuentaDTO toDto(Cuenta cuenta) {
        CuentaDTO dto = new CuentaDTO();
        dto.setNumeroCuenta(cuenta.getNumerocuenta());
        dto.setIdCliente(cuenta.getCliente().getIdcliente());
        dto.setTipoCuenta(cuenta.getTipocuenta());
        dto.setSaldoInicial(cuenta.getSaldoinicial());
        return dto;
    }

    public MovimientoDTO toDto(Movimiento movimiento) {
        MovimientoDTO dto = new MovimientoDTO();
        dto.setNumeroCuenta(movimiento.getCuenta().getNumerocuenta());
        dto.setTipoMovimiento(movimiento.getTipomovimiento().toString());
        dto.setMonto(movimiento.getMontomovimiento().abs());
        return dto;
    }
}
