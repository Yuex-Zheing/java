package com.wquimis.demo.banking.controller;

import com.wquimis.demo.banking.dto.CuentaDTO;
import com.wquimis.demo.banking.dto.ErrorDTO;
import com.wquimis.demo.banking.dto.OnboardingRequestDTO;
import com.wquimis.demo.banking.entities.Cuenta;
import com.wquimis.demo.banking.exceptions.OnboardingException;
import com.wquimis.demo.banking.services.OnboardingService;
import com.wquimis.demo.banking.utils.DtoConverter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/onboarding")
@Tag(name = "Onboarding", description = "API para el proceso de onboarding de nuevos clientes")
public class OnboardingController {

    private final OnboardingService onboardingService;
    private final DtoConverter dtoConverter;

    public OnboardingController(OnboardingService onboardingService, DtoConverter dtoConverter) {
        this.onboardingService = onboardingService;
        this.dtoConverter = dtoConverter;
    }

    @Operation(summary = "Crear nuevo cliente bancario", description = "Crea una nueva persona con su respectivo cliente y cuenta bancaria")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Cliente creado exitosamente",
                    content = @Content(schema = @Schema(implementation = CuentaDTO.class))),
        @ApiResponse(responseCode = "400", description = "Datos inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorDTO.class))),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content(schema = @Schema(implementation = ErrorDTO.class)))
    })
    @PostMapping("/banking")
    public ResponseEntity<?> crearClienteBancario(@RequestBody OnboardingRequestDTO request) {
        try {
            Cuenta cuenta = onboardingService.procesarOnboarding(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                               .body(dtoConverter.toDto(cuenta));
        } catch (OnboardingException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                               .body(ErrorDTO.builder()
                                           .codigo(e.getCodigo())
                                           .mensajeTecnico(e.getMessage())
                                           .mensajeNegocio(e.getMensajeNegocio())
                                           .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                               .body(ErrorDTO.builder()
                                           .codigo("ERR_500")
                                           .mensajeTecnico(e.getMessage())
                                           .mensajeNegocio("Error interno del servidor")
                                           .build());
        }
    }
}
