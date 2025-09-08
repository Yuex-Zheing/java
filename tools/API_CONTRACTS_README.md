# Contratos de API y Colección de Postman Actualizados

Este directorio contiene los contratos de API y la colección de Postman actualizados para el sistema de microservicios bancarios.

## 📋 Archivos Incluidos

### Contratos OpenAPI 3.0.3 (YAML)
- `onboarding-service.yaml` - API del servicio de onboarding
- `personas-clientes-service.yaml` - API del servicio de personas y clientes  
- `cuentas-movimientos-service.yaml` - API del servicio de cuentas y movimientos

### Colección de Postman
- `banking-api.postman_collection.json` - Colección completa con todos los endpoints

### Otros Archivos
- `POSTMAN_COLLECTION_UPDATES.md` - Historial de actualizaciones
- `API_CONTRACTS_README.md` - Este archivo

## 🏗️ Arquitectura de Microservicios

### 1. Onboarding Service (Puerto 8080)
**Propósito**: Orquestación del proceso completo de onboarding de clientes

**Endpoints principales**:
- `POST /api/onboarding` - Crear cliente completo
- `GET /api/health` - Verificación de estado

**Características especiales**:
- Crea cuentas con números prefijo 99 (990001-999999)
- Orquesta la creación de persona, cliente y cuenta
- Genera depósito inicial automático

### 2. Personas-Clientes Service (Puerto 8081)
**Propósito**: Gestión de personas físicas y clientes bancarios

**Endpoints de Personas**:
- `GET /api/personas` - Listar personas activas
- `GET /api/personas/{id}` - Obtener persona por ID
- `GET /api/personas/identificacion/{identificacion}` - Buscar por identificación
- `POST /api/personas` - Crear persona
- `PUT /api/personas/{id}` - Actualizar persona
- `DELETE /api/personas/{id}` - Eliminar persona

**Endpoints de Clientes**:
- `GET /api/clientes` - Listar clientes activos
- `GET /api/clientes/{id}` - Obtener cliente por ID
- `GET /api/clientes/persona/{personaId}` - Buscar por ID de persona
- `GET /api/clientes/nombre-usuario/{nombreUsuario}` - Buscar por nombre de usuario
- `POST /api/clientes` - Crear cliente
- `PUT /api/clientes/{id}` - Actualizar cliente
- `DELETE /api/clientes/{id}` - Eliminar cliente

### 3. Cuentas-Movimientos Service (Puerto 8082)
**Propósito**: Gestión de cuentas bancarias y movimientos financieros

**Endpoints de Cuentas**:
- `GET /api/cuentas` - Listar todas las cuentas
- `GET /api/cuentas/activas` - Listar cuentas activas
- `GET /api/cuentas/{numeroCuenta}` - Obtener cuenta por número
- `GET /api/cuentas/cliente/{idCliente}` - Obtener cuentas por cliente
- `GET /api/cuentas/tipo/{tipoCuenta}` - Obtener cuentas por tipo
- `POST /api/cuentas` - Crear cuenta
- `PUT /api/cuentas/{numeroCuenta}` - Actualizar cuenta
- `DELETE /api/cuentas/{numeroCuenta}` - Eliminar cuenta

**Endpoints de Movimientos**:
- `GET /api/movimientos` - Listar todos los movimientos
- `GET /api/movimientos/{id}` - Obtener movimiento por ID
- `GET /api/movimientos/cuenta/{numeroCuenta}` - Movimientos por cuenta
- `GET /api/movimientos/cuenta/{numeroCuenta}/fechas` - Movimientos por fecha
- `POST /api/movimientos/cuenta/{numeroCuenta}` - Realizar movimiento
- `PUT /api/movimientos/{id}` - Actualizar movimiento
- `DELETE /api/movimientos/{id}` - Anular movimiento

**Endpoints de Reportes**:
- `GET /api/reportes/cuenta/{numeroCuenta}` - Generar reporte de cuenta

## 📊 Entidades Principales

### Persona
```yaml
properties:
  idpersona: integer (ID único)
  identificacionpersona: string (10 chars, único)
  nombres: string (150 chars)
  genero: string (M/F)
  edad: integer (18-120)
  direccion: string (300 chars)
  telefono: string (15 chars)
  estado: boolean
```

### Cliente
```yaml
properties:
  idcliente: integer (ID único)
  persona: Persona (relación OneToOne)
  nombreusuario: string (50 chars, único)
  contrasena: string (100 chars, encriptada)
  estado: boolean
```

