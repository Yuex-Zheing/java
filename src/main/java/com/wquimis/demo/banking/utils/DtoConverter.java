package com.wquimis.demo.banking.utils;

import com.wquimis.demo.banking.dto.*;
import com.wquimis.demo.banking.entities.*;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;
import java.math.BigDecimal;

@Component
public class DtoConverter {

    public static Cliente createClienteFromDTO(CreateClienteDTO dto) {
        Cliente cliente = new Cliente();
        cliente.setNombreusuario(dto.getNombreUsuario());
        cliente.setContrasena(dto.getContrasena());
        cliente.setEstado(true);
        return cliente;
    }

    public static Persona updatePersonaFromDTO(PersonaUpdateDTO dto, Persona persona) {
        persona.setNombres(dto.getNombres());
        persona.setGenero(dto.getGenero());
        persona.setEdad(dto.getEdad());
        persona.setDireccion(dto.getDireccion());
        persona.setTelefono(dto.getTelefono());
        persona.setEstado(dto.getEstado());
        return persona;
    }

    public static PersonaDTO toPersonaDTO(Persona persona) {
        PersonaDTO dto = new PersonaDTO();
        dto.setId(persona.getIdpersona());
        dto.setIdentificacionpersona(persona.getIdentificacionpersona());
        dto.setNombres(persona.getNombres());
        dto.setGenero(persona.getGenero());
        dto.setEdad(persona.getEdad());
        dto.setDireccion(persona.getDireccion());
        dto.setTelefono(persona.getTelefono());
        dto.setEstado(persona.getEstado());
        return dto;
    }

    public static ClienteDTO toClienteDTO(Cliente cliente) {
        ClienteDTO dto = new ClienteDTO();
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

    public static Persona toPersona(PersonaDTO dto) {
        Persona persona = new Persona();
        if (dto.getId() != null) {
            persona.setIdpersona(dto.getId());
        }
        persona.setIdentificacionpersona(dto.getIdentificacionpersona());
        persona.setNombres(dto.getNombres());
        persona.setGenero(dto.getGenero());
        persona.setEdad(dto.getEdad());
        persona.setDireccion(dto.getDireccion());
        persona.setTelefono(dto.getTelefono());
        persona.setEstado(dto.getEstado() != null ? dto.getEstado() : true);
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
        persona.setIdentificacionpersona(dto.getIdentificacionpersona());
        persona.setNombres(dto.getNombres());
        persona.setGenero(dto.getGenero());
        persona.setEdad(dto.getEdad());
        persona.setDireccion(dto.getDireccion());
        persona.setTelefono(dto.getTelefono());
        persona.setEstado(dto.getEstado() != null ? dto.getEstado() : true);
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
        movimiento.setFechamovimiento(LocalDate.now());
        movimiento.setHoramovimiento(LocalTime.now());
        movimiento.setTipomovimiento(Movimiento.TipoMovimiento.valueOf(dto.getTipo()));
        // El valor será positivo para depósitos y negativo para retiros
        BigDecimal monto = new BigDecimal(dto.getValor());
        if (dto.getTipo().equals("RETIRO")) {
            monto = monto.negate();
        }
        movimiento.setMontomovimiento(monto);
        movimiento.setMovimientodescripcion(dto.getDescripcion());
        movimiento.setEstado(true);
        return movimiento;
    }

    public PersonaDTO toDto(Persona persona) {
        PersonaDTO dto = new PersonaDTO();
        dto.setId(persona.getIdpersona());
        dto.setIdentificacionpersona(persona.getIdentificacionpersona());
        dto.setNombres(persona.getNombres());
        dto.setGenero(persona.getGenero());
        dto.setEdad(persona.getEdad());
        dto.setDireccion(persona.getDireccion());
        dto.setTelefono(persona.getTelefono());
        dto.setEstado(persona.getEstado());
        return dto;
    }

    public ClienteDTO toDto(Cliente cliente) {
        ClienteDTO dto = new ClienteDTO();
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

    public CuentaDTO toDto(Cuenta cuenta) {
        CuentaDTO dto = new CuentaDTO();
        dto.setNumeroCuenta(cuenta.getNumerocuenta());
        dto.setIdCliente(cuenta.getCliente().getIdcliente());
        dto.setTipoCuenta(cuenta.getTipocuenta());
        dto.setSaldoInicial(cuenta.getSaldoinicial());
        dto.setEstado(cuenta.getEstado());
        return dto;
    }

    public MovimientoDTO toDto(Movimiento movimiento) {
        MovimientoDTO dto = new MovimientoDTO();
        dto.setId(movimiento.getIdmovimiento());
        dto.setFecha(movimiento.getFechamovimiento().format(
            java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        dto.setHora(movimiento.getHoramovimiento().format(
            java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss.SSS")));
        dto.setDescripcion(movimiento.getMovimientodescripcion());
        dto.setTipo(movimiento.getTipomovimiento().toString());
        dto.setValor(movimiento.getMontomovimiento().abs().toPlainString());
        dto.setSaldo(movimiento.getSaldodisponible());
        
        // Campo interno
        if (movimiento.getCuenta() != null) {
            dto.setNumeroCuenta(movimiento.getCuenta().getNumerocuenta());
        }
        return dto;
    }
}
