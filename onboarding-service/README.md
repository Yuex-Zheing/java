# Onboarding Service

Microservicio de orquestación para el onboarding completo de clientes en el sistema bancario.

## Descripción

Este servicio se encarga de orquestar la creación completa de un cliente mediante la coordinación de los servicios `personas-clientes-service` y `cuentas-movimientos-service`.

## Funcionalidades

- **Orquestación completa**: Maneja el flujo completo de creación de cliente
- **Comunicación con microservicios**: Utiliza WebClient para comunicación reactiva
- **Números de cuenta especiales**: Genera cuentas con prefijo 99 para identificar onboarding
- **Validaciones robustas**: Validación completa de datos de entrada
- **Manejo de errores**: Gestión integral de errores y rollback

## Flujo de Onboarding

1. **Crear Persona**: Registro en personas-clientes-service
2. **Crear Cliente**: Asociación de cliente a la persona creada
3. **Crear Cuenta**: Generación de cuenta con número especial (99XXXX)
4. **Movimiento Inicial**: Si saldo inicial > 0, crea depósito automático

## Configuración

### Puerto
- **Puerto por defecto**: 8080

### Servicios Externos
- **Personas-Clientes**: http://localhost:8081/api
- **Cuentas-Movimientos**: http://localhost:8082/api

## API Endpoints

### POST /api/onboarding
Crea un cliente completo con persona, cliente, cuenta y movimiento inicial.

**Request Body:**
```json
{
  "persona": {
    "identificacionpersona": "0919395186",
    "nombres": "William Quimis",
    "genero": "M",
    "edad": 35,
    "direccion": "Av. Principal 123",
    "telefono": "0991234567"
  },
  "cliente": {
    "nombreUsuario": "wquimis",
    "contrasena": "Pass123*"
  },
  "cuenta": {
    "tipoCuenta": "AHORROS",
    "saldoInicial": 100.00
  }
}
```

**Response:**
```json
{
  "personaId": 1,
  "personaNombres": "William Quimis",
  "personaIdentificacion": "0919395186",
  "clienteId": 1,
  "clienteNombreUsuario": "wquimis",
  "numeroCuenta": 991234,
  "tipoCuenta": "AHORROS",
  "saldoDisponible": "100.00",
  "movimientoId": 1,
  "movimientoDescripcion": "Depósito inicial por onboarding",
  "mensaje": "Onboarding completado exitosamente",
  "fechaCreacion": "2025-09-08T15:30:00"
}
```

### GET /api/health
Verificar estado del servicio.

## Validaciones

### Persona
- Identificación: 10-13 caracteres
- Nombres: Máximo 100 caracteres
- Género: M o F
- Edad: 18-120 años
- Teléfono: 10 dígitos

### Cliente
- Nombre usuario: 4-20 caracteres alfanuméricos
- Contraseña: 8-20 caracteres con mayúscula, minúscula, número y carácter especial

### Cuenta
- Tipo: AHORROS o CORRIENTE
- Saldo inicial: No negativo

## Características Especiales

### Números de Cuenta Onboarding
- **Prefijo**: 99
- **Formato**: 99XXXX (6 dígitos máximo)
- **Rango**: 991000 - 999999

### Manejo de Errores
- Rollback automático en caso de falla
- Códigos de error específicos
- Logging detallado para auditoría

## Ejecución

```bash
cd onboarding-service
mvn spring-boot:run
```

## Documentación API
Una vez iniciado el servicio, la documentación Swagger estará disponible en:
- http://localhost:8080/swagger-ui.html
