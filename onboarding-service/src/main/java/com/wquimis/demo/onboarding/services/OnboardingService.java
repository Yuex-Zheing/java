package com.wquimis.demo.onboarding.services;

import com.wquimis.demo.onboarding.config.ExternalServicesConfig;
import com.wquimis.demo.onboarding.dto.*;
import com.wquimis.demo.onboarding.exceptions.OnboardingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Random;

@Service
@Slf4j
@RequiredArgsConstructor
public class OnboardingService {

    private final WebClient.Builder webClientBuilder;
    private final ExternalServicesConfig externalServicesConfig;
    private final Random random = new Random();

    public OnboardingResponseDTO procesarOnboarding(OnboardingRequestDTO request) {
        log.info("Iniciando proceso de onboarding para: {}", request.getPersona().getNombres());
        
        try {
            // Paso 1: Crear persona
            PersonaDTO personaCreada = crearPersona(request.getPersona());
            log.info("Persona creada con ID: {}", personaCreada.getId());

            // Paso 2: Crear cliente
            ClienteResponseDTO clienteCreado = crearCliente(request.getCliente(), personaCreada.getId());
            log.info("Cliente creado con ID: {}", clienteCreado.getId());

            // Paso 3: Crear cuenta con número especial (prefijo 99)
            Integer numeroCuenta = generarNumeroCuentaOnboarding();
            CuentaDTO cuentaCreada = crearCuenta(request.getCuenta(), clienteCreado.getId(), numeroCuenta);
            log.info("Cuenta creada con número: {}", cuentaCreada.getNumeroCuenta());

            // Paso 4: NO crear movimiento inicial - el servicio de cuentas ya lo hace automáticamente
            // cuando el saldo inicial es > 0

            // Construir respuesta
            OnboardingResponseDTO response = new OnboardingResponseDTO();
            response.setPersonaId(personaCreada.getId());
            response.setPersonaNombres(personaCreada.getNombres());
            response.setPersonaIdentificacion(personaCreada.getIdentificacionpersona());
            
            response.setClienteId(clienteCreado.getId());
            response.setClienteNombreUsuario(clienteCreado.getNombreUsuario());
            
            response.setNumeroCuenta(cuentaCreada.getNumeroCuenta());
            response.setTipoCuenta(cuentaCreada.getTipoCuenta());
            response.setSaldoDisponible(cuentaCreada.getSaldoDisponible() != null ? 
                cuentaCreada.getSaldoDisponible().toString() : 
                cuentaCreada.getSaldoInicial().toString());
            
            response.setMensaje("Onboarding completado exitosamente. La cuenta ya incluye el depósito inicial automáticamente.");
            
            log.info("Onboarding completado exitosamente para cuenta: {}", numeroCuenta);
            return response;
            
        } catch (Exception e) {
            log.error("Error durante el proceso de onboarding: {}", e.getMessage(), e);
            throw new OnboardingException("Error durante el proceso de onboarding: " + e.getMessage(), e);
        }
    }

    private PersonaDTO crearPersona(PersonaRequestDTO personaRequest) {
        try {
            PersonaDTO persona = new PersonaDTO();
            persona.setIdentificacionpersona(personaRequest.getIdentificacionpersona());
            persona.setNombres(personaRequest.getNombres());
            persona.setGenero(personaRequest.getGenero());
            persona.setEdad(personaRequest.getEdad());
            persona.setDireccion(personaRequest.getDireccion());
            persona.setTelefono(personaRequest.getTelefono());
            persona.setEstado(true);

            log.info("Creando persona: {}", persona.getNombres());
            log.debug("Persona request: {}", persona);

            PersonaDTO response = webClientBuilder.build()
                    .post()
                    .uri(externalServicesConfig.getPersonasClientes().getPersonasUrl())
                    .bodyValue(persona)
                    .retrieve()
                    .bodyToMono(PersonaDTO.class)
                    .timeout(Duration.ofSeconds(30))
                    .block();
                    
            log.info("Persona creada exitosamente con ID: {}", response.getId());
            return response;
                    
        } catch (Exception e) {
            log.error("Error al crear persona: {}", e.getMessage(), e);
            throw new OnboardingException("Error al crear persona: " + e.getMessage(), e);
        }
    }

    private ClienteResponseDTO crearCliente(ClienteRequestDTO clienteRequest, Long personaId) {
        try {
            // Crear el DTO para enviar al servicio
            ClienteDTO clienteDto = new ClienteDTO();
            clienteDto.setPersonaId(personaId);
            clienteDto.setNombreUsuario(clienteRequest.getNombreUsuario());
            clienteDto.setContrasena(clienteRequest.getContrasena());
            
            log.info("Creando cliente para persona ID: {}", personaId);
            log.debug("Cliente request: {}", clienteDto);
            
            ClienteResponseDTO response = webClientBuilder.build()
                .post()
                .uri(externalServicesConfig.getPersonasClientes().getClientesUrl())
                .bodyValue(clienteDto)
                .retrieve()
                .bodyToMono(ClienteResponseDTO.class)
                .timeout(Duration.ofSeconds(30))
                .block();
                
            log.info("Cliente creado exitosamente con ID: {}", response.getId());
            return response;
            
        } catch (Exception e) {
            log.error("Error al crear cliente: {}", e.getMessage(), e);
            throw new OnboardingException("Error al crear cliente: " + e.getMessage(), e);
        }
    }

    private CuentaDTO crearCuenta(CuentaRequestDTO cuentaRequest, Long idCliente, Integer numeroCuenta) {
        try {
            // Crear el DTO para enviar al servicio
            CuentaDTO cuentaDto = new CuentaDTO();
            cuentaDto.setNumeroCuenta(numeroCuenta);
            cuentaDto.setIdCliente(idCliente);
            cuentaDto.setTipoCuenta(cuentaRequest.getTipoCuenta());
            cuentaDto.setSaldoInicial(cuentaRequest.getSaldoInicial());
            
            log.info("Creando cuenta para cliente ID: {} con número: {}", idCliente, numeroCuenta);
            log.debug("Cuenta request: {}", cuentaDto);
            
            CuentaDTO response = webClientBuilder.build()
                .post()
                .uri(externalServicesConfig.getCuentasMovimientos().getCuentasUrl())
                .bodyValue(cuentaDto)
                .retrieve()
                .bodyToMono(CuentaDTO.class)
                .timeout(Duration.ofSeconds(30))
                .block();
                
            log.info("Cuenta creada exitosamente con número: {}", response.getNumeroCuenta());
            return response;
            
        } catch (Exception e) {
            log.error("Error al crear cuenta: {}", e.getMessage(), e);
            throw new OnboardingException("Error al crear cuenta: " + e.getMessage(), e);
        }
    }

    /**
     * Genera un número de cuenta para onboarding con prefijo 99
     * y máximo 6 dígitos (99XXXX)
     */
    private Integer generarNumeroCuentaOnboarding() {
        // Generar número aleatorio de 4 dígitos (1000-9999)
        int sufijo = random.nextInt(9000) + 1000;
        // Concatenar con prefijo 99
        return Integer.parseInt("99" + sufijo);
    }
}
