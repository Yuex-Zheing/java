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

        BigDecimal saldoDisponible = cuenta.getSaldodisponible();

        if (movimiento.getTipomovimiento() == Movimiento.TipoMovimiento.RETIRO) {
            BigDecimal montoRetiro = movimiento.getMontomovimiento().abs();
            if (saldoDisponible.compareTo(montoRetiro) < 0) {
                throw new SaldoNoDisponibleException("Saldo no disponible");
            }
            saldoDisponible = saldoDisponible.subtract(montoRetiro);
            movimiento.setMovimientodescripcion("Retiro en efectivo por " + montoRetiro.toString());
            movimiento.setMontomovimiento(montoRetiro.negate());
        } else {
            BigDecimal montoDeposito = movimiento.getMontomovimiento().abs();
            saldoDisponible = saldoDisponible.add(montoDeposito);
            movimiento.setMovimientodescripcion("Depósito en efectivo por " + montoDeposito.toString());
            movimiento.setMontomovimiento(montoDeposito);
        }

        movimiento.setSaldodisponible(saldoDisponible);
        movimiento.setFechamovimiento(LocalDate.now());
        movimiento.setHoramovimiento(LocalTime.now());
        movimiento.setEstado(true);

        // Solo actualizamos el saldo disponible, no el saldo inicial
        cuenta.setSaldodisponible(saldoDisponible);
        cuentaService.update(cuenta.getNumerocuenta(), cuenta);

        return movimientoRepository.save(movimiento);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Movimiento movimiento = findById(id);
        Cuenta cuenta = movimiento.getCuenta();
        
        // Revertir el efecto del movimiento en el saldo
        BigDecimal saldoActual = cuenta.getSaldoinicial();
        
        // Obtener el monto absoluto para la descripción
        BigDecimal montoAbsoluto = movimiento.getMontomovimiento().abs();
        
        if (movimiento.getTipomovimiento() == Movimiento.TipoMovimiento.RETIRO) {
            // Si era un retiro, sumamos el monto al saldo (revertimos el retiro)
            saldoActual = saldoActual.subtract(movimiento.getMontomovimiento()); // El monto es negativo en retiros
            movimiento.setMovimientodescripcion("Reverso de retiro por $" + montoAbsoluto.toString());
        } else {
            // Si era un depósito, restamos el monto del saldo (revertimos el depósito)
            saldoActual = saldoActual.subtract(movimiento.getMontomovimiento()); // El monto es positivo en depósitos
            movimiento.setMovimientodescripcion("Reverso de depósito por $" + montoAbsoluto.toString());
        }
        
        // Actualizar saldo de la cuenta
        cuenta.setSaldoinicial(saldoActual);
        cuenta.setSaldodisponible(saldoActual);
        cuentaService.update(cuenta.getNumerocuenta(), cuenta);
        
        // Marcar el movimiento como eliminado y actualizar descripción y saldo
        movimiento.setEstado(false);
        movimiento.setSaldodisponible(saldoActual);
        movimientoRepository.save(movimiento);
    }
}
