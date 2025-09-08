package com.wquimis.demo.cuentasmovimientos.controller;

import com.wquimis.demo.cuentasmovimientos.dto.CuentaDTO;
import com.wquimis.demo.cuentasmovimientos.dto.ErrorDTO;
import com.wquimis.demo.cuentasmovimientos.dto.MovimientoDTO;
import com.wquimis.demo.cuentasmovimientos.dto.UpdateCuentaDTO;
import com.wquimis.demo.cuentasmovimientos.exceptions.CuentaExistenteException;
import com.wquimis.demo.cuentasmovimientos.entities.Cuenta;
import com.wquimis.demo.cuentasmovimientos.entities.Movimiento;
import com.wquimis.demo.cuentasmovimientos.exceptions.SaldoNoDisponibleException;
import com.wquimis.demo.cuentasmovimientos.services.CuentaService;
import com.wquimis.demo.cuentasmovimientos.services.MovimientoService;
import com.wquimis.demo.cuentasmovimientos.utils.DtoConverter;
import org.springframework.http.HttpStatus;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@Validated
@Tag(name = "Gestión de Cuentas y Movimientos", description = "APIs para gestionar cuentas y movimientos de forma autónoma")
public class CuentaMovimientoController {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATE_REPORT_FORMATTER = DateTimeFormatter.ofPattern("d/M/yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final CuentaService cuentaService;
    private final MovimientoService movimientoService;
    private final DtoConverter dtoConverter;

    public CuentaMovimientoController(CuentaService cuentaService, MovimientoService movimientoService, 
                                     DtoConverter dtoConverter) {
        this.cuentaService = cuentaService;
        this.movimientoService = movimientoService;
        this.dtoConverter = dtoConverter;
    }

    // ===== ENDPOINTS PARA CUENTAS =====

    @Operation(summary = "Obtener todas las cuentas")
    @GetMapping("/cuentas")
    public ResponseEntity<?> getAllCuentas() {
        try {
            List<CuentaDTO> cuentas = cuentaService.findAll().stream()
                    .map(dtoConverter::toDto)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(cuentas);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorDTO.of("ERR_999", 
                    e.getMessage(),
                    "Ha ocurrido un error al obtener las cuentas"));
        }
    }

