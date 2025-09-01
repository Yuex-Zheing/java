package com.wquimis.demo.banking.controller;

import com.wquimis.demo.banking.dto.CuentaDTO;
import com.wquimis.demo.banking.dto.MovimientoDTO;
import com.wquimis.demo.banking.entities.Cuenta;
import com.wquimis.demo.banking.entities.Movimiento;
import com.wquimis.demo.banking.services.CuentaService;
import com.wquimis.demo.banking.services.MovimientoService;
import com.wquimis.demo.banking.services.ClienteService;
import com.wquimis.demo.banking.utils.DtoConverter;

import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Gestión de Cuentas y Movimientos", description = "APIs para gestionar cuentas y movimientos")

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
        reporte.put("FechaReporte", LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")));

        Map<String, String> clienteInfo = new HashMap<>();
        clienteInfo.put("nombres", cliente.getPersona().getNombres());
        clienteInfo.put("identificacion", cliente.getPersona().getIdentificacionpersona());
        reporte.put("Cliente", clienteInfo);

        List<Map<String, Object>> cuentasInfo = cuentas.stream().map(cuenta -> {
            Map<String, Object> cuentaInfo = new HashMap<>();
            cuentaInfo.put("Numero", cuenta.getNumerocuenta().toString());
            cuentaInfo.put("FechaCreacion", cuenta.getFechacreacion().toLocalDate().format(
                java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            cuentaInfo.put("TipoCuenta", cuenta.getTipocuenta().toString());
            cuentaInfo.put("SaldoInicial", cuenta.getSaldoinicial().toString());
            cuentaInfo.put("Estado", cuenta.getEstado() ? "ACTIVA" : "INACTIVA");

            var movimientos = movimientoService.findByNumeroCuentaAndFechaBetween(
                cuenta.getNumerocuenta(), fechaInicio, fechaFin);

            List<Map<String, Object>> movimientosInfo = movimientos.stream().map(mov -> {
                Map<String, Object> movInfo = new HashMap<>();
                movInfo.put("id", mov.getIdmovimiento());
                movInfo.put("Fecha", mov.getFechamovimiento().format(
                    java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                movInfo.put("Hora", mov.getHoramovimiento().format(
                    java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss.SSS")));
                movInfo.put("Tipo", mov.getTipomovimiento().toString());
                movInfo.put("Monto", mov.getMontomovimiento().abs().toString());
                movInfo.put("Saldo Disponible", mov.getSaldodisponible().toString());
                movInfo.put("Descripcion", mov.getMovimientodescripcion());
                return movInfo;
            }).toList();

            cuentaInfo.put("Movimientos", movimientosInfo);
            cuentaInfo.put("SaldoDisponible", cuenta.getSaldoinicial().toString());

            return cuentaInfo;
        }).toList();

        reporte.put("Cuentas", cuentasInfo);

        return ResponseEntity.ok(reporte);
    }
}
