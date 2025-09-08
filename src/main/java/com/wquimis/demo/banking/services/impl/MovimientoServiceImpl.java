package com.wquimis.demo.banking.services.impl;

import com.wquimis.demo.banking.entities.Movimiento;
import com.wquimis.demo.banking.entities.Cuenta;
import com.wquimis.demo.banking.exceptions.SaldoNoDisponibleException;
import com.wquimis.demo.banking.repository.MovimientoRepository;
import com.wquimis.demo.banking.services.MovimientoService;
import com.wquimis.demo.banking.services.CuentaService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class MovimientoServiceImpl implements MovimientoService {

    private final MovimientoRepository movimientoRepository;
    private final CuentaService cuentaService;

    public MovimientoServiceImpl(MovimientoRepository movimientoRepository, CuentaService cuentaService) {
        this.movimientoRepository = movimientoRepository;
        this.cuentaService = cuentaService;
    }

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
        // Obtener el movimiento original
        Movimiento movimientoOriginal = findById(id);
        if (!movimientoOriginal.getEstado()) {
            throw new IllegalStateException("El movimiento ya está anulado");
        }

        Cuenta cuenta = movimientoOriginal.getCuenta();
        BigDecimal saldoDisponible = cuenta.getSaldodisponible();
        
        // Crear un nuevo movimiento de reverso
        Movimiento movimientoReverso = new Movimiento();
        movimientoReverso.setCuenta(cuenta);
        movimientoReverso.setEstado(true);
        movimientoReverso.setFechamovimiento(LocalDate.now());
        movimientoReverso.setHoramovimiento(LocalTime.now());
        
        // Configurar el tipo y monto del reverso
        if (movimientoOriginal.getTipomovimiento() == Movimiento.TipoMovimiento.RETIRO) {
            // Si era un retiro, creamos un depósito por el mismo monto
            movimientoReverso.setTipomovimiento(Movimiento.TipoMovimiento.DEPOSITO);
            movimientoReverso.setMontomovimiento(movimientoOriginal.getMontomovimiento().abs());
            saldoDisponible = saldoDisponible.add(movimientoOriginal.getMontomovimiento().abs());
            movimientoReverso.setMovimientodescripcion("Reverso de retiro - Movimiento #" + movimientoOriginal.getIdmovimiento());
        } else {
            // Si era un depósito, creamos un retiro por el mismo monto
            movimientoReverso.setTipomovimiento(Movimiento.TipoMovimiento.RETIRO);
            movimientoReverso.setMontomovimiento(movimientoOriginal.getMontomovimiento().negate());
            saldoDisponible = saldoDisponible.subtract(movimientoOriginal.getMontomovimiento());
            movimientoReverso.setMovimientodescripcion("Reverso de depósito - Movimiento #" + movimientoOriginal.getIdmovimiento());
        }
        
        // Actualizar saldos
        movimientoReverso.setSaldodisponible(saldoDisponible);
        cuenta.setSaldodisponible(saldoDisponible);
        
        // Marcar el movimiento original como anulado
        movimientoOriginal.setEstado(false);
        movimientoOriginal.setMovimientodescripcion(movimientoOriginal.getMovimientodescripcion() + " [ANULADO]");
        
        // Guardar los cambios
        movimientoRepository.save(movimientoOriginal);
        movimientoRepository.save(movimientoReverso);
        cuentaService.update(cuenta.getNumerocuenta(), cuenta);
    }
}
