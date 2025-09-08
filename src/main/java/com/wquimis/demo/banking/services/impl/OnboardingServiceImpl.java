package com.wquimis.demo.banking.services.impl;

import com.wquimis.demo.banking.dto.OnboardingRequestDTO;
import com.wquimis.demo.banking.entities.Cliente;
import com.wquimis.demo.banking.entities.Cuenta;
import com.wquimis.demo.banking.entities.Persona;
import com.wquimis.demo.banking.exceptions.OnboardingException;
import com.wquimis.demo.banking.repository.CuentaRepository;
import com.wquimis.demo.banking.services.ClienteService;
import com.wquimis.demo.banking.services.CuentaService;
import com.wquimis.demo.banking.services.OnboardingService;
import com.wquimis.demo.banking.services.PersonaService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class OnboardingServiceImpl implements OnboardingService {

    private final PersonaService personaService;
    private final ClienteService clienteService;
    private final CuentaService cuentaService;
    private final CuentaRepository cuentaRepository;

    public OnboardingServiceImpl(PersonaService personaService, ClienteService clienteService, 
                               CuentaService cuentaService, CuentaRepository cuentaRepository) {
        this.personaService = personaService;
        this.clienteService = clienteService;
        this.cuentaService = cuentaService;
        this.cuentaRepository = cuentaRepository;
    }

    @Override
    @Transactional
    public Cuenta procesarOnboarding(OnboardingRequestDTO request) throws OnboardingException {
        // Validaciones iniciales
        if (request == null) {
            throw new OnboardingException("ERR_ONB", "Solicitud de onboarding nula", "La solicitud no puede ser nula");
        }

        if (request.getPersona() == null) {
            throw new OnboardingException("ERR_PERSONA", "Los datos de la persona son requeridos", "Debe proporcionar los datos de la persona");
        }
        if (request.getCliente() == null) {
            throw new OnboardingException("ERR_CLIENTE", "Los datos del cliente son requeridos", "Debe proporcionar los datos del cliente");
        }
        if (request.getCuenta() == null) {
            throw new OnboardingException("ERR_CUENTA", "Los datos de la cuenta son requeridos", "Debe proporcionar los datos de la cuenta");
        }

        try {

            // 1. Crear Persona
            Persona persona = new Persona();
            persona.setIdentificacionpersona(request.getPersona().getIdentificacionpersona());
            persona.setNombres(request.getPersona().getNombres());
            persona.setGenero(request.getPersona().getGenero());
            persona.setEdad(request.getPersona().getEdad());
            persona.setDireccion(request.getPersona().getDireccion());
            persona.setTelefono(request.getPersona().getTelefono());
            persona.setEstado(true);

            try {
                persona = personaService.save(persona);
            } catch (Exception e) {
                throw new OnboardingException("PERSONA", "ERR_004", "Error al crear la persona: " + e.getMessage());
            }

            // 2. Crear Cliente
            Cliente cliente = new Cliente();
            cliente.setPersona(persona);
            cliente.setNombreusuario(request.getCliente().getNombreUsuario());
            cliente.setContrasena(request.getCliente().getContrasena());
            cliente.setEstado(true);
            
            try {
                cliente = clienteService.save(cliente);
            } catch (Exception e) {
                throw new OnboardingException("CLIENTE", "ERR_005", "Error al crear el cliente: " + e.getMessage());
            }

            // 3. Generar número de cuenta
            Integer ultimoNumeroCuenta;
            try {
                ultimoNumeroCuenta = cuentaRepository.findTopByOrderByNumerocuentaDesc()
                    .map(Cuenta::getNumerocuenta)
                    .orElse(100000);
            } catch (Exception e) {
                throw new OnboardingException("CUENTA", "ERR_006", "Error al obtener el último número de cuenta: " + e.getMessage());
            }
            Integer nuevoNumeroCuenta = ultimoNumeroCuenta + 1;

            // 4. Crear Cuenta
            Cuenta cuenta = new Cuenta();
            cuenta.setNumerocuenta(nuevoNumeroCuenta);
            cuenta.setCliente(cliente);
            cuenta.setTipocuenta(request.getCuenta().getTipoCuenta()); // El DTO ya usa el enum correcto
            cuenta.setSaldoinicial(request.getCuenta().getSaldoInicial());
            cuenta.setSaldodisponible(request.getCuenta().getSaldoInicial());
            cuenta.setEstado(true);
            cuenta.setFechacreacion(LocalDateTime.now());

            try {
                return cuentaService.save(cuenta);
            } catch (Exception e) {
                throw new OnboardingException("CUENTA", "ERR_007", "Error al crear la cuenta: " + e.getMessage());
            }
        } catch (Exception e) {
            throw new OnboardingException(
                "ERR_ONB",
                e.getMessage(),
                "Error en el proceso de onboarding: " + e.getMessage()
            );
        }
    }
}
