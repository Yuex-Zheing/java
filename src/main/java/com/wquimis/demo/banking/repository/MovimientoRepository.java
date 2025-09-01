package com.wquimis.demo.banking.repository;

import com.wquimis.demo.banking.entities.Movimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface MovimientoRepository extends JpaRepository<Movimiento, Long> {
    List<Movimiento> findByCuentaNumerocuenta(Integer numeroCuenta);
    List<Movimiento> findByCuentaNumerocuentaAndFechamovimientoBetween(Integer numeroCuenta, LocalDate fechaInicio, LocalDate fechaFin);
}
