package com.wquimis.demo.cuentasmovimientos.services.impl;

import com.wquimis.demo.cuentasmovimientos.dto.MovimientoDTO;
import com.wquimis.demo.cuentasmovimientos.entities.Cuenta;
import com.wquimis.demo.cuentasmovimientos.entities.Movimiento;
import com.wquimis.demo.cuentasmovimientos.exceptions.SaldoNoDisponibleException;
import com.wquimis.demo.cuentasmovimientos.repository.MovimientoRepository;
import com.wquimis.demo.cuentasmovimientos.services.CuentaService;
import com.wquimis.demo.cuentasmovimientos.services.MovimientoService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Transactional
@Slf4j
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
        Movimiento movimientoOriginal = findById(id);
        
        // Verificar que el movimiento no haya sido ya revertido
        if (!movimientoOriginal.getEstado()) {
            throw new IllegalStateException("No se puede revertir un movimiento que ya ha sido revertido");
        }
        
        Cuenta cuenta = movimientoOriginal.getCuenta();
        
        // Crear el movimiento de reverso
        Movimiento movimientoReverso = new Movimiento();
        movimientoReverso.setCuenta(cuenta);
        movimientoReverso.setFechamovimiento(LocalDate.now());
        movimientoReverso.setHoramovimiento(LocalTime.now());
        movimientoReverso.setEstado(true); // El reverso es una operación correcta
        
        // Determinar el tipo de movimiento reverso y el monto
        BigDecimal montoReverso = movimientoOriginal.getMontomovimiento().abs(); // Usar valor absoluto
        
        if (movimientoOriginal.getTipomovimiento() == Movimiento.TipoMovimiento.DEPOSITO) {
            // Si el original fue un depósito, hacer un retiro de reverso
            movimientoReverso.setTipomovimiento(Movimiento.TipoMovimiento.RETIRO);
            movimientoReverso.setMovimientodescripcion(
                movimientoOriginal.getMovimientodescripcion() + " [REVERSO ID#" + movimientoOriginal.getIdmovimiento() + "]");
            
            // Verificar que hay saldo suficiente para el reverso
            if (!cuenta.tieneSaldoSuficiente(montoReverso)) {
                throw new SaldoNoDisponibleException(
                    "No hay saldo suficiente para revertir el depósito. Saldo actual: " + 
                    cuenta.getSaldodisponible() + ", Monto requerido: " + montoReverso);
            }
            
            // Debitar de la cuenta
            cuenta.debitar(montoReverso);
            movimientoReverso.setMontomovimiento(montoReverso);
            
        } else {
            // Si el original fue un retiro, hacer un depósito de reverso
            movimientoReverso.setTipomovimiento(Movimiento.TipoMovimiento.DEPOSITO);
            movimientoReverso.setMovimientodescripcion(
                movimientoOriginal.getMovimientodescripcion() + " [REVERSO ID#" + movimientoOriginal.getIdmovimiento() + "]");
            
            // Acreditar a la cuenta
            cuenta.acreditar(montoReverso);
            movimientoReverso.setMontomovimiento(montoReverso);
        }
        
        // Establecer el saldo disponible después del reverso
        movimientoReverso.setSaldodisponible(cuenta.getSaldodisponible());
        
        // Anular el movimiento original: cambiar descripción y marcar como reversado
        LocalDateTime fechaHoraAnulacion = LocalDateTime.now();
        String descripcionAnulacion = String.format("Operacion Anulada %s.%03d", 
            fechaHoraAnulacion.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
            fechaHoraAnulacion.getNano() / 1_000_000); // Convertir nanosegundos a milisegundos
        
        movimientoOriginal.setMovimientodescripcion(descripcionAnulacion);
        movimientoOriginal.setEstado(false); // Marcado como reversado (esReverso = true en DTO)
        
        // El movimiento de reverso tiene estado = true (esReverso = false en DTO) ya que es una operación nueva y correcta
        
        // Guardar los cambios
        cuentaService.saveOrUpdate(cuenta);
        movimientoRepository.save(movimientoOriginal); // Actualizar el estado y descripción del original
        movimientoRepository.save(movimientoReverso);   // Guardar el movimiento de reverso
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
        } else { // DEPÓSITO
            boolean esDepositoInicial = false;
            // Detectar posible depósito inicial (patrón de onboarding)
            if (movimiento.getMovimientodescripcion() != null &&
                movimiento.getMovimientodescripcion().toLowerCase().contains("depósito inicial") ) {
                // Si el saldo disponible ya es igual al saldo inicial + monto (o ya refleja el monto)
                // entonces probablemente el saldo ya fue seteado al crear la cuenta
                BigDecimal saldoAntes = cuenta.getSaldodisponible();
                BigDecimal saldoInicial = cuenta.getSaldoinicial();
                if (saldoAntes != null && saldoInicial != null) {
                    // Caso A: prePersist ya igualó saldodisponible = saldoinicial y el movimiento repite ese monto
                    if (saldoAntes.compareTo(saldoInicial) == 0 && monto.compareTo(saldoInicial) == 0) {
                        esDepositoInicial = true;
                        log.info("[MOVIMIENTOS][SKIP_DEPOSITO_INICIAL] Evitando doble acreditación. Cuenta {} saldoActual {} monto {}", cuenta.getNumerocuenta(), saldoAntes, monto);
                    }
                    // Caso B: saldo disponible ya incluye el monto (posible reintento) -> saldoAntes - monto == saldoInicial
                    else if (saldoAntes.subtract(monto).compareTo(saldoInicial) == 0) {
                        esDepositoInicial = true;
                        log.warn("[MOVIMIENTOS][REINTENTO_DEPOSITO_INICIAL] Detectado reintento de depósito inicial. Cuenta {} saldoActual {} monto {}", cuenta.getNumerocuenta(), saldoAntes, monto);
                    }
                }
            }
            if (!esDepositoInicial) {
                cuenta.acreditar(monto);
            }
        }

        // Establecer fechas y hora actuales
        movimiento.setFechamovimiento(LocalDate.now());
        movimiento.setHoramovimiento(LocalTime.now());
        
    // Establecer el saldo disponible después del movimiento (o sin cambios si se saltó)
    movimiento.setSaldodisponible(cuenta.getSaldodisponible());

        // Guardar la cuenta actualizada
        cuentaService.saveOrUpdate(cuenta);

        // Guardar y retornar el movimiento
        return movimientoRepository.save(movimiento);
    }
}
