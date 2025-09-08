package com.wquimis.demo.cuentasmovimientos.utils;

import com.wquimis.demo.cuentasmovimientos.dto.*;
import com.wquimis.demo.cuentasmovimientos.entities.*;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;
import java.math.BigDecimal;

@Component
public class DtoConverter {

    public Cuenta toEntity(CuentaDTO dto) {
        Cuenta cuenta = new Cuenta();
        cuenta.setNumerocuenta(dto.getNumeroCuenta());
        cuenta.setIdcliente(dto.getIdCliente());
        cuenta.setTipocuenta(Cuenta.TipoCuenta.valueOf(dto.getTipoCuenta()));
        cuenta.setSaldoinicial(dto.getSaldoInicial());
        cuenta.setEstado(dto.getEstado());
        return cuenta;
    }

    public CuentaDTO toDto(Cuenta cuenta) {
        CuentaDTO dto = new CuentaDTO();
        dto.setNumeroCuenta(cuenta.getNumerocuenta());
        dto.setIdCliente(cuenta.getIdcliente());
        dto.setTipoCuenta(cuenta.getTipocuenta().toString());
        dto.setSaldoInicial(cuenta.getSaldoinicial());
        dto.setSaldoDisponible(cuenta.getSaldodisponible() != null ? 
            cuenta.getSaldodisponible() : cuenta.getSaldoinicial());
        dto.setEstado(cuenta.getEstado());
        dto.setFechaCreacion(cuenta.getFechacreacion());
        dto.setFechaCierre(cuenta.getFechacierre());
        return dto;
    }

    public Movimiento toEntity(MovimientoDTO dto) {
        Movimiento movimiento = new Movimiento();
        movimiento.setTipomovimiento(Movimiento.TipoMovimiento.valueOf(dto.getTipomovimiento()));
        movimiento.setMontomovimiento(dto.getMontomovimiento());
        movimiento.setMovimientodescripcion(dto.getMovimientodescripcion());
        return movimiento;
    }

    public MovimientoDTO toDto(Movimiento movimiento) {
        MovimientoDTO dto = new MovimientoDTO();
        dto.setId(movimiento.getIdmovimiento());
        dto.setNumeroCuenta(movimiento.getCuenta().getNumerocuenta());
        dto.setTipomovimiento(movimiento.getTipomovimiento().toString());
        dto.setMontomovimiento(movimiento.getMontomovimiento());
        dto.setFecha(movimiento.getFechamovimiento().toString());
        dto.setHora(movimiento.getHoramovimiento().toString());
        dto.setSaldo(movimiento.getSaldodisponible());
        dto.setMovimientodescripcion(movimiento.getMovimientodescripcion());
        
        // esReverso será true SOLO si el estado es false (operación anulada/reversada)
        // Los movimientos de reverso (que tienen estado = true) muestran esReverso = false
        dto.setEsReverso(!movimiento.getEstado());
        
        return dto;
    }
}
