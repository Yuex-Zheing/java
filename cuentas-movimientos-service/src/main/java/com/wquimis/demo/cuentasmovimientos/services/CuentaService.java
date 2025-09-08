package com.wquimis.demo.cuentasmovimientos.services;

import com.wquimis.demo.cuentasmovimientos.entities.Cuenta;
import java.util.List;

public interface CuentaService {
    List<Cuenta> findAll();
    Cuenta findByNumeroCuenta(Integer numeroCuenta);
    List<Cuenta> findByTipoCuenta(Cuenta.TipoCuenta tipoCuenta);
    List<Cuenta> findByEstado(Boolean estado);
    List<Cuenta> findByIdCliente(Long idCliente);
    Cuenta save(Cuenta cuenta);
    Cuenta saveOrUpdate(Cuenta cuenta);
    Cuenta update(Integer numeroCuenta, Cuenta cuenta);
    void delete(Integer numeroCuenta);
}
