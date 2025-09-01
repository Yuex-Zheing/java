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

        // Validar que el monto sea positivo para depósitos y negativo para retiros
        if (movimiento.getTipomovimiento() == Movimiento.TipoMovimiento.RETIRO) {
            BigDecimal montoRetiro = movimiento.getMontomovimiento().abs();
            if (saldoActual.compareTo(montoRetiro) < 0) {
                throw new SaldoNoDisponibleException("Saldo no disponible");
            }
            saldoActual = saldoActual.subtract(montoRetiro);
            movimiento.setMovimientodescripcion("Retiro en efectivo por " + montoRetiro.toString());
            movimiento.setMontomovimiento(montoRetiro.negate()); // Guardar como negativo
        } else {
            BigDecimal montoDeposito = movimiento.getMontomovimiento().abs();
            saldoActual = saldoActual.add(montoDeposito);
            movimiento.setMovimientodescripcion("Depósito en efectivo por " + montoDeposito.toString());
            movimiento.setMontomovimiento(montoDeposito); // Guardar como positivo
        }

        movimiento.setSaldodisponible(saldoActual);
        movimiento.setFechamovimiento(LocalDate.now());
        movimiento.setHoramovimiento(LocalTime.now());
        movimiento.setEstado(true);

        // Actualizar saldo de la cuenta
        cuenta.setSaldoinicial(saldoActual);
        cuentaService.update(cuenta.getNumerocuenta(), cuenta);

        return movimientoRepository.save(movimiento);
    }

    @Override
    public List<Movimiento> findByCuenta(String numeroCuenta) {
        return movimientoRepository.findByCuenta_NumeroCuenta(numeroCuenta);
    }

    @Override
    public Movimiento findById(Long id) {
        return movimientoRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Movimiento no encontrado"));
    }

    @Override
    public void delete(Long id) {
        movimientoRepository.deleteById(id);
    }

    @Override
    public Movimiento update(String numeroCuenta, Movimiento movimiento) {
        // Lógica para actualizar un movimiento
        return null;
    }
}
