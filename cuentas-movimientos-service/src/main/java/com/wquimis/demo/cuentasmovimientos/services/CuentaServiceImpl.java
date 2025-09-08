package com.wquimis.demo.cuentasmovimientos.services;

import com.wquimis.demo.cuentasmovimientos.entities.Cuenta;
import com.wquimis.demo.cuentasmovimientos.exceptions.CuentaExistenteException;
import com.wquimis.demo.cuentasmovimientos.repository.CuentaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class CuentaServiceImpl implements CuentaService {

    private final CuentaRepository cuentaRepository;

    public CuentaServiceImpl(CuentaRepository cuentaRepository) {
        this.cuentaRepository = cuentaRepository;
    }

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
    public List<Cuenta> findByTipoCuenta(Cuenta.TipoCuenta tipoCuenta) {
        return cuentaRepository.findByTipocuenta(tipoCuenta);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cuenta> findByEstado(Boolean estado) {
        return cuentaRepository.findByEstado(estado);
    }

    @Override
    @Transactional
    public Cuenta save(Cuenta cuenta) {
        // Verificar si ya existe una cuenta con el mismo número
        if (cuentaRepository.findById(cuenta.getNumerocuenta()).isPresent()) {
            throw new CuentaExistenteException("Ya existe una cuenta con el número: " + cuenta.getNumerocuenta());
        }

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
        // Permitir actualizar estado
        existingCuenta.setEstado(cuenta.getEstado());
        return cuentaRepository.save(existingCuenta);
    }

    @Override
    @Transactional
    public void delete(Integer numeroCuenta) {
        Cuenta cuenta = findByNumeroCuenta(numeroCuenta);
        cuenta.setEstado(false);
        cuentaRepository.save(cuenta);
    }
}
