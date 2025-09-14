package com.wquimis.demo.onboarding.controller;

import com.wquimis.demo.common.dto.ErrorDTO;
import com.wquimis.demo.onboarding.dto.OnboardingRequestDTO;
import com.wquimis.demo.onboarding.dto.OnboardingResponseDTO;
import com.wquimis.demo.onboarding.exceptions.EntityAlreadyExistsException;
import com.wquimis.demo.onboarding.exceptions.ExternalServiceException;
import com.wquimis.demo.onboarding.exceptions.OnboardingException;
import com.wquimis.demo.onboarding.exceptions.ValidationException;
import com.wquimis.demo.onboarding.services.OnboardingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@Validated
@Slf4j
@Tag(name = "Onboarding", description = "API para onboarding completo de clientes")
public class OnboardingController {

    private final OnboardingService onboardingService;

    public OnboardingController(OnboardingService onboardingService) {
        this.onboardingService = onboardingService;
    }

    @Operation(
        summary = "Crear cliente completo",
        description = "Orquesta la creación completa de un cliente: " +
                     "1. Crea la persona en personas-clientes-service, " +
                     "2. Crea el cliente asociado a la persona, " +
                     "3. Crea la cuenta con número especial (prefijo 99) y saldo inicial automático. " +
                     "Las cuentas creadas por onboarding tendrán números con prefijo 99 (rango: 990001-999999). " +
                     "El depósito inicial se crea automáticamente por el servicio de cuentas."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cliente creado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
        @ApiResponse(responseCode = "409", description = "Conflicto - cliente ya existe"),
        @ApiResponse(responseCode = "502", description = "Error en servicios externos"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PostMapping("/onboarding")
    public ResponseEntity<?> crearClienteCompleto(@Valid @RequestBody OnboardingRequestDTO request) {
        try {
            log.info("Recibida solicitud de onboarding para: {}", request.getPersona().getNombres());
            
            OnboardingResponseDTO response = onboardingService.procesarOnboarding(request);
            
            log.info("Onboarding completado exitosamente para cuenta: {}", response.getNumeroCuenta());
            return ResponseEntity.ok(response);
            
        } catch (EntityAlreadyExistsException e) {
            log.warn("Entidad ya existe durante onboarding: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorDTO.of("ERR_CONFLICT_001",
                    e.getMessage(),
                    String.format("La %s especificada ya existe en el sistema", e.getEntityType())));
                    
        } catch (ValidationException e) {
            log.warn("Error de validación durante onboarding: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorDTO.of("ERR_VALIDATION_001",
                    e.getMessage(),
                    "Los datos proporcionados no son válidos"));
                    
        } catch (ExternalServiceException e) {
            log.error("Error en servicio externo durante onboarding: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(ErrorDTO.of("ERR_EXT_001",
                    "Error en servicio externo: " + e.getMessage(),
                    "Ha ocurrido un error al comunicarse con los servicios externos"));
                    
        } catch (OnboardingException e) {
            log.error("Error durante proceso de onboarding: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorDTO.of("ERR_ONB_001",
                    e.getMessage(),
                    "Ha ocurrido un error durante el proceso de onboarding"));
                    
        } catch (Exception e) {
            log.error("Error inesperado durante onboarding: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorDTO.of("ERR_999",
                    e.getMessage(),
                    "Ha ocurrido un error inesperado"));
        }
    }

    @Operation(summary = "Verificar estado del servicio")
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Onboarding Service is UP");
    }
}
