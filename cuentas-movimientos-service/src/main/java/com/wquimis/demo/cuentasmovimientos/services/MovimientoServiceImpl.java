package com.wquimis.demo.cuentasmovimientos.services;

import com.wquimis.demo.cuentasmovimientos.dto.MovimientoDTO;
import com.wquimis.demo.cuentasmovimientos.entities.Cuenta;
import com.wquimis.demo.cuentasmovimientos.entities.Movimiento;
import com.wquimis.demo.cuentasmovimientos.exceptions.SaldoNoDisponibleException;
import com.wquimis.demo.cuentasmovimientos.repository.MovimientoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
@Transactional
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
        return movimientoRepository.findByCuentaNumerocuentaOrderByFechamovimientoDescHoramovimientoDesc(numeroCuenta);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Movimiento> findByNumeroCuentaAndFechaBetween(Integer numeroCuenta, LocalDate fechaInicio, LocalDate fechaFin) {
        return movimientoRepository.findByCuentaNumerocuentaAndFechamovimientoBetweenOrderByFechamovimientoDescHoramovimientoDesc(
                numeroCuenta, fechaInicio, fechaFin);
    }

    @Override
    public Movimiento save(Movimiento movimiento) {
        return movimientoRepository.save(movimiento);
    }

    @Override
    public Movimiento update(Long id, MovimientoDTO movimientoDTO) {
        Movimiento movimiento = findById(id);
        
        // Solo permitir actualizar la descripción
        if (movimientoDTO.getMovimientodescripcion() != null) {
            movimiento.setMovimientodescripcion(movimientoDTO.getMovimientodescripcion());
        }
        
        return movimientoRepository.save(movimiento);
    }

    @Override
    public void deleteById(Long id) {
        Movimiento movimiento = findById(id);
        
        // Revertir el movimiento en la cuenta
        Cuenta cuenta = movimiento.getCuenta();
        if (movimiento.getTipomovimiento() == Movimiento.TipoMovimiento.DEPOSITO) {
            // Si fue un depósito, debitar el monto
            cuenta.debitar(movimiento.getMontomovimiento());
        } else {
            // Si fue un retiro, acreditar el monto
            cuenta.acreditar(movimiento.getMontomovimiento());
        }
        
        // Guardar la cuenta actualizada
        cuentaService.save(cuenta);
        
        // Eliminar el movimiento
        movimientoRepository.deleteById(id);
    }

    @Override
    public Movimiento realizarMovimiento(Movimiento movimiento) {
        Cuenta cuenta = movimiento.getCuenta();
        BigDecimal monto = movimiento.getMontomovimiento();

        // Validar saldo disponible para retiros
        if (movimiento.getTipomovimiento() == Movimiento.TipoMovimiento.RETIRO) {
            if (!cuenta.tieneSaldoSuficiente(monto)) {
                throw new SaldoNoDisponibleException(
                    "Saldo no disponible. Saldo actual: " + cuenta.getSaldodisponible() + 
                    ", Monto solicitado: " + monto);
            }
            // Debitar de la cuenta
            cuenta.debitar(monto);
            // El monto se guarda como negativo para retiros
            movimiento.setMontomovimiento(monto.negate());
        } else {
            // Acreditar a la cuenta
            cuenta.acreditar(monto);
            // El monto se guarda como positivo para depósitos
            movimiento.setMontomovimiento(monto);
        }

        // Establecer fechas y hora actuales
        movimiento.setFechamovimiento(LocalDate.now());
        movimiento.setHoramovimiento(LocalTime.now());
        
        // Establecer el saldo disponible después del movimiento
        movimiento.setSaldodisponible(cuenta.getSaldodisponible());

        // Guardar la cuenta actualizada
        cuentaService.save(cuenta);

        // Guardar y retornar el movimiento
        return movimientoRepository.save(movimiento);
    }
}
