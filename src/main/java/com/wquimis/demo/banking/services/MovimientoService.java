package com.wquimis.demo.banking.services;

import com.wquimis.demo.banking.entities.Movimiento;
import java.time.LocalDate;
import java.util.List;

public interface MovimientoService {
    List<Movimiento> findAll();
    Movimiento findById(Long id);
    List<Movimiento> findByNumeroCuenta(Integer numeroCuenta);
    List<Movimiento> findByNumeroCuentaAndFechaBetween(Integer numeroCuenta, LocalDate fechaInicio, LocalDate fechaFin);
    Movimiento realizarMovimiento(Movimiento movimiento);
    void delete(Long id);
}
