package com.wquimis.demo.banking.services;

import com.wquimis.demo.banking.dto.OnboardingRequestDTO;
import com.wquimis.demo.banking.entities.Cuenta;
import com.wquimis.demo.banking.exceptions.OnboardingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class OnboardingServiceIntegrationTest {

    @Autowired
    private OnboardingService onboardingService;

    private OnboardingRequestDTO request;

    @BeforeEach
    void setUp() {
        request = new OnboardingRequestDTO();
        
        // Configurar datos de persona
        OnboardingRequestDTO.PersonaOnboardingDTO persona = new OnboardingRequestDTO.PersonaOnboardingDTO();
        persona.setIdentificacionpersona("0919395186");
        persona.setNombres("William Quimis");
        persona.setGenero("M");
        persona.setEdad(35);
        persona.setDireccion("Av. Principal 123");
        persona.setTelefono("0991234567");
        request.setPersona(persona);

        // Configurar datos de cliente
        OnboardingRequestDTO.ClienteOnboardingDTO cliente = new OnboardingRequestDTO.ClienteOnboardingDTO();
        cliente.setNombreUsuario("wquimis");
        cliente.setContrasena("Pass123*");
        request.setCliente(cliente);

        // Configurar datos de cuenta
        OnboardingRequestDTO.CuentaOnboardingDTO cuenta = new OnboardingRequestDTO.CuentaOnboardingDTO();
        cuenta.setTipoCuenta(Cuenta.TipoCuenta.AHORROS);
        cuenta.setSaldoInicial(new BigDecimal("100.00"));
        request.setCuenta(cuenta);
    }

    @Test
    @Sql("/test-data.sql")
    void testProcesoOnboardingExitoso() {
        // Act
        Cuenta cuentaCreada = onboardingService.procesarOnboarding(request);

        // Assert
        assertNotNull(cuentaCreada, "La cuenta no debería ser null");
        assertTrue(cuentaCreada.getNumerocuenta() > 100001, "El número de cuenta debería ser mayor al último existente");
        assertEquals(Cuenta.TipoCuenta.AHORROS, cuentaCreada.getTipocuenta());
        assertEquals(0, new BigDecimal("100.00").compareTo(cuentaCreada.getSaldoinicial()));
        assertEquals(0, new BigDecimal("100.00").compareTo(cuentaCreada.getSaldodisponible()));
        assertTrue(cuentaCreada.getEstado());
    }

    @Test
    @Sql("/test-data.sql")
    void testValidacionDatosPersona() {
        // Arrange
        request.setPersona(null);

        // Act & Assert
        OnboardingException exception = assertThrows(OnboardingException.class, 
            () -> onboardingService.procesarOnboarding(request)
        );

        assertEquals("ERR_PERSONA", exception.getCodigo());
        assertTrue(exception.getMessage().contains("Los datos de la persona son requeridos"));
        assertTrue(exception.getMensajeNegocio().contains("Debe proporcionar los datos de la persona"));
    }

    @Test
    @Sql("/test-data.sql")
    void testValidacionDatosCliente() {
        // Arrange
        request.setCliente(null);

        // Act & Assert
        OnboardingException exception = assertThrows(OnboardingException.class, 
            () -> onboardingService.procesarOnboarding(request)
        );

        assertEquals("ERR_CLIENTE", exception.getCodigo());
        assertTrue(exception.getMessage().contains("Los datos del cliente son requeridos"));
        assertTrue(exception.getMensajeNegocio().contains("Debe proporcionar los datos del cliente"));
    }

    @Test
    @Sql("/test-data.sql")
    void testValidacionDatosCuenta() {
        // Arrange
        request.setCuenta(null);

        // Act & Assert
        OnboardingException exception = assertThrows(OnboardingException.class, 
            () -> onboardingService.procesarOnboarding(request)
        );

        assertEquals("ERR_CUENTA", exception.getCodigo());
        assertTrue(exception.getMessage().contains("Los datos de la cuenta son requeridos"));
        assertTrue(exception.getMensajeNegocio().contains("Debe proporcionar los datos de la cuenta"));
    }
}
