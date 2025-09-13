package com.wquimis.demo.onboarding.services.impl;

import com.wquimis.demo.onboarding.config.ExternalServicesConfig;
import com.wquimis.demo.onboarding.dto.*;
import com.wquimis.demo.onboarding.exceptions.EntityAlreadyExistsException;
import com.wquimis.demo.onboarding.exceptions.ExternalServiceException;
import com.wquimis.demo.onboarding.exceptions.OnboardingException;
import com.wquimis.demo.onboarding.exceptions.ValidationException;
import com.wquimis.demo.onboarding.services.OnboardingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Random;

@Service
@Slf4j
@RequiredArgsConstructor
public class OnboardingServiceImpl implements OnboardingService {

    private final WebClient.Builder webClientBuilder;
    private final ExternalServicesConfig externalServicesConfig;
    private final Random random = new Random();

    @Override
    public OnboardingResponseDTO procesarOnboarding(OnboardingRequestDTO request) {
        log.info("Iniciando proceso de onboarding para: {}", request.getPersona().getNombres());
        
        try {
            // Validaciones iniciales
            validarDatosIniciales(request);
            
            // Paso 1: Verificar si la persona ya existe, si no, crearla
            PersonaDTO personaCreada = obtenerOCrearPersona(request.getPersona());
            log.info("Persona procesada con ID: {}", personaCreada.getId());

            // Paso 2: Verificar si el cliente ya existe, si no, crearlo
            ClienteResponseDTO clienteCreado = obtenerOCrearCliente(request.getCliente(), personaCreada.getId());
            log.info("Cliente procesado con ID: {}", clienteCreado.getId());

            // Paso 3: Verificar si ya tiene una cuenta activa, si no, crear una nueva
            CuentaDTO cuentaCreada = obtenerOCrearCuenta(request.getCuenta(), clienteCreado.getId());
            log.info("Cuenta procesada con número: {}", cuentaCreada.getNumeroCuenta());

            // Paso 4: Crear movimiento de depósito inicial si el saldo es mayor a 0
            if (cuentaCreada.getSaldoInicial() != null && cuentaCreada.getSaldoInicial().compareTo(BigDecimal.ZERO) > 0) {
                crearMovimientoInicialDeposito(cuentaCreada.getNumeroCuenta(), cuentaCreada.getSaldoInicial());
                log.info("Movimiento de depósito inicial creado para cuenta: {} por monto: {}", 
                        cuentaCreada.getNumeroCuenta(), cuentaCreada.getSaldoInicial());
            } else {
                log.info("No se creó movimiento inicial - saldo inicial es 0 o no definido para cuenta: {}", 
                        cuentaCreada.getNumeroCuenta());
            }

            // Construir respuesta
            OnboardingResponseDTO response = construirRespuesta(personaCreada, clienteCreado, cuentaCreada);
            
            log.info("Onboarding completado exitosamente para cuenta: {} con saldo: {}", 
                     cuentaCreada.getNumeroCuenta(), 
                     cuentaCreada.getSaldoInicial() != null ? cuentaCreada.getSaldoInicial() : cuentaCreada.getSaldoDisponible());
            return response;
            
        } catch (EntityAlreadyExistsException | ValidationException e) {
            // Re-lanzar excepciones específicas sin modificar
            log.warn("Validación falló durante onboarding: {}", e.getMessage());
            throw e;
        } catch (WebClientResponseException e) {
            log.error("Error en comunicación con servicios externos: Status {}, Response: {}", 
                     e.getStatusCode(), e.getResponseBodyAsString(), e);
            
            // Intentar detectar errores de duplicado a nivel de base de datos
            String responseBody = e.getResponseBodyAsString();
            if (e.getStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR && 
                responseBody.contains("Duplicate entry")) {
                
                if (responseBody.contains("identificacionpersona")) {
                    throw new EntityAlreadyExistsException("persona", request.getPersona().getIdentificacionpersona(),
                        "La persona con esta identificación ya existe en el sistema");
                } else if (responseBody.contains("nombreusuario")) {
                    throw new EntityAlreadyExistsException("cliente", request.getCliente().getNombreUsuario(),
                        "El nombre de usuario ya está en uso");
                } else {
                    throw new EntityAlreadyExistsException("entidad", "desconocido",
                        "Ya existe una entidad con estos datos en el sistema");
                }
            }
            
            throw new ExternalServiceException("Error en servicio externo: " + parseErrorMessage(e), e);
        } catch (Exception e) {
            log.error("Error durante el proceso de onboarding: {}", e.getMessage(), e);
            throw new OnboardingException("Error durante el proceso de onboarding: " + e.getMessage(), e);
        }
    }

