package com.wquimis.demo.banking.utils;

import com.wquimis.demo.banking.dto.*;
import com.wquimis.demo.banking.entities.*;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;
import java.math.BigDecimal;

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

    public Cuenta toEntity(CuentaDTO dto, Cliente cliente) {
        Cuenta cuenta = modelMapper.map(dto, Cuenta.class);
        cuenta.setNumerocuenta(dto.getNumeroCuenta());
        cuenta.setCliente(cliente);
        cuenta.setTipocuenta(dto.getTipoCuenta());
        cuenta.setSaldoinicial(dto.getSaldoInicial());
        cuenta.setEstado(true);
        cuenta.setFechacreacion(LocalDateTime.now());
        return cuenta;
    }

    public Movimiento toEntity(MovimientoDTO dto, Cuenta cuenta) {
        Movimiento movimiento = modelMapper.map(dto, Movimiento.class);
        movimiento.setCuenta(cuenta);
        movimiento.setFechamovimiento(LocalDate.now());
        movimiento.setHoramovimiento(LocalTime.now());
        movimiento.setTipomovimiento(Movimiento.TipoMovimiento.valueOf(dto.getTipomovimiento()));
        
        // El valor será positivo para depósitos y negativo para retiros
        BigDecimal monto = dto.getMontomovimiento();
        if (dto.getTipomovimiento().equals("RETIRO")) {
            monto = monto.negate();
        }
        movimiento.setMontomovimiento(monto);
        movimiento.setMovimientodescripcion(dto.getMovimientodescripcion());
        movimiento.setEstado(true);
        return movimiento;
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

    public CuentaDTO toDto(Cuenta cuenta) {
        CuentaDTO dto = modelMapper.map(cuenta, CuentaDTO.class);
        dto.setNumeroCuenta(cuenta.getNumerocuenta());
        dto.setIdCliente(cuenta.getCliente().getIdcliente());
        dto.setTipoCuenta(cuenta.getTipocuenta());
        dto.setSaldoInicial(cuenta.getSaldoinicial());
        dto.setEstado(cuenta.getEstado());
        return dto;
    }

    public MovimientoDTO toDto(Movimiento movimiento) {
        MovimientoDTO dto = modelMapper.map(movimiento, MovimientoDTO.class);
        dto.setId(movimiento.getIdmovimiento());
        dto.setFecha(movimiento.getFechamovimiento().format(
            java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        dto.setHora(movimiento.getHoramovimiento().format(
            java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss.SSS")));
        dto.setMovimientodescripcion(movimiento.getMovimientodescripcion());
        dto.setTipomovimiento(movimiento.getTipomovimiento().toString());
        dto.setMontomovimiento(movimiento.getMontomovimiento().abs());
        dto.setSaldo(movimiento.getSaldodisponible());
        
        // Campo interno
        if (movimiento.getCuenta() != null) {
            dto.setNumeroCuenta(movimiento.getCuenta().getNumerocuenta());
        }
        return dto;
    }
}
