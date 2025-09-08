package com.wquimis.demo.cuentasmovimientos.repository;

import com.wquimis.demo.cuentasmovimientos.entities.Cuenta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CuentaRepository extends JpaRepository<Cuenta, Integer> {
    List<Cuenta> findByTipocuenta(Cuenta.TipoCuenta tipoCuenta);
    List<Cuenta> findByEstado(Boolean estado);
    List<Cuenta> findByIdcliente(Long idCliente);
    Optional<Cuenta> findTopByOrderByNumerocuentaDesc();
}
