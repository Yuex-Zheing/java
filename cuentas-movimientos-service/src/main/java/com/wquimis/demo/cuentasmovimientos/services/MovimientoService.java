package com.wquimis.demo.cuentasmovimientos.services;

import com.wquimis.demo.cuentasmovimientos.entities.Movimiento;
import com.wquimis.demo.cuentasmovimientos.dto.MovimientoDTO;
import java.time.LocalDate;
import java.util.List;

public interface MovimientoService {
    List<Movimiento> findAll();
    Movimiento findById(Long id);
    List<Movimiento> findByNumeroCuenta(Integer numeroCuenta);
    List<Movimiento> findByNumeroCuentaAndFechaBetween(Integer numeroCuenta, LocalDate fechaInicio, LocalDate fechaFin);
    Movimiento save(Movimiento movimiento);
    Movimiento update(Long id, MovimientoDTO movimientoDTO);
    void deleteById(Long id);
    Movimiento realizarMovimiento(Movimiento movimiento);
}
