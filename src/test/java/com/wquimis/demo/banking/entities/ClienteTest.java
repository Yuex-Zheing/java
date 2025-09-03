package com.wquimis.demo.banking.entities;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.*;

class ClienteTest {

    private Cliente cliente;
    private Persona persona;

    @BeforeEach
    void setUp() {
        // Configurar persona
        persona = new Persona();
        persona.setIdpersona(1L);
        persona.setIdentificacionpersona("0919395186");
        persona.setNombres("William Quimis");
        persona.setGenero("M");
        persona.setEdad(35);
        persona.setDireccion("Av. Principal 123");
        persona.setTelefono("0991234567");
        persona.setEstado(true);

        // Configurar cliente
        cliente = new Cliente();
        cliente.setIdcliente(1L);
        cliente.setPersona(persona);
        cliente.setNombreusuario("wquimis");
        cliente.setContrasena("Pass123*");
        cliente.setEstado(true);
        cliente.setCuentas(new ArrayList<>());
    }

    @Test
    void testClienteCreation() {
        // Assert basic properties
        assertNotNull(cliente, "El cliente no debería ser null");
        assertEquals(1L, cliente.getIdcliente(), "El ID del cliente debería ser 1");
        assertEquals("wquimis", cliente.getNombreusuario(), "El nombre de usuario debería ser wquimis");
        assertEquals("Pass123*", cliente.getContrasena(), "La contraseña debería coincidir");
        assertTrue(cliente.getEstado(), "El estado debería ser true");
        assertNotNull(cliente.getCuentas(), "La lista de cuentas no debería ser null");
        assertTrue(cliente.getCuentas().isEmpty(), "La lista de cuentas debería estar vacía inicialmente");
    }

    @Test
    void testRelacionPersona() {
        // Assert relationship with Persona
        assertNotNull(cliente.getPersona(), "La persona asociada no debería ser null");
        assertEquals(1L, cliente.getPersona().getIdpersona(), "El ID de la persona debería ser 1");
        assertEquals("0919395186", cliente.getPersona().getIdentificacionpersona(), "La identificación debería coincidir");
        assertEquals("William Quimis", cliente.getPersona().getNombres(), "El nombre debería coincidir");
        assertEquals("M", cliente.getPersona().getGenero(), "El género debería coincidir");
        assertTrue(cliente.getPersona().getEstado(), "El estado de la persona debería ser true");
    }

    @Test
    void testModificacionCliente() {
        // Act
        cliente.setNombreusuario("wquimis2");
        cliente.setContrasena("NewPass123*");
        cliente.setEstado(false);

        // Assert
        assertEquals("wquimis2", cliente.getNombreusuario(), "El nuevo nombre de usuario debería coincidir");
        assertEquals("NewPass123*", cliente.getContrasena(), "La nueva contraseña debería coincidir");
        assertFalse(cliente.getEstado(), "El nuevo estado debería ser false");
    }

    @Test
    void testAgregarCuenta() {
        // Arrange
        Cuenta cuenta = new Cuenta();
        cuenta.setNumerocuenta(100001);
        cuenta.setCliente(cliente);

        // Act
        cliente.getCuentas().add(cuenta);

        // Assert
        assertEquals(1, cliente.getCuentas().size(), "El cliente debería tener una cuenta");
        assertEquals(100001, cliente.getCuentas().get(0).getNumerocuenta(), "El número de cuenta debería coincidir");
        assertEquals(cliente, cliente.getCuentas().get(0).getCliente(), "La referencia al cliente debería ser la misma");
    }
}
