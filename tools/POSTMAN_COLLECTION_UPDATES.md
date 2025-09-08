# Banking API - Postman Collection Actualizada

## 🆕 **Nuevos Endpoints - Onboarding Service (Puerto 8080)**

### **Endpoint Principal: Onboarding Completo**

```
POST http://{{host}}:8080/api/onboarding
```

### **Funcionalidad**
Orquesta la creación completa de un cliente en una sola llamada:

1. **Crea Persona** → `personas-clientes-service:8081/api/personas`
2. **Crea Cliente** → `personas-clientes-service:8081/api/clientes`
3. **Crea Cuenta** → `cuentas-movimientos-service:8082/api/cuentas`
4. **Depósito Automático** → El servicio de cuentas crea automáticamente el depósito inicial

### **Características Especiales**

#### **🎯 Números de Cuenta Únicos**
- **Prefijo 99**: Las cuentas de onboarding tienen números con prefijo `99XXXX`
- **Identificación**: Fácil identificación de cuentas creadas por onboarding
- **Rango**: 990001 - 999999

#### **💰 Depósito Inicial Automático**
- **Sin movimiento manual**: El servicio de cuentas crea automáticamente el depósito
- **Condición**: Solo si `saldoInicial > 0`
- **Eficiencia**: Menos llamadas entre servicios

### **📋 Ejemplos de Uso**

#### **Cuenta de Ahorros**
```json
{
  "persona": {
    "identificacionpersona": "0919395186",
    "nombres": "María González",
    "genero": "F",
    "edad": 28,
    "direccion": "Av. Principal 456",
    "telefono": "0987654321"
  },
  "cliente": {
    "nombreUsuario": "mgonzalez",
    "contrasena": "SecurePass123*"
  },
  "cuenta": {
    "tipoCuenta": "AHORROS",
    "saldoInicial": 250.00
  }
}
```

#### **Cuenta Corriente**
```json
{
  "persona": {
    "identificacionpersona": "0912345678",
    "nombres": "Carlos Mendoza",
    "genero": "M",
    "edad": 35,
    "direccion": "Calle Secundaria 789",
    "telefono": "0991234567"
  },
  "cliente": {
    "nombreUsuario": "cmendoza",
    "contrasena": "BusinessPass456*"
  },
  "cuenta": {
    "tipoCuenta": "CORRIENTE",
    "saldoInicial": 1000.00
  }
}
```

### **📊 Respuesta Esperada**
```json
{
  "personaId": 123,
  "personaNombres": "María González",
  "personaIdentificacion": "0919395186",
  "clienteId": 456,
  "clienteNombreUsuario": "mgonzalez",
  "numeroCuenta": 991234,
  "tipoCuenta": "AHORROS",
  "saldoDisponible": "250.00",
  "mensaje": "Onboarding completado exitosamente. La cuenta ya incluye el depósito inicial automáticamente."
}
```

### **🔧 Endpoints de Soporte**

#### **Health Check**
```
GET http://{{host}}:8080/actuator/health
```

#### **Documentación API**
```
GET http://{{host}}:8080/api-docs
GET http://{{host}}:8080/swagger-ui.html
```

### **⚙️ Configuración Flexible**

El servicio puede configurarse mediante variables de entorno:

```bash
# URLs de servicios externos
PERSONAS_CLIENTES_URL=http://localhost:8081
CUENTAS_MOVIMIENTOS_URL=http://localhost:8082

# Puerto del servicio
SERVER_PORT=8080
```

### **🧪 Scripts de Prueba Automáticos**

La colección incluye scripts automáticos:

- **Pre-request**: Logging de solicitudes
- **Test**: Validación de códigos de estado y tiempos de respuesta
- **Validaciones**: Status 200/201/204 y tiempo < 5000ms

### **🚀 Beneficios del Onboarding Service**

1. **Simplicidad**: Una sola llamada para proceso completo
2. **Consistencia**: Transacciones coordinadas entre servicios
3. **Eficiencia**: Menos round-trips, mejor rendimiento
4. **Trazabilidad**: Logs completos del proceso de onboarding
5. **Escalabilidad**: Arquitectura de microservicios mantenida

### **📝 Notas de Integración**

- **Timeouts**: 30 segundos por llamada externa
- **Retry**: Manejo de errores con excepciones específicas
- **Idempotencia**: Los números de cuenta son únicos
- **Observabilidad**: Logs detallados en cada paso

La colección Postman ahora incluye todos los endpoints necesarios para probar la funcionalidad completa del ecosistema bancario.
