package com.wquimis.demo.banking.controller;

import com.wquimis.demo.banking.dto.CuentaDTO;
import com.wquimis.demo.banking.dto.MovimientoDTO;
import com.wquimis.demo.banking.entities.Cuenta;
import com.wquimis.demo.banking.entities.Movimiento;
import com.wquimis.demo.banking.services.CuentaService;
import com.wquimis.demo.banking.services.MovimientoService;
import com.wquimis.demo.banking.services.ClienteService;
import com.wquimis.demo.banking.utils.DtoConverter;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class CuentaMovimientoController {

    @Autowired
    private CuentaService cuentaService;

    @Autowired
    private MovimientoService movimientoService;

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private DtoConverter dtoConverter;

    // Endpoints para Cuentas
    @GetMapping("/cuentas")
    public ResponseEntity<List<CuentaDTO>> getAllCuentas() {
        List<CuentaDTO> cuentas = cuentaService.findAll().stream()
            .map(dtoConverter::toDto)
            .collect(Collectors.toList());
        return ResponseEntity.ok(cuentas);
    }

    @GetMapping("/cuentas/{numeroCuenta}")
    public ResponseEntity<CuentaDTO> getCuentaByNumero(@PathVariable Integer numeroCuenta) {
        return ResponseEntity.ok(dtoConverter.toDto(cuentaService.findByNumeroCuenta(numeroCuenta)));
    }

    @GetMapping("/cuentas/cliente/{clienteId}")
    public ResponseEntity<List<CuentaDTO>> getCuentasByCliente(@PathVariable Long clienteId) {
        List<CuentaDTO> cuentas = cuentaService.findByClienteId(clienteId).stream()
            .map(dtoConverter::toDto)
            .collect(Collectors.toList());
        return ResponseEntity.ok(cuentas);
    }

    @PostMapping("/cuentas")
    public ResponseEntity<CuentaDTO> createCuenta(@Valid @RequestBody CuentaDTO cuentaDto) {
        var cliente = clienteService.findById(cuentaDto.getIdCliente());
        var cuenta = dtoConverter.toEntity(cuentaDto, cliente);
        return ResponseEntity.ok(dtoConverter.toDto(cuentaService.save(cuenta)));
    }

    @PutMapping("/cuentas/{numeroCuenta}")
    public ResponseEntity<CuentaDTO> updateCuenta(
            @PathVariable Integer numeroCuenta,
            @Valid @RequestBody CuentaDTO cuentaDto) {
        var cuentaExistente = cuentaService.findByNumeroCuenta(numeroCuenta);
        var cuenta = dtoConverter.toEntity(cuentaDto, cuentaExistente.getCliente());
        return ResponseEntity.ok(dtoConverter.toDto(cuentaService.update(numeroCuenta, cuenta)));
    }

    @DeleteMapping("/cuentas/{numeroCuenta}")
    public ResponseEntity<Void> deleteCuenta(@PathVariable Integer numeroCuenta) {
        cuentaService.delete(numeroCuenta);
        return ResponseEntity.ok().build();
    }

    // Endpoints para Movimientos
    @PostMapping("/movimientos")
    public ResponseEntity<MovimientoDTO> realizarMovimiento(@Valid @RequestBody MovimientoDTO movimientoDto) {
        var cuenta = cuentaService.findByNumeroCuenta(movimientoDto.getNumeroCuenta());
        var movimiento = dtoConverter.toEntity(movimientoDto, cuenta);
        return ResponseEntity.ok(dtoConverter.toDto(movimientoService.realizarMovimiento(movimiento)));
    }

    @GetMapping("/movimientos/cuenta/{numeroCuenta}")
    public ResponseEntity<List<MovimientoDTO>> getMovimientosByCuenta(@PathVariable Integer numeroCuenta) {
        List<MovimientoDTO> movimientos = movimientoService.findByNumeroCuenta(numeroCuenta).stream()
            .map(dtoConverter::toDto)
            .collect(Collectors.toList());
        return ResponseEntity.ok(movimientos);
    }

    // Endpoint para Reportes
    @GetMapping("/reportes")
    public ResponseEntity<Map<String, Object>> generarReporte(
            @RequestParam("identificacion") String identificacionCliente,
            @RequestParam("fechaInicio") @DateTimeFormat(pattern = "dd/MM/yyyy") LocalDate fechaInicio,
            @RequestParam("fechaFin") @DateTimeFormat(pattern = "dd/MM/yyyy") LocalDate fechaFin) {

        var cliente = clienteService.findByIdentificacionPersona(identificacionCliente);
        List<Cuenta> cuentas = cuentaService.findByIdentificacionPersona(identificacionCliente);

        Map<String, Object> reporte = new HashMap<>();
        reporte.put("fechaReporte", LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")));

        Map<String, Object> clienteInfo = new HashMap<>();
        clienteInfo.put("nombres", cliente.getPersona().getNombres());
        clienteInfo.put("identificacion", cliente.getPersona().getIdentificacionpersona());
        reporte.put("cliente", clienteInfo);

        var cuentasInfo = cuentas.stream().map(cuenta -> {
            Map<String, Object> cuentaInfo = new HashMap<>();
            cuentaInfo.put("numero", cuenta.getNumerocuenta());
            cuentaInfo.put("fechaCreacion", cuenta.getFechacreacion().toLocalDate().format(
                java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            cuentaInfo.put("tipoCuenta", cuenta.getTipocuenta());
            cuentaInfo.put("saldoInicial", cuenta.getSaldoinicial());
            cuentaInfo.put("saldoDisponible", cuenta.getSaldoinicial());
            cuentaInfo.put("estado", cuenta.getEstado() ? "ACTIVA" : "INACTIVA");

            var movimientos = movimientoService.findByNumeroCuentaAndFechaBetween(
                cuenta.getNumerocuenta(), fechaInicio, fechaFin);
            cuentaInfo.put("movimientos", movimientos);

            return cuentaInfo;
        reporte.put("cuentas", cuentasInfo);

        return ResponseEntity.ok(reporte);
    }
                movInfo.put("Tipo", mov.getTipomovimiento().toString());
                movInfo.put("Monto", mov.getMontomovimiento().abs().toString());

                movInfo.put("Descripcion", mov.getMovimientodescripcion());
