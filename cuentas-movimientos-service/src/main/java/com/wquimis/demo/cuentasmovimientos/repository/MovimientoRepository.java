package com.wquimis.demo.cuentasmovimientos.repository;

import com.wquimis.demo.cuentasmovimientos.entities.Movimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface MovimientoRepository extends JpaRepository<Movimiento, Long> {
    
    @Query("SELECT m FROM Movimiento m WHERE m.cuenta.numerocuenta = :numeroCuenta ORDER BY m.fechamovimiento DESC, m.horamovimiento DESC")
    List<Movimiento> findByNumeroCuentaOrderByFechaDesc(@Param("numeroCuenta") Integer numeroCuenta);
    
    @Query("SELECT m FROM Movimiento m WHERE m.cuenta.numerocuenta = :numeroCuenta AND m.fechamovimiento BETWEEN :fechaInicio AND :fechaFin ORDER BY m.fechamovimiento DESC, m.horamovimiento DESC")
    List<Movimiento> findByNumeroCuentaAndFechaBetweenOrderByFechaDesc(
        @Param("numeroCuenta") Integer numeroCuenta, 
        @Param("fechaInicio") LocalDate fechaInicio, 
        @Param("fechaFin") LocalDate fechaFin);
        
    List<Movimiento> findByCuentaNumerocuenta(Integer numeroCuenta);
    List<Movimiento> findByCuentaNumerocuentaAndFechamovimientoBetween(Integer numeroCuenta, LocalDate fechaInicio, LocalDate fechaFin);
    
    // Métodos con ordenamiento específico
    List<Movimiento> findByCuentaNumerocuentaOrderByFechamovimientoDescHoramovimientoDesc(Integer numeroCuenta);
    List<Movimiento> findByCuentaNumerocuentaAndFechamovimientoBetweenOrderByFechamovimientoDescHoramovimientoDesc(
        Integer numeroCuenta, LocalDate fechaInicio, LocalDate fechaFin);
}
