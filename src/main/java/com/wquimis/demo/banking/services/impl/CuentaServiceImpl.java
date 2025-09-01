package com.wquimis.demo.banking.services.impl;

import com.wquimis.demo.banking.entities.Cuenta;
import com.wquimis.demo.banking.repository.CuentaRepository;
import com.wquimis.demo.banking.services.CuentaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class CuentaServiceImpl implements CuentaService {

    @Autowired
    private CuentaRepository cuentaRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Cuenta> findAll() {
        return cuentaRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Cuenta findByNumeroCuenta(Integer numeroCuenta) {
        return cuentaRepository.findById(numeroCuenta)
            .orElseThrow(() -> new EntityNotFoundException("Cuenta no encontrada con número: " + numeroCuenta));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cuenta> findByClienteId(Long clienteId) {
        return cuentaRepository.findByClienteIdcliente(clienteId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cuenta> findByIdentificacionPersona(String identificacionPersona) {
        return cuentaRepository.findByClientePersonaIdentificacionpersona(identificacionPersona);
    }

    @Override
    @Transactional
    public Cuenta save(Cuenta cuenta) {
        if (cuenta.getEstado() == null) {
            cuenta.setEstado(true);
        }
        if (cuenta.getFechacreacion() == null) {
            cuenta.setFechacreacion(LocalDateTime.now());
        }
        return cuentaRepository.save(cuenta);
    }

    @Override
    @Transactional
    public Cuenta update(Integer numeroCuenta, Cuenta cuenta) {
        Cuenta existingCuenta = findByNumeroCuenta(numeroCuenta);
        existingCuenta.setTipocuenta(cuenta.getTipocuenta());
        existingCuenta.setSaldoinicial(cuenta.getSaldoinicial());
        existingCuenta.setEstado(cuenta.getEstado());
        if (!existingCuenta.getEstado()) {
            existingCuenta.setFechacierre(LocalDateTime.now());
        }
        return cuentaRepository.save(existingCuenta);
    }

    @Override
    @Transactional
    public void delete(Integer numeroCuenta) {
        Cuenta cuenta = findByNumeroCuenta(numeroCuenta);
        cuenta.setEstado(false);
        cuenta.setFechacierre(LocalDateTime.now());
        cuentaRepository.save(cuenta);
    }
}