    private void validarDatosIniciales(OnboardingRequestDTO request) {
        // Validar identificación
        if (request.getPersona().getIdentificacionpersona() == null || 
            request.getPersona().getIdentificacionpersona().trim().isEmpty()) {
            throw new ValidationException("identificacionpersona", "null/empty", 
                "La identificación de la persona es requerida");
        }
        
        // Validar nombres
        if (request.getPersona().getNombres() == null || 
            request.getPersona().getNombres().trim().isEmpty()) {
            throw new ValidationException("nombres", "null/empty", 
                "Los nombres de la persona son requeridos");
        }
        
        // Validar nombre de usuario
        if (request.getCliente().getNombreUsuario() == null || 
            request.getCliente().getNombreUsuario().trim().isEmpty()) {
            throw new ValidationException("nombreUsuario", "null/empty", 
                "El nombre de usuario del cliente es requerido");
        }
        
        // Validar saldo inicial
        if (request.getCuenta().getSaldoInicial() == null || 
            request.getCuenta().getSaldoInicial().compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("saldoInicial", 
                request.getCuenta().getSaldoInicial() != null ? request.getCuenta().getSaldoInicial().toString() : "null", 
                "El saldo inicial debe ser mayor o igual a cero");
        }
    }

    private PersonaDTO obtenerOCrearPersona(PersonaRequestDTO personaRequest) {
        try {
            // Primero intentar obtener la persona por identificación
            PersonaDTO personaExistente = buscarPersonaPorIdentificacion(personaRequest.getIdentificacionpersona());
            
            if (personaExistente != null) {
                log.info("Persona ya existe con ID: {}, identificación: {}", 
                        personaExistente.getId(), personaExistente.getIdentificacionpersona());
                
                // Validar que los datos coincidan (nombres, género, etc.)
                validarCoincidenciaPersona(personaExistente, personaRequest);
                
                return personaExistente;
            }
            
            // Si no existe, crear nueva persona
            return crearPersona(personaRequest);
            
        } catch (EntityAlreadyExistsException | ValidationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error al procesar persona: {}", e.getMessage(), e);
            throw new OnboardingException("Error al procesar persona: " + e.getMessage(), e);
        }
    }