    @Operation(summary = "Obtener todas las cuentas activas")
    @GetMapping("/cuentas/activas")
    public ResponseEntity<?> getCuentasActivas() {
        try {
            List<CuentaDTO> cuentas = cuentaService.findByEstado(true).stream()
                    .map(dtoConverter::toDto)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(cuentas);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorDTO.of("ERR_999", 
                    e.getMessage(),
                    "Ha ocurrido un error al obtener las cuentas activas"));
        }
    }

    @Operation(summary = "Obtener cuenta por número")
    @GetMapping("/cuentas/{numeroCuenta}")
    public ResponseEntity<?> getCuentaByNumero(@PathVariable Integer numeroCuenta) {
        try {
            Cuenta cuenta = cuentaService.findByNumeroCuenta(numeroCuenta);
            return ResponseEntity.ok(dtoConverter.toDto(cuenta));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorDTO.of("ERR_001",
                    "Cuenta no encontrada con número: " + numeroCuenta,
                    "La cuenta solicitada no existe"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorDTO.of("ERR_999",
                    e.getMessage(),
                    "Ha ocurrido un error al obtener la cuenta"));
        }
    }

    @Operation(summary = "Obtener cuentas por tipo")
    @GetMapping("/cuentas/tipo/{tipoCuenta}")
    public ResponseEntity<?> getCuentasByTipo(@PathVariable String tipoCuenta) {
        try {
            Cuenta.TipoCuenta tipo = Cuenta.TipoCuenta.valueOf(tipoCuenta.toUpperCase());
            List<CuentaDTO> cuentas = cuentaService.findByTipoCuenta(tipo).stream()
                    .map(dtoConverter::toDto)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(cuentas);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorDTO.of("ERR_002",
                    "Tipo de cuenta inválido: " + tipoCuenta,
                    "Los tipos válidos son: AHORROS, CORRIENTE"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorDTO.of("ERR_999",
                    e.getMessage(),
                    "Ha ocurrido un error al obtener las cuentas por tipo"));
        }
    }

    @Operation(summary = "Crear nueva cuenta")
    @PostMapping("/cuentas")
    public ResponseEntity<?> createCuenta(@Valid @RequestBody CuentaDTO cuentaDto) {
        try {
            var cuenta = dtoConverter.toEntity(cuentaDto);
            return ResponseEntity.ok(dtoConverter.toDto(cuentaService.save(cuenta)));
        } catch (CuentaExistenteException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorDTO.of("ERR_008",
                    e.getMessage(),
                    "Ya existe una cuenta con el número proporcionado"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorDTO.of("ERR_999",
                    e.getMessage(),
                    "Ha ocurrido un error al crear la cuenta"));
        }
    }

    @Operation(summary = "Actualizar cuenta existente")
    @PutMapping("/cuentas/{numeroCuenta}")
    public ResponseEntity<?> updateCuenta(
            @PathVariable Integer numeroCuenta,
            @Valid @RequestBody UpdateCuentaDTO updateCuentaDto) {
        try {
            Cuenta cuentaExistente = cuentaService.findByNumeroCuenta(numeroCuenta);
            cuentaExistente.setEstado(updateCuentaDto.getEstado());
            return ResponseEntity.ok(dtoConverter.toDto(cuentaService.update(numeroCuenta, cuentaExistente)));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorDTO.of("ERR_001",
                    "Cuenta no encontrada con número: " + numeroCuenta,
                    "La cuenta que intenta actualizar no existe"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorDTO.of("ERR_999",
                    e.getMessage(),
                    "Ha ocurrido un error al actualizar la cuenta"));
        }
    }

    @Operation(summary = "Eliminar cuenta (desactivar)")
    @DeleteMapping("/cuentas/{numeroCuenta}")
    public ResponseEntity<?> deleteCuenta(@PathVariable Integer numeroCuenta) {
        try {
            cuentaService.delete(numeroCuenta);
            return ResponseEntity.ok(ErrorDTO.of("SUCCESS",
                "Cuenta eliminada correctamente",
                "La cuenta ha sido desactivada exitosamente"));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorDTO.of("ERR_001",
                    "Cuenta no encontrada con número: " + numeroCuenta,
                    "La cuenta que intenta eliminar no existe"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorDTO.of("ERR_999",
                    e.getMessage(),
                    "Ha ocurrido un error al eliminar la cuenta"));
        }
    }

    // ===== ENDPOINTS PARA MOVIMIENTOS =====

    @Operation(summary = "Realizar nuevo movimiento")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Movimiento realizado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos del movimiento inválidos"),
        @ApiResponse(responseCode = "404", description = "Cuenta no encontrada"),
        @ApiResponse(responseCode = "422", description = "Saldo insuficiente para realizar el retiro")
    })
    @PostMapping("/movimientos/cuenta/{numeroCuenta}")
    public ResponseEntity<?> realizarMovimiento(
            @PathVariable @NotNull(message = "El número de cuenta es requerido") Integer numeroCuenta,
            @Valid @RequestBody MovimientoDTO movimientoDto) {
        try {
            // Verificar que la cuenta existe
            var cuenta = cuentaService.findByNumeroCuenta(numeroCuenta);
            
            // Validar que la cuenta esté activa
            if (!cuenta.getEstado()) {
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .body(ErrorDTO.of("ERR_002",
                        "La cuenta se encuentra inactiva",
                        "No se pueden realizar movimientos en una cuenta inactiva"));
            }

            // Preparar el movimiento
            var movimiento = new Movimiento();
            movimiento.setCuenta(cuenta);
            movimiento.setTipomovimiento(Movimiento.TipoMovimiento.valueOf(movimientoDto.getTipomovimiento()));
            movimiento.setMontomovimiento(movimientoDto.getMontomovimiento());
            movimiento.setMovimientodescripcion(movimientoDto.getMovimientodescripcion());
            
            // Realizar el movimiento y obtener el resultado
            var movimientoRealizado = movimientoService.realizarMovimiento(movimiento);
            return ResponseEntity.ok(dtoConverter.toDto(movimientoRealizado));
        } catch (SaldoNoDisponibleException e) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ErrorDTO.of("ERR_003",
                    e.getMessage(),
                    "Saldo insuficiente para realizar el retiro"));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorDTO.of("ERR_001",
                    "Cuenta no encontrada con número: " + numeroCuenta,
                    "La cuenta especificada no existe"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorDTO.of("ERR_999",
                    e.getMessage(),
                    "Ha ocurrido un error al realizar el movimiento"));
        }
    }

    @Operation(summary = "Obtener todos los movimientos")
    @GetMapping("/movimientos")
    public ResponseEntity<?> getAllMovimientos() {
        try {
            List<MovimientoDTO> movimientos = movimientoService.findAll().stream()
                    .map(dtoConverter::toDto)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(movimientos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorDTO.of("ERR_999",
                    e.getMessage(),
                    "Ha ocurrido un error al obtener los movimientos"));
        }
    }

    @Operation(summary = "Obtener movimiento por ID")
    @GetMapping("/movimientos/{id}")
    public ResponseEntity<?> getMovimientoById(@PathVariable Long id) {
        try {
            Movimiento movimiento = movimientoService.findById(id);
            return ResponseEntity.ok(dtoConverter.toDto(movimiento));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorDTO.of("ERR_001",
                    "Movimiento no encontrado con ID: " + id,
                    "El movimiento solicitado no existe"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorDTO.of("ERR_999",
                    e.getMessage(),
                    "Ha ocurrido un error al obtener el movimiento"));
        }
    }

    @Operation(summary = "Obtener todos los movimientos por cuenta")
    @GetMapping("/movimientos/cuenta/{numeroCuenta}")
    public ResponseEntity<?> getMovimientosByCuenta(@PathVariable Integer numeroCuenta) {
        try {
            List<MovimientoDTO> movimientos = movimientoService.findByNumeroCuenta(numeroCuenta).stream()
                    .map(dtoConverter::toDto)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(movimientos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorDTO.of("ERR_999",
                    e.getMessage(),
                    "Ha ocurrido un error al obtener los movimientos"));
        }
    }

    @Operation(summary = "Obtener movimientos por cuenta y fechas")
    @GetMapping("/movimientos/cuenta/{numeroCuenta}/fechas")
    public ResponseEntity<?> getMovimientosByFecha(
            @PathVariable Integer numeroCuenta,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        try {
            List<MovimientoDTO> movimientos = movimientoService
                .findByNumeroCuentaAndFechaBetween(numeroCuenta, fechaInicio, fechaFin)
                .stream()
                .map(dtoConverter::toDto)
                .collect(Collectors.toList());
                
            return ResponseEntity.ok(movimientos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorDTO.of("ERR_999",
                    e.getMessage(),
                    "Ha ocurrido un error al obtener los movimientos"));
        }
    }

    @Operation(summary = "Actualizar movimiento")
    @PutMapping("/movimientos/{id}")
    public ResponseEntity<?> updateMovimiento(@PathVariable Long id, @Valid @RequestBody MovimientoDTO movimientoDto) {
        try {
            Movimiento movimiento = movimientoService.update(id, movimientoDto);
            return ResponseEntity.ok(dtoConverter.toDto(movimiento));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorDTO.of("ERR_001",
                    "Movimiento no encontrado con ID: " + id,
                    "El movimiento que intenta actualizar no existe"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorDTO.of("ERR_999",
                    e.getMessage(),
                    "Ha ocurrido un error al actualizar el movimiento"));
        }
    }

    @Operation(summary = "Eliminar movimiento")
    @DeleteMapping("/movimientos/{id}")
    public ResponseEntity<?> deleteMovimiento(@PathVariable Long id) {
        try {
            movimientoService.deleteById(id);
            return ResponseEntity.ok(ErrorDTO.of("SUCCESS",
                "Movimiento eliminado correctamente",
                "El movimiento ha sido anulado exitosamente"));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorDTO.of("ERR_001",
                    "Movimiento no encontrado con ID: " + id,
                    "El movimiento que intenta eliminar no existe"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorDTO.of("ERR_999",
                    e.getMessage(),
                    "Ha ocurrido un error al eliminar el movimiento"));
        }
    }

    // ===== ENDPOINTS PARA REPORTES =====

    @Operation(summary = "Generar reporte de cuenta")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Reporte generado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Cuenta no encontrada")
    })
    @GetMapping("/reportes/cuenta/{numeroCuenta}")
    public ResponseEntity<?> generarReporteCuenta(
            @PathVariable Integer numeroCuenta,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        try {
            // Obtener información de la cuenta
            Cuenta cuenta = cuentaService.findByNumeroCuenta(numeroCuenta);

            // Crear el reporte
            Map<String, Object> reporte = new LinkedHashMap<>();
            reporte.put("FechaReporte", LocalDate.now().format(DATE_REPORT_FORMATTER));
            reporte.put("Cuenta", createCuentaInfo(cuenta, fechaInicio, fechaFin));

            return ResponseEntity.ok(reporte);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorDTO.of("ERR_001",
                    "Cuenta no encontrada con número: " + numeroCuenta,
                    "La cuenta especificada no existe"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorDTO.of("ERR_999",
                    e.getMessage(),
                    "Ha ocurrido un error al generar el reporte"));
        }
    }

    // ===== MÉTODOS PRIVADOS PARA REPORTES =====

    /**
     * Crea el mapa de información de una cuenta específica
     */
    private Map<String, Object> createCuentaInfo(Cuenta cuenta, LocalDate fechaInicio, LocalDate fechaFin) {
        Map<String, Object> cuentaInfo = new LinkedHashMap<>();
        
        cuentaInfo.put("Numero", cuenta.getNumerocuenta().toString());
        cuentaInfo.put("TipoCuenta", cuenta.getTipocuenta().toString());
        cuentaInfo.put("SaldoInicial", cuenta.getSaldoinicial().toPlainString());
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