### Cuenta
```yaml
properties:
  numerocuenta: integer (ID único)
  idcliente: integer (referencia a cliente)
  tipocuenta: enum (AHORROS, CORRIENTE)
  saldoinicial: decimal (10,4)
  saldodisponible: decimal (10,4)
  estado: boolean
  fechacreacion: datetime
  fechacierre: datetime
```

### Movimiento
```yaml
properties:
  idmovimiento: integer (ID único)
  cuenta: Cuenta (relación ManyToOne)
  estado: boolean
  fechamovimiento: date
  horamovimiento: time
  tipomovimiento: enum (DEPOSITO, RETIRO)
  montomovimiento: decimal (10,4)
  saldodisponible: decimal (10,4)
  movimientodescripcion: string (300 chars)
```

## 🔧 Uso de la Colección de Postman

### Variables de Entorno
La colección utiliza las siguientes variables:
- `{{host}}` - Host donde están ejecutándose los servicios (default: localhost)

### Ejemplos Incluidos
- **Onboarding completo**: Creación de cliente con cuenta de ahorros y corriente
- **Gestión de personas**: CRUD completo con validaciones
- **Gestión de clientes**: Operaciones con diferentes métodos de búsqueda
- **Operaciones bancarias**: Depósitos, retiros y consultas
- **Reportes**: Generación de estados de cuenta

### Scripts de Prueba
Cada request incluye:
- **Pre-request**: Logging de la operación
- **Test**: Validación de códigos de respuesta y tiempos

## 📝 Contratos OpenAPI

### Características de los Contratos
- **OpenAPI 3.0.3**: Última versión estable
- **Documentación completa**: Descripciones detalladas de cada endpoint
- **Ejemplos**: Casos de uso reales para cada operación
- **Esquemas**: Definición completa de DTOs y entidades
- **Manejo de errores**: Códigos de respuesta y mensajes de error estándar

### Validaciones Incluidas
- **Tipos de datos**: Validación de tipos y formatos
- **Rangos**: Valores mínimos y máximos
- **Patrones**: Expresiones regulares para formatos específicos
- **Longitudes**: Límites de caracteres para strings
- **Enumeraciones**: Valores permitidos para campos específicos

## 🚀 Cómo Usar

### 1. Importar en Postman
```bash
1. Abrir Postman
2. File > Import
3. Seleccionar banking-api.postman_collection.json
4. Configurar variable de entorno 'host' si es necesario
```

### 2. Generar Documentación desde OpenAPI
```bash
# Usando Swagger UI
docker run -p 80:8080 -e SWAGGER_JSON=/api/spec.yaml -v $(pwd):/api swaggerapi/swagger-ui

# Usando Redoc
npx redoc-cli build onboarding-service.yaml
```

### 3. Generar Código Cliente
```bash
# Usando OpenAPI Generator
openapi-generator generate -i personas-clientes-service.yaml -g typescript-fetch -o ./clients/personas-clientes
```

## ⚠️ Consideraciones Importantes

### Números de Cuenta
- **Onboarding**: Prefijo 99 (990001-999999)
- **Creación directa**: Sin restricción de prefijo

### Movimientos
- **Depósito inicial**: Se crea automáticamente al crear cuenta con saldo > 0
- **Anulación**: Crea movimiento de reverso, no elimina el original
- **Trazabilidad**: Todos los movimientos quedan registrados para auditoría

### Estados
- **Persona/Cliente/Cuenta activos**: `estado: true`
- **Eliminación lógica**: Cambio de estado a `false`
- **Movimientos anulados**: `estado: false` + movimiento de reverso

## 📋 Lista de Actualizaciones

### Colección de Postman v2.0.0
- ✅ Agregados todos los endpoints faltantes
- ✅ Separación de depósitos y retiros
- ✅ Ejemplos actualizados con datos realistas
- ✅ Corrección de DTOs de actualización
- ✅ Health checks actualizados
- ✅ Scripts de validación mejorados

### Contratos OpenAPI v1.0.0
- ✅ Contratos completos para los 3 servicios
- ✅ Documentación detallada de cada endpoint
- ✅ Esquemas completos de DTOs y entidades
- ✅ Ejemplos de request/response
- ✅ Manejo de errores estandarizado
- ✅ Validaciones de entrada detalladas

## 📞 Soporte

Para preguntas o soporte sobre estos contratos:
- **Email**: dev@bankingsystem.com
- **Documentación**: https://docs.bankingsystem.com
- **Issues**: Crear issue en el repositorio del proyecto
