package com.wquimis.demo.banking.controller;

import com.wquimis.demo.banking.dto.CuentaDTO;
import com.wquimis.demo.banking.dto.MovimientoDTO;
import com.wquimis.demo.banking.entities.Cliente;
import com.wquimis.demo.banking.entities.Cuenta;
import com.wquimis.demo.banking.entities.Movimiento;
import com.wquimis.demo.banking.services.ClienteService;
import com.wquimis.demo.banking.services.CuentaService;
import com.wquimis.demo.banking.services.MovimientoService;
import com.wquimis.demo.banking.utils.DtoConverter;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@Validated
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
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Movimiento realizado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos del movimiento inválidos"),
        @ApiResponse(responseCode = "404", description = "Cuenta no encontrada"),
        @ApiResponse(responseCode = "422", description = "Saldo insuficiente para realizar el retiro")
    })
    @PostMapping("/movimientos/{numeroCuenta}")
    public ResponseEntity<MovimientoDTO> realizarMovimiento(
            @PathVariable @NotNull(message = "El número de cuenta es requerido") Integer numeroCuenta,
            @Valid @RequestBody MovimientoDTO movimientoDto) {
        // Verificar que la cuenta existe
        var cuenta = cuentaService.findByNumeroCuenta(numeroCuenta);
        
        // Validar que la cuenta esté activa
        if (!cuenta.getEstado()) {
            throw new IllegalStateException("La cuenta se encuentra inactiva");
        }
        
        // Preparar el movimiento
        movimientoDto.setNumeroCuenta(numeroCuenta);
        var movimiento = dtoConverter.toEntity(movimientoDto, cuenta);
        
        // Realizar el movimiento y obtener el resultado
        var movimientoRealizado = movimientoService.realizarMovimiento(movimiento);
        return ResponseEntity.ok(dtoConverter.toDto(movimientoRealizado));
    }

    @Operation(summary = "Eliminar movimiento")
    @DeleteMapping("/movimientos/{idMovimiento}")
    public ResponseEntity<Void> deleteMovimiento(@PathVariable Long idMovimiento) {
        movimientoService.delete(idMovimiento);
        return ResponseEntity.ok().build();
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

    private static final java.time.format.DateTimeFormatter DATE_FORMATTER = 
        java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final java.time.format.DateTimeFormatter DATE_REPORT_FORMATTER = 
        java.time.format.DateTimeFormatter.ofPattern("d/M/yyyy");
    private static final java.time.format.DateTimeFormatter TIME_FORMATTER = 
        java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss.SSSS");

    /**
     * Genera el reporte de movimientos de las cuentas del cliente
     */
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

        var cliente = clienteService.findByIdentificacionPersona(identificacionCliente);
        List<Cuenta> cuentas = cuentaService.findByIdentificacionPersona(identificacionCliente);

        Map<String, Object> reporte = new LinkedHashMap<>();
        reporte.put("FechaReporte", LocalDate.now().format(DATE_REPORT_FORMATTER));
        reporte.put("Cliente", createClienteInfo(cliente));
        reporte.put("Cuentas", createCuentasInfo(cuentas, fechaInicio, fechaFin));

        return ResponseEntity.ok(reporte);
    }

    /**
     * Crea el mapa de información del cliente
     */
    private Map<String, String> createClienteInfo(Cliente cliente) {
        Map<String, String> clienteInfo = new LinkedHashMap<>();
        clienteInfo.put("nombres", cliente.getPersona().getNombres());
        clienteInfo.put("identificacion", cliente.getPersona().getIdentificacionpersona());
        return clienteInfo;
    }

    /**
     * Crea la lista de información de cuentas
     */
    private List<Map<String, Object>> createCuentasInfo(List<Cuenta> cuentas, LocalDate fechaInicio, LocalDate fechaFin) {
        return cuentas.stream()
            .sorted((c1, c2) -> c2.getFechacreacion().compareTo(c1.getFechacreacion()))
            .map(cuenta -> createCuentaInfo(cuenta, fechaInicio, fechaFin))
            .toList();
    }

    /**
     * Crea el mapa de información de una cuenta específica
     */
    private Map<String, Object> createCuentaInfo(Cuenta cuenta, LocalDate fechaInicio, LocalDate fechaFin) {
        Map<String, Object> cuentaInfo = new LinkedHashMap<>();
        
        cuentaInfo.put("SaldoInicial", cuenta.getSaldoinicial().toPlainString());
        cuentaInfo.put("Numero", cuenta.getNumerocuenta().toString());
        cuentaInfo.put("TipoCuenta", cuenta.getTipocuenta().toString());
        cuentaInfo.put("SaldoDisponible", (cuenta.getSaldodisponible() != null ? 
            cuenta.getSaldodisponible() : cuenta.getSaldoinicial()).toPlainString());
        cuentaInfo.put("Estado", cuenta.getEstado() ? "ACTIVA" : "INACTIVA");
        cuentaInfo.put("FechaCreacion", cuenta.getFechacreacion().toLocalDate().format(DATE_FORMATTER));
        cuentaInfo.put("Movimientos", createMovimientosInfo(cuenta, fechaInicio, fechaFin));
        
        return cuentaInfo;
    }

    /**
     * Crea la lista de información de movimientos de una cuenta
     */
    private List<Map<String, Object>> createMovimientosInfo(Cuenta cuenta, LocalDate fechaInicio, LocalDate fechaFin) {
        var movimientos = movimientoService.findByNumeroCuentaAndFechaBetween(
            cuenta.getNumerocuenta(), fechaInicio, fechaFin);

        return movimientos.stream()
            .sorted((m1, m2) -> {
                int compareDate = m2.getFechamovimiento().compareTo(m1.getFechamovimiento());
                return compareDate == 0 ? m2.getHoramovimiento().compareTo(m1.getHoramovimiento()) : compareDate;
            })
            .map(this::createMovimientoInfo)
            .toList();
    }

    /**
     * Crea el mapa de información de un movimiento específico
     */
    private Map<String, Object> createMovimientoInfo(Movimiento mov) {
        Map<String, Object> movInfo = new LinkedHashMap<>();
        
        movInfo.put("Fecha", mov.getFechamovimiento().format(DATE_FORMATTER));
        movInfo.put("Hora", mov.getHoramovimiento().format(TIME_FORMATTER));
        movInfo.put("Descripcion", mov.getMovimientodescripcion());
        movInfo.put("Tipo", mov.getTipomovimiento().toString());
        movInfo.put("Monto", mov.getMontomovimiento().abs().toPlainString());
        movInfo.put("Saldo Disponible", mov.getSaldodisponible().doubleValue());
        movInfo.put("id", mov.getIdmovimiento());
        
        return movInfo;
    }
    }
