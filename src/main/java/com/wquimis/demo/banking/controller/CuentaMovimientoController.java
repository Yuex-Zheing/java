package com.wquimis.demo.banking.controller;

import com.wquimis.demo.banking.dto.CuentaDTO;
import com.wquimis.demo.banking.dto.MovimientoDTO;
import com.wquimis.demo.banking.entities.Cuenta;
import com.wquimis.demo.banking.entities.Movimiento;
import com.wquimis.demo.banking.services.CuentaService;
import com.wquimis.demo.banking.services.MovimientoService;
import com.wquimis.demo.banking.services.ClienteService;
import com.wquimis.demo.banking.utils.DtoConverter;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
    @Operation(summary = "Obtener todas las cuentas")
    @GetMapping("/cuentas")
    public ResponseEntity<List<CuentaDTO>> getAllCuentas() {
        try {
            List<CuentaDTO> cuentas = cuentaService.findAll().stream()
                    .map(dtoConverter::toDto)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(cuentas);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "Obtener cuenta por número")
    @GetMapping("/cuentas/{numeroCuenta}")
    public ResponseEntity<CuentaDTO> getCuentaByNumero(@PathVariable Integer numeroCuenta) {
        return ResponseEntity.ok(dtoConverter.toDto(cuentaService.findByNumeroCuenta(numeroCuenta)));
    }

    @Operation(summary = "Obtener cuentas por cliente")
    @GetMapping("/cuentas/cliente/{clienteId}")
    public ResponseEntity<List<CuentaDTO>> getCuentasByCliente(@PathVariable Long clienteId) {
        List<CuentaDTO> cuentas = cuentaService.findByClienteId(clienteId).stream()
                .map(dtoConverter::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(cuentas);
    }

    @Operation(summary = "Crear nueva cuenta")
    @PostMapping("/cuentas")
    public ResponseEntity<CuentaDTO> createCuenta(@Valid @RequestBody CuentaDTO cuentaDto) {
        var cliente = clienteService.findById(cuentaDto.getIdCliente());
        var cuenta = dtoConverter.toEntity(cuentaDto, cliente);
        return ResponseEntity.ok(dtoConverter.toDto(cuentaService.save(cuenta)));
    }

    @Operation(summary = "Actualizar cuenta existente")
    @PutMapping("/cuentas/{numeroCuenta}")
    public ResponseEntity<CuentaDTO> updateCuenta(
            @PathVariable Integer numeroCuenta,
            @Valid @RequestBody CuentaDTO cuentaDto) {
        var cuentaExistente = cuentaService.findByNumeroCuenta(numeroCuenta);
        var cuenta = dtoConverter.toEntity(cuentaDto, cuentaExistente.getCliente());
        return ResponseEntity.ok(dtoConverter.toDto(cuentaService.update(numeroCuenta, cuenta)));
    }

    @Operation(summary = "Eliminar cuenta")
    @DeleteMapping("/cuentas/{numeroCuenta}")
    public ResponseEntity<Void> deleteCuenta(@PathVariable Integer numeroCuenta) {
        cuentaService.delete(numeroCuenta);
        return ResponseEntity.ok().build();
    }

    // Endpoints para Movimientos
    @Operation(summary = "Realizar nuevo movimiento")
    @PostMapping("/movimientos")
    public ResponseEntity<MovimientoDTO> realizarMovimiento(@Valid @RequestBody MovimientoDTO movimientoDto) {
        var cuenta = cuentaService.findByNumeroCuenta(movimientoDto.getNumeroCuenta());
        var movimiento = dtoConverter.toEntity(movimientoDto, cuenta);
        return ResponseEntity.ok(dtoConverter.toDto(movimientoService.realizarMovimiento(movimiento)));
    }

    @Operation(summary = "Obtener movimientos por cuenta")
    @GetMapping("/movimientos/cuenta/{numeroCuenta}")
    public ResponseEntity<List<MovimientoDTO>> getMovimientosByCuenta(@PathVariable Integer numeroCuenta) {
        try {
            List<MovimientoDTO> movimientos = movimientoService.findByNumeroCuenta(numeroCuenta).stream()
                    .map(dtoConverter::toDto)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(movimientos);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // Endpoint para Reportes
    @Operation(summary = "Generar reporte de movimientos")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Reporte generado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Cliente no encontrado")
    })
    @GetMapping("/reportes")
    public ResponseEntity<Map<String, Object>> generarReporte(
            @RequestParam("identificacion") String identificacionCliente,
            @RequestParam("fechaInicio") @DateTimeFormat(pattern = "dd/MM/yyyy") LocalDate fechaInicio,
            @RequestParam("fechaFin") @DateTimeFormat(pattern = "dd/MM/yyyy") LocalDate fechaFin) {

        // Obtener información del cliente consultado
        var cliente = clienteService.findByIdentificacionPersona(identificacionCliente);
        List<Cuenta> cuentas = cuentaService.findByIdentificacionPersona(identificacionCliente);

        Map<String, Object> reporte = new HashMap<>();
        
        // Fecha actual cuando se solicita el reporte
        reporte.put("FechaReporte", LocalDate.now().format(
            java.time.format.DateTimeFormatter.ofPattern("d/M/yyyy")));

        // Información del cliente consultado
        Map<String, String> clienteInfo = new HashMap<>();
        clienteInfo.put("nombres", cliente.getPersona().getNombres()); // nombres completos del cliente
        clienteInfo.put("identificacion", cliente.getPersona().getIdentificacionpersona()); // identificación del cliente
        reporte.put("Cliente", clienteInfo);

        // Procesar y ordenar todas las cuentas del cliente de forma descendente por fecha de creación
        List<Map<String, Object>> cuentasInfo = cuentas.stream()
            .sorted((c1, c2) -> c2.getFechacreacion().compareTo(c1.getFechacreacion()))
            .map(cuenta -> {
                Map<String, Object> cuentaInfo = new HashMap<>();
                
                cuentaInfo.put("Numero", cuenta.getNumerocuenta().toString()); // número de cuenta
                cuentaInfo.put("FechaCreacion", cuenta.getFechacreacion().toLocalDate().format(
                    java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"))); // fecha de creación
                cuentaInfo.put("TipoCuenta", cuenta.getTipocuenta().toString()); // tipo de cuenta
                cuentaInfo.put("SaldoInicial", cuenta.getSaldoinicial().toPlainString()); // saldo inicial
                cuentaInfo.put("SaldoDisponible", (cuenta.getSaldodisponible() != null ? 
                    cuenta.getSaldodisponible() : cuenta.getSaldoinicial()).toPlainString()); // saldo actual disponible
                cuentaInfo.put("Estado", cuenta.getEstado() ? "ACTIVA" : "INACTIVA"); // estado de la cuenta

                // Obtener y ordenar movimientos de forma descendente por fecha y hora
                var movimientos = movimientoService.findByNumeroCuentaAndFechaBetween(
                    cuenta.getNumerocuenta(), fechaInicio, fechaFin);

                List<Map<String, Object>> movimientosInfo = movimientos.stream()
                    .sorted((m1, m2) -> {
                        int compareDate = m2.getFechamovimiento().compareTo(m1.getFechamovimiento());
                        if (compareDate == 0) {
                            return m2.getHoramovimiento().compareTo(m1.getHoramovimiento());
                        }
                        return compareDate;
                    })
                    .map(mov -> {
                        Map<String, Object> movInfo = new HashMap<>();
                        movInfo.put("id", mov.getIdmovimiento());
                        movInfo.put("Monto", mov.getMontomovimiento().abs().toPlainString());
                        movInfo.put("Tipo", mov.getTipomovimiento().toString());
                        movInfo.put("Fecha", mov.getFechamovimiento().format(
                            java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                        movInfo.put("Hora", mov.getHoramovimiento().format(
                            java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss.SSSS")));
                        movInfo.put("Descripcion", mov.getMovimientodescripcion());
                        movInfo.put("Saldo Disponible", mov.getSaldodisponible().doubleValue()); // saldo disponible después del movimiento
                        return movInfo;
                    })
                    .toList();

                cuentaInfo.put("Movimientos", movimientosInfo);
                return cuentaInfo;
            })
            .toList();

        reporte.put("Cuentas", cuentasInfo);
        return ResponseEntity.ok(reporte);
    }
}
