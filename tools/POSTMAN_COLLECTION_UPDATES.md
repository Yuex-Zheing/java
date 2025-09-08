# Banking API - Postman Collection y Contratos API Actualizados

## 📅 **Última Actualización: Septiembre 8, 2025**

### 🆕 **Nueva Versión: v2.0.0**
- ✅ Colección de Postman completamente actualizada
- ✅ Contratos OpenAPI 3.0.3 para todos los servicios
- ✅ Documentación completa de entidades y endpoints

---

## 🚀 **Principales Actualizaciones**

### **1. Análisis Completo de Controladores**
Se analizaron todos los controladores de los 3 microservicios:
- `CuentaMovimientoController.java` - 15 endpoints
- `OnboardingController.java` - 2 endpoints  
- `PersonaClienteController.java` - 12 endpoints

### **2. Endpoints Agregados a la Colección**

#### **Personas-Clientes Service (8081)**
- ✅ `GET /api/personas/identificacion/{identificacion}` - Buscar persona por identificación
- ✅ `GET /api/clientes/persona/{personaId}` - Buscar cliente por ID de persona
- ✅ `GET /api/clientes/nombre-usuario/{nombreUsuario}` - Buscar cliente por nombre de usuario

#### **Cuentas-Movimientos Service (8082)**
- ✅ `GET /api/cuentas/cliente/{idCliente}` - Obtener cuentas por cliente
- ✅ Separación de depósitos y retiros en endpoints diferenciados
- ✅ Ejemplos mejorados para todos los movimientos

#### **Onboarding Service (8080)**
- ✅ Corrección del endpoint de health check: `/api/health`

### **3. Contratos OpenAPI 3.0.3 Creados**

#### **📄 `onboarding-service.yaml`**
- Documentación completa del proceso de onboarding
- Ejemplos de casos de éxito y error
- Esquemas detallados de DTOs

#### **📄 `personas-clientes-service.yaml`**
- Operaciones CRUD para personas y clientes
- Validaciones de entrada
- Múltiples métodos de búsqueda

#### **📄 `cuentas-movimientos-service.yaml`**
- Gestión completa de cuentas y movimientos
- Endpoints de reportes
- Proceso de anulación de movimientos

---

## 🆕 **Endpoints de Onboarding Service (Puerto 8080)**

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

## 📋 **Nuevos Archivos Creados**

### **1. Contratos OpenAPI 3.0.3**
- `onboarding-service.yaml` - Contrato del servicio de onboarding
- `personas-clientes-service.yaml` - Contrato del servicio de personas y clientes
- `cuentas-movimientos-service.yaml` - Contrato del servicio de cuentas y movimientos

### **2. Documentación**
- `API_CONTRACTS_README.md` - Documentación completa de contratos y uso

### **3. Colección Actualizada**
- `banking-api.postman_collection.json` - v2.0.0 con todos los endpoints

## 🔍 **Resumen de Entidades**

### **Persona**
- ID único autogenerado
- Identificación única de 10 dígitos
- Datos demográficos completos
- Estado activo/inactivo

### **Cliente**  
- Asociado a una persona (OneToOne)
- Nombre de usuario único
- Contraseña encriptada
- Estado activo/inactivo

### **Cuenta**
- Número único (prefijo 99 para onboarding)
- Tipos: AHORROS, CORRIENTE
- Saldos inicial y disponible
- Trazabilidad de fechas

### **Movimiento**
- Asociado a cuenta (ManyToOne)
- Tipos: DEPOSITO, RETIRO
- Fecha y hora de transacción
- Descripción y trazabilidad completa

## 🎯 **Próximos Pasos**

1. **Importar colección** en Postman
2. **Revisar contratos** OpenAPI para integración
3. **Probar flujos** de onboarding completo
4. **Validar** todos los endpoints nuevos

## 📞 **Soporte**

Para consultas sobre estos cambios:
- Revisar `API_CONTRACTS_README.md` para documentación detallada
- Usar la colección de Postman para pruebas
- Los contratos YAML contienen toda la especificación técnica

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