    private PersonaDTO buscarPersonaPorIdentificacion(String identificacion) {
        try {
            log.debug("Buscando persona con identificación: {}", identificacion);
            
            return webClientBuilder.build()
                    .get()
                    .uri(externalServicesConfig.getPersonasClientes().getPersonasUrl() + "/identificacion/" + identificacion)
                    .retrieve()
                    .bodyToMono(PersonaDTO.class)
                    .timeout(Duration.ofSeconds(30))
                    .block();
                    
        } catch (WebClientResponseException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                log.debug("Persona no encontrada con identificación: {}", identificacion);
                return null;
            }
            throw e;
        }
    }

    private void validarCoincidenciaPersona(PersonaDTO existente, PersonaRequestDTO nuevo) {
        // Validar que los nombres coincidan (permite variaciones menores)
        if (!normalizarTexto(existente.getNombres()).equals(normalizarTexto(nuevo.getNombres()))) {
            throw new ValidationException("nombres", nuevo.getNombres(),
                String.format("Los nombres no coinciden. Existente: '%s', Nuevo: '%s'", 
                             existente.getNombres(), nuevo.getNombres()));
        }
        
        // Validar género si está especificado
        if (nuevo.getGenero() != null && !nuevo.getGenero().equals(existente.getGenero())) {
            throw new ValidationException("genero", nuevo.getGenero(),
                String.format("El género no coincide. Existente: '%s', Nuevo: '%s'", 
                             existente.getGenero(), nuevo.getGenero()));
        }
        
        log.info("Validación de coincidencia de persona exitosa para identificación: {}", 
                existente.getIdentificacionpersona());
    }

    private String normalizarTexto(String texto) {
        return texto != null ? texto.trim().toLowerCase() : "";
    }

    private ClienteResponseDTO obtenerOCrearCliente(ClienteRequestDTO clienteRequest, Long personaId) {
        try {
            // Primero intentar obtener el cliente por persona ID
            ClienteResponseDTO clienteExistente = buscarClientePorPersonaId(personaId);
            
            if (clienteExistente != null) {
                log.info("Cliente ya existe con ID: {} para persona ID: {}", 
                        clienteExistente.getId(), personaId);
                
                // Validar que el nombre de usuario coincida
                if (!clienteExistente.getNombreUsuario().equals(clienteRequest.getNombreUsuario())) {
                    throw new EntityAlreadyExistsException("cliente", personaId.toString(),
                        String.format("Ya existe un cliente para esta persona con nombre de usuario '%s'. " +
                                     "No se puede crear otro con nombre '%s'", 
                                     clienteExistente.getNombreUsuario(), clienteRequest.getNombreUsuario()));
                }
                
                return clienteExistente;
            }
            
            // También verificar si el nombre de usuario ya está en uso
            ClienteResponseDTO clientePorNombre = buscarClientePorNombreUsuario(clienteRequest.getNombreUsuario());
            if (clientePorNombre != null) {
                throw new EntityAlreadyExistsException("cliente", clienteRequest.getNombreUsuario(),
                    String.format("El nombre de usuario '%s' ya está en uso por otro cliente", 
                                 clienteRequest.getNombreUsuario()));
            }
            
            // Si no existe, crear nuevo cliente
            return crearCliente(clienteRequest, personaId);
            
        } catch (EntityAlreadyExistsException | ValidationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error al procesar cliente: {}", e.getMessage(), e);
            throw new OnboardingException("Error al procesar cliente: " + e.getMessage(), e);
        }
    }

    private ClienteResponseDTO buscarClientePorPersonaId(Long personaId) {
        try {
            log.debug("Buscando cliente para persona ID: {}", personaId);
            
            return webClientBuilder.build()
                    .get()
                    .uri(externalServicesConfig.getPersonasClientes().getClientesUrl() + "/persona/" + personaId)
                    .retrieve()
                    .bodyToMono(ClienteResponseDTO.class)
                    .timeout(Duration.ofSeconds(30))
                    .block();
                    
        } catch (WebClientResponseException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                log.debug("Cliente no encontrado para persona ID: {}", personaId);
                return null;
            }
            throw e;
        }
    }

    private ClienteResponseDTO buscarClientePorNombreUsuario(String nombreUsuario) {
        try {
            log.debug("Buscando cliente con nombre de usuario: {}", nombreUsuario);
            
            return webClientBuilder.build()
                    .get()
                    .uri(externalServicesConfig.getPersonasClientes().getClientesUrl() + "/nombre-usuario/" + nombreUsuario)
                    .retrieve()
                    .bodyToMono(ClienteResponseDTO.class)
                    .timeout(Duration.ofSeconds(30))
                    .block();
                    
        } catch (WebClientResponseException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                log.debug("Cliente no encontrado con nombre de usuario: {}", nombreUsuario);
                return null;
            }
            throw e;
        }
    }

    private CuentaDTO obtenerOCrearCuenta(CuentaRequestDTO cuentaRequest, Long clienteId) {
        try {
            // Buscar cuentas existentes para el cliente
            CuentaDTO[] cuentasExistentes = buscarCuentasPorClienteId(clienteId);
            
            if (cuentasExistentes != null && cuentasExistentes.length > 0) {
                // Verificar si ya tiene una cuenta del mismo tipo
                for (CuentaDTO cuenta : cuentasExistentes) {
                    if (cuenta.getTipoCuenta().equals(cuentaRequest.getTipoCuenta())) {
                        log.info("Cliente ya tiene una cuenta del tipo '{}' con número: {}", 
                                cuenta.getTipoCuenta(), cuenta.getNumeroCuenta());
                        
                        throw new EntityAlreadyExistsException("cuenta", 
                            cuenta.getNumeroCuenta().toString(),
                            String.format("El cliente ya tiene una cuenta de tipo '%s' con número %d. " +
                                         "No se puede crear otra cuenta del mismo tipo.", 
                                         cuenta.getTipoCuenta(), cuenta.getNumeroCuenta()));
                    }
                }
            }
            
            // Si no tiene cuenta del tipo solicitado, crear una nueva
            Integer numeroCuenta = generarNumeroCuentaOnboarding();
            return crearCuenta(cuentaRequest, clienteId, numeroCuenta);
            
        } catch (EntityAlreadyExistsException | ValidationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error al procesar cuenta: {}", e.getMessage(), e);
            throw new OnboardingException("Error al procesar cuenta: " + e.getMessage(), e);
        }
    }

    private CuentaDTO[] buscarCuentasPorClienteId(Long clienteId) {
        try {
            log.debug("Buscando cuentas para cliente ID: {}", clienteId);
            
            return webClientBuilder.build()
                    .get()
                    .uri(externalServicesConfig.getCuentasMovimientos().getCuentasUrl() + "/cliente/" + clienteId)
                    .retrieve()
                    .bodyToMono(CuentaDTO[].class)
                    .timeout(Duration.ofSeconds(30))
                    .block();
                    
        } catch (WebClientResponseException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                log.debug("No se encontraron cuentas para cliente ID: {}", clienteId);
                return new CuentaDTO[0];
            }
            throw e;
        }
    }

    private OnboardingResponseDTO construirRespuesta(PersonaDTO persona, ClienteResponseDTO cliente, CuentaDTO cuenta) {
        OnboardingResponseDTO response = new OnboardingResponseDTO();
        response.setPersonaId(persona.getId());
        response.setPersonaNombres(persona.getNombres());
        response.setPersonaIdentificacion(persona.getIdentificacionpersona());
        
        response.setClienteId(cliente.getId());
        response.setClienteNombreUsuario(cliente.getNombreUsuario());
        
        response.setNumeroCuenta(cuenta.getNumeroCuenta());
        response.setTipoCuenta(cuenta.getTipoCuenta());
        response.setSaldoDisponible(cuenta.getSaldoDisponible() != null ? 
            cuenta.getSaldoDisponible().toString() : 
            cuenta.getSaldoInicial().toString());
        
        response.setMensaje("Onboarding completado exitosamente. Cliente configurado con cuenta activa y saldo disponible.");
        
        return response;
    }

    private String parseErrorMessage(WebClientResponseException e) {
        try {
            // Intentar extraer mensaje de error del cuerpo de respuesta
            String responseBody = e.getResponseBodyAsString();
            if (responseBody != null && !responseBody.isEmpty()) {
                // Aquí podrías parsear un JSON de error si los servicios lo devuelven
                return responseBody;
            }
        } catch (Exception ex) {
            log.debug("No se pudo parsear el mensaje de error: {}", ex.getMessage());
        }
        
        return "HTTP " + e.getStatusCode() + " - " + e.getStatusText();
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

            log.info("Creando nueva persona: {}", persona.getNombres());
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
                    
        } catch (WebClientResponseException e) {
            log.error("Error HTTP al crear persona: Status {}, Response: {}", 
                     e.getStatusCode(), e.getResponseBodyAsString());
            
            if (e.getStatusCode() == HttpStatus.CONFLICT) {
                throw new EntityAlreadyExistsException("persona", personaRequest.getIdentificacionpersona(),
                    "La persona con esta identificación ya existe");
            } else if (e.getStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR && 
                      e.getResponseBodyAsString().contains("Duplicate entry")) {
                // Manejo específico para errores de MySQL de clave duplicada
                throw new EntityAlreadyExistsException("persona", personaRequest.getIdentificacionpersona(),
                    "La persona con esta identificación ya existe en el sistema");
            }
            
            throw new ExternalServiceException("Error al crear persona: " + parseErrorMessage(e), e);
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
            
            log.info("Creando nuevo cliente para persona ID: {}", personaId);
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
            
        } catch (WebClientResponseException e) {
            log.error("Error HTTP al crear cliente: Status {}, Response: {}", 
                     e.getStatusCode(), e.getResponseBodyAsString());
            
            if (e.getStatusCode() == HttpStatus.CONFLICT) {
                throw new EntityAlreadyExistsException("cliente", clienteRequest.getNombreUsuario(),
                    "El cliente con este nombre de usuario ya existe");
            } else if (e.getStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR && 
                      e.getResponseBodyAsString().contains("Duplicate entry")) {
                // Manejo específico para errores de MySQL de clave duplicada
                throw new EntityAlreadyExistsException("cliente", clienteRequest.getNombreUsuario(),
                    "Ya existe un cliente para esta persona o el nombre de usuario está en uso");
            }
            
            throw new ExternalServiceException("Error al crear cliente: " + parseErrorMessage(e), e);
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
            
            log.info("Creando nueva cuenta para cliente ID: {} con número: {}", idCliente, numeroCuenta);
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
            
        } catch (WebClientResponseException e) {
            log.error("Error HTTP al crear cuenta: Status {}, Response: {}", 
                     e.getStatusCode(), e.getResponseBodyAsString());
            
            if (e.getStatusCode() == HttpStatus.CONFLICT) {
                throw new EntityAlreadyExistsException("cuenta", numeroCuenta.toString(),
                    "La cuenta con este número ya existe");
            } else if (e.getStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR && 
                      e.getResponseBodyAsString().contains("Duplicate entry")) {
                // Manejo específico para errores de MySQL de clave duplicada
                throw new EntityAlreadyExistsException("cuenta", numeroCuenta.toString(),
                    "La cuenta con este número ya existe en el sistema");
            }
            
            throw new ExternalServiceException("Error al crear cuenta: " + parseErrorMessage(e), e);
        } catch (Exception e) {
            log.error("Error al crear cuenta: {}", e.getMessage(), e);
            throw new OnboardingException("Error al crear cuenta: " + e.getMessage(), e);
        }
    }

    private void crearMovimientoInicialDeposito(Integer numeroCuenta, BigDecimal montoDeposito) {
        // Validaciones previas
        if (montoDeposito == null) {
            log.debug("[ONBOARDING][MOVIMIENTO_INICIAL] Monto nulo, no se crea movimiento inicial para cuenta {}", numeroCuenta);
            return;
        }
        if (montoDeposito.compareTo(BigDecimal.ZERO) <= 0) {
            log.info("[ONBOARDING][MOVIMIENTO_INICIAL] Saldo inicial <= 0 ({}), no se crea movimiento para cuenta {}", montoDeposito, numeroCuenta);
            return;
        }

        try {
            MovimientoDTO movimiento = new MovimientoDTO();
            movimiento.setTipomovimiento("DEPOSITO");
            movimiento.setMovimientodescripcion("Depósito inicial por onboarding");
            movimiento.setMontomovimiento(montoDeposito);
            movimiento.setEsReverso(false);

            String baseMovimientosUrl = externalServicesConfig.getCuentasMovimientos().getMovimientosUrl(); // termina en /api/movimientos
            String endpoint = baseMovimientosUrl + "/cuenta/" + numeroCuenta; // /api/movimientos/cuenta/{numeroCuenta}

            log.info("[ONBOARDING][MOVIMIENTO_INICIAL] Creando depósito inicial cuenta {} monto {} endpoint {}", numeroCuenta, montoDeposito, endpoint);
            log.debug("[ONBOARDING][MOVIMIENTO_INICIAL] Payload: {}", movimiento);

            MovimientoDTO response = webClientBuilder.build()
                .post()
                .uri(endpoint)
                .bodyValue(movimiento)
                .retrieve()
                .bodyToMono(MovimientoDTO.class)
                .timeout(Duration.ofSeconds(30))
                .block();

            if (response != null && response.getId() != null) {
                log.info("[ONBOARDING][MOVIMIENTO_INICIAL] Movimiento inicial creado ID {} para cuenta {}", response.getId(), numeroCuenta);
            } else {
                log.warn("[ONBOARDING][MOVIMIENTO_INICIAL] Respuesta sin ID al crear movimiento inicial para cuenta {}", numeroCuenta);
            }
        } catch (WebClientResponseException e) {
            log.error("[ONBOARDING][MOVIMIENTO_INICIAL] Error HTTP Status {} Body {}", e.getStatusCode(), e.getResponseBodyAsString());
            log.warn("[ONBOARDING][MOVIMIENTO_INICIAL] Falló creación de movimiento inicial (cuenta {}), proceso onboarding continúa", numeroCuenta);
        } catch (Exception e) {
            log.error("[ONBOARDING][MOVIMIENTO_INICIAL] Error inesperado {}", e.getMessage(), e);
            log.warn("[ONBOARDING][MOVIMIENTO_INICIAL] Falló creación de movimiento inicial (cuenta {}), proceso onboarding continúa", numeroCuenta);
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
