package com.wquimis.demo.banking.services;

import com.wquimis.demo.banking.entities.Cuenta;
import java.util.List;

public interface CuentaService {
    List<Cuenta> findAll();
    Cuenta findByNumeroCuenta(Integer numeroCuenta);
    List<Cuenta> findByClienteId(Long clienteId);
    List<Cuenta> findByIdentificacionPersona(String identificacionPersona);
    Cuenta save(Cuenta cuenta);
    Cuenta update(Integer numeroCuenta, Cuenta cuenta);
    void delete(Integer numeroCuenta);
}
