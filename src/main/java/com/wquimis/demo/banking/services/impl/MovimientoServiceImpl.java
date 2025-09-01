package com.wquimis.demo.banking.services.impl;

import com.wquimis.demo.banking.entities.Movimiento;
import com.wquimis.demo.banking.entities.Cuenta;
import com.wquimis.demo.banking.exceptions.SaldoNoDisponibleException;
import com.wquimis.demo.banking.repository.MovimientoRepository;
import com.wquimis.demo.banking.services.MovimientoService;
import com.wquimis.demo.banking.services.CuentaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class MovimientoServiceImpl implements MovimientoService {

    @Autowired
    private MovimientoRepository movimientoRepository;

    @Autowired
    private CuentaService cuentaService;

    @Override
    @Transactional(readOnly = true)
    public List<Movimiento> findAll() {
        return movimientoRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Movimiento findById(Long id) {
        return movimientoRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Movimiento no encontrado con ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Movimiento> findByNumeroCuenta(Integer numeroCuenta) {
        return movimientoRepository.findByCuentaNumerocuenta(numeroCuenta);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Movimiento> findByNumeroCuentaAndFechaBetween(Integer numeroCuenta, LocalDate fechaInicio, LocalDate fechaFin) {
        return movimientoRepository.findByCuentaNumerocuentaAndFechamovimientoBetween(numeroCuenta, fechaInicio, fechaFin);
    }

    @Override
    @Transactional
    public Movimiento realizarMovimiento(Movimiento movimiento) {
        if (movimiento.getMontomovimiento() == null || movimiento.getMontomovimiento().compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalArgumentException("El monto del movimiento debe ser diferente de cero");
        }

        Cuenta cuenta = cuentaService.findByNumeroCuenta(movimiento.getCuenta().getNumerocuenta());

        if (!cuenta.getEstado()) {
            throw new IllegalStateException("La cuenta no está activa");
        }

        BigDecimal saldoActual = cuenta.getSaldoinicial();

        if (movimiento.getTipomovimiento() == Movimiento.TipoMovimiento.RETIRO) {
            BigDecimal montoRetiro = movimiento.getMontomovimiento().abs();
            if (saldoActual.compareTo(montoRetiro) < 0) {
                throw new SaldoNoDisponibleException("Saldo no disponible");
            }
            saldoActual = saldoActual.subtract(montoRetiro);
            movimiento.setMovimientodescripcion("Retiro en efectivo por " + montoRetiro.toString());
            movimiento.setMontomovimiento(montoRetiro.negate());
        } else {
            BigDecimal montoDeposito = movimiento.getMontomovimiento().abs();
            saldoActual = saldoActual.add(montoDeposito);
            movimiento.setMovimientodescripcion("Depósito en efectivo por " + montoDeposito.toString());
            movimiento.setMontomovimiento(montoDeposito);
        }

        movimiento.setSaldodisponible(saldoActual);
        movimiento.setFechamovimiento(LocalDate.now());
        movimiento.setHoramovimiento(LocalTime.now());
        movimiento.setEstado(true);

        cuenta.setSaldoinicial(saldoActual);
        cuentaService.update(cuenta.getNumerocuenta(), cuenta);

        return movimientoRepository.save(movimiento);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Movimiento movimiento = findById(id);
        movimiento.setEstado(false);
        movimientoRepository.save(movimiento);
    }
}
