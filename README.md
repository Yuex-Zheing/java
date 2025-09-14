````markdown
# Banking Microservices Project

## Descripción
Proyecto completo de microservicios bancarios desarrollado con Spring Boot 3.5.5 y Java 21, implementando una arquitectura de microservicios modular y escalable para la gestión integral de un sistema bancario.

## 🏗️ Arquitectura de Microservicios

### 1. 👥 Personas-Clientes Service
- **Puerto**: 8081
- **Base de Datos**: `banking_personas_clientes`
- **Responsabilidades**: 
  - Gestión completa de personas (CRUD)
  - Gestión de clientes bancarios (CRUD)
  - Validaciones de identificación y datos personales
  - Endpoints especializados para búsquedas por identificación y nombre de usuario
- **Ubicación**: `./personas-clientes-service/`

### 2. 💰 Cuentas-Movimientos Service
- **Puerto**: 8082
- **Base de Datos**: `banking_cuentas_movimientos`
- **Responsabilidades**: 
  - Gestión autónoma de cuentas bancarias
  - Procesamiento de movimientos y transacciones financieras
  - Cálculo automático de saldos disponibles
  - Generación de reportes financieros
  - Validaciones de saldo y límites de transacción
- **Ubicación**: `./cuentas-movimientos-service/`

### 3. 🚀 Onboarding Service
- **Puerto**: 8080
- **Responsabilidades**:
  - Orquestación del proceso completo de onboarding de clientes
  - Coordinación entre servicios de personas-clientes y cuentas-movimientos
  - Generación de números de cuenta especiales (prefijo 99)
  - Validaciones integrales antes de crear entidades
  - Manejo avanzado de errores y rollback
- **Ubicación**: `./onboarding-service/`

## 🛠️ Tecnologías Utilizadas
- **Framework**: Spring Boot 3.5.5
- **Java**: 21 (LTS)
- **Base de Datos**: MySQL 8.0+
- **Documentación API**: SpringDoc OpenAPI 2.7.0
- **Mapeo de Objetos**: ModelMapper 3.2.0
- **Comunicación Inter-Servicios**: Spring WebClient (Reactivo)
- **Monitoreo**: Spring Boot Actuator
- **Herramienta de Build**: Maven 3.6+
- **Containerización**: Docker (opcional)

## 📋 Configuración del Entorno

### Requisitos Previos
- ☑️ Java 21+ (OpenJDK recomendado)
- ☑️ Maven 3.6+
- ☑️ MySQL 8.0+
- ☑️ Puerto 8080, 8081, 8082 disponibles

### Variables de Entorno
Cada microservicio puede configurarse mediante las siguientes variables:

```bash
# Base de datos personas-clientes (8081)
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/banking_personas_clientes?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
SPRING_DATASOURCE_USERNAME=zheing
SPRING_DATASOURCE_PASSWORD=Pa$$w0rd

# Base de datos cuentas-movimientos (8082)
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/banking_cuentas_movimientos?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true

# Configuración JPA
SPRING_JPA_HIBERNATE_DDL_AUTO=none
SPRING_JPA_SHOW_SQL=true

# Puertos de servicios
SERVER_PORT=8080  # Onboarding
SERVER_PORT=8081  # Personas-Clientes
SERVER_PORT=8082  # Cuentas-Movimientos

# URLs de comunicación inter-servicios (Onboarding)
PERSONAS_CLIENTES_URL=http://localhost:8081
CUENTAS_MOVIMIENTOS_URL=http://localhost:8082

# Zona horaria
TZ=America/Guayaquil
```

## 🗄️ Configuración de Base de Datos

### Crear esquemas de base de datos
```sql
-- Para Personas-Clientes Service
CREATE DATABASE IF NOT EXISTS banking_personas_clientes;

-- Para Cuentas-Movimientos Service  
CREATE DATABASE IF NOT EXISTS banking_cuentas_movimientos;
```

### Scripts SQL Disponibles
Los scripts de configuración se encuentran en `./tools/`:
- `1_create_schema.sql` - Creación de esquemas completos
- `2_insert_sample_data.sql` - Datos de prueba para testing
- `3_grant_remote_access.sql` - Permisos de acceso remoto
- `docker-compose.yml` - Configuración Docker para MySQL

## 🚀 Ejecución de los Servicios

### Opción 1: Script Automático (Windows)
```bash
start-microservices.bat
```

### Opción 2: Ejecución Manual

#### 1. Iniciar Personas-Clientes Service
```bash
cd personas-clientes-service
mvn clean spring-boot:run
```
✅ **Disponible en**: http://localhost:8081

#### 2. Iniciar Cuentas-Movimientos Service
```bash
cd cuentas-movimientos-service
mvn clean spring-boot:run
```
✅ **Disponible en**: http://localhost:8082

#### 3. Iniciar Onboarding Service
```bash
cd onboarding-service
mvn clean spring-boot:run
```
✅ **Disponible en**: http://localhost:8080

## 📚 APIs y Documentación

### 🌐 Interfaces de Usuario
| Servicio | Swagger UI | API Docs | Health Check |
|----------|------------|----------|--------------|
| **Onboarding** | [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html) | [/v3/api-docs](http://localhost:8080/v3/api-docs) | [/actuator/health](http://localhost:8080/actuator/health) |
| **Personas-Clientes** | [http://localhost:8081/swagger-ui.html](http://localhost:8081/swagger-ui.html) | [/v3/api-docs](http://localhost:8081/v3/api-docs) | [/actuator/health](http://localhost:8081/actuator/health) |
| **Cuentas-Movimientos** | [http://localhost:8082/swagger-ui.html](http://localhost:8082/swagger-ui.html) | [/v3/api-docs](http://localhost:8082/v3/api-docs) | [/actuator/health](http://localhost:8082/actuator/health) |

### 🎯 Endpoints Principales

#### Onboarding Service (8080)
```http
POST /api/onboarding           # 🚀 Proceso completo de onboarding
GET  /api/health              # ❤️ Estado del servicio
```

#### Personas-Clientes Service (8081)
```http
GET  /api/personas                           # 📋 Listar personas
GET  /api/personas/{id}                      # 👤 Obtener persona por ID
GET  /api/personas/identificacion/{id}       # 🆔 Buscar por identificación
POST /api/personas                           # ➕ Crear persona
PUT  /api/personas/{id}                      # ✏️ Actualizar persona
DELETE /api/personas/{id}                    # 🗑️ Eliminar persona

GET  /api/clientes                           # 📋 Listar clientes
GET  /api/clientes/{id}                      # 👤 Obtener cliente por ID
GET  /api/clientes/persona/{personaId}       # 🔗 Cliente por persona
GET  /api/clientes/nombre-usuario/{usuario}  # 👥 Cliente por username
POST /api/clientes                           # ➕ Crear cliente
PUT  /api/clientes/{id}                      # ✏️ Actualizar cliente
DELETE /api/clientes/{id}                    # 🗑️ Eliminar cliente
```

#### Cuentas-Movimientos Service (8082)
```http
GET  /api/cuentas                           # 📋 Listar cuentas
GET  /api/cuentas/{numeroCuenta}            # 🏦 Obtener cuenta por número
GET  /api/cuentas/cliente/{idCliente}       # 👤 Cuentas por cliente
GET  /api/cuentas/activas                   # ✅ Cuentas activas
GET  /api/cuentas/tipo/{tipoCuenta}         # 📊 Cuentas por tipo
POST /api/cuentas                           # ➕ Crear cuenta
PUT  /api/cuentas/{numeroCuenta}            # ✏️ Actualizar cuenta
DELETE /api/cuentas/{numeroCuenta}          # 🗑️ Eliminar cuenta

GET  /api/movimientos                                   # 📋 Listar movimientos
GET  /api/movimientos/{id}                              # 📄 Movimiento por ID
GET  /api/movimientos/cuenta/{numeroCuenta}             # 💱 Movimientos por cuenta
GET  /api/movimientos/cuenta/{numeroCuenta}/fechas      # 📅 Movimientos con filtro de fechas
POST /api/movimientos/cuenta/{numeroCuenta}             # ➕ Realizar movimiento
PUT  /api/movimientos/{id}                              # ✏️ Actualizar movimiento
DELETE /api/movimientos/{id}                            # 🗑️ Eliminar movimiento

GET  /api/reportes/cuenta/{numeroCuenta}                # 📊 Reporte completo de cuenta
```

## 🔄 Comunicación Entre Servicios

### Patrón de Comunicación
- **Onboarding Service** actúa como **orquestador**
- **Comunicación reactiva** usando Spring WebClient
- **Timeouts configurables** (30 segundos por defecto)
- **Manejo robusto de errores** con retry y fallback

### Flujo de Onboarding
```
Cliente -> Onboarding Service -> Personas-Clientes Service
                              -> Cuentas-Movimientos Service
```

**Secuencia**:
1. **Validar datos iniciales**
2. **Buscar/Crear persona** en personas-clientes-service
3. **Buscar/Crear cliente** en personas-clientes-service
4. **Buscar/Crear cuenta** en cuentas-movimientos-service
5. **Responder con información completa**

## 🏗️ Estructura del Proyecto
```
banking-microservices/
├── 📁 personas-clientes-service/          # Microservicio de Personas y Clientes
│   ├── src/main/java/com/wquimis/demo/personasclientes/
│   │   ├── 🚀 PersonasClientesApplication.java
│   │   ├── 🎛️ config/
│   │   ├── 🌐 controller/
│   │   ├── 📦 dto/
│   │   ├── 🏢 entities/
│   │   ├── ⚠️ exceptions/
│   │   ├── 💾 repository/
│   │   ├── 🔧 services/
│   │   └── 🛠️ utils/
│   ├── src/main/resources/application.properties
│   ├── pom.xml
│   └── README.md
├── 📁 cuentas-movimientos-service/        # Microservicio de Cuentas y Movimientos
│   ├── src/main/java/com/wquimis/demo/cuentasmovimientos/
│   │   ├── 🚀 CuentasMovimientosApplication.java
│   │   ├── 🎛️ config/
│   │   ├── 🌐 controller/
│   │   ├── 📦 dto/
│   │   ├── 🏢 entities/
│   │   ├── ⚠️ exceptions/
│   │   ├── 💾 repository/
│   │   ├── 🔧 services/
│   │   └── 🛠️ utils/
│   ├── src/main/resources/application.properties
│   ├── pom.xml
│   └── README.md
├── 📁 onboarding-service/                # Microservicio de Orquestación
│   ├── src/main/java/com/wquimis/demo/onboarding/
│   │   ├── 🚀 OnboardingApplication.java
│   │   ├── 🎛️ config/
│   │   ├── 🌐 controller/
│   │   ├── 📦 dto/
│   │   ├── ⚠️ exceptions/
│   │   └── 🔧 services/
│   ├── src/main/resources/application.properties
│   ├── pom.xml
│   ├── README.md
│   └── ONBOARDING_IMPROVEMENTS.md
├── 📁 tools/                              # Scripts y herramientas
│   ├── 🗃️ 1_create_schema.sql
│   ├── 📊 2_insert_sample_data.sql
│   ├── 🔐 3_grant_remote_access.sql
│   ├── 🐳 docker-compose.yml
│   ├── ⚙️ mysql-custom.cnf
│   ├── 📮 banking-api.postman_collection.json
│   └── 📝 POSTMAN_COLLECTION_UPDATES.md
├── 🚀 start-microservices.bat            # Script de inicio automático
├── 📋 Dockerfile                         # Configuración Docker
├── 📦 pom.xml                            # Configuración Maven padre
├── 📖 README.md                          # Este archivo
└── 📚 README-MICROSERVICIOS.md           # Documentación adicional
```

## 🧪 Testing y Desarrollo

### Colección de Postman
Una colección completa está disponible en:
`tools/banking-api.postman_collection.json`

**Incluye**:
- ✅ Todos los endpoints de los 3 microservicios
- ✅ Ejemplos de payloads para cada operación
- ✅ Scripts de validación automática
- ✅ Variables de entorno configurables
- ✅ Casos de prueba para onboarding completo

### Comandos de Testing
```bash
# Compilar todos los servicios
mvn clean compile -f personas-clientes-service/pom.xml
mvn clean compile -f cuentas-movimientos-service/pom.xml
mvn clean compile -f onboarding-service/pom.xml

# Ejecutar tests
mvn test -f personas-clientes-service/pom.xml
mvn test -f cuentas-movimientos-service/pom.xml
mvn test -f onboarding-service/pom.xml

# Generar JARs ejecutables
mvn clean package -f personas-clientes-service/pom.xml
mvn clean package -f cuentas-movimientos-service/pom.xml
mvn clean package -f onboarding-service/pom.xml
```

## 🔧 Características Avanzadas

### 🎯 Validaciones Inteligentes del Onboarding
- **Detección de duplicados**: Previene creación de entidades duplicadas
- **Validación de coherencia**: Verifica que los datos coincidan si la entidad ya existe
- **Manejo de errores de BD**: Convierte errores MySQL en mensajes claros
- **Rollback automático**: Deshace cambios si algún paso falla

### 🏦 Números de Cuenta Especiales
- **Prefijo 99**: Cuentas creadas por onboarding tienen números 99XXXX
- **Rango**: 990001 - 999999
- **Identificación**: Fácil identificación de cuentas de onboarding

### 📊 Manejo de Errores Estructurado
```json
{
  "codigo": "ERR_CONFLICT_001",
  "mensaje": "La persona con esta identificación ya existe",
  "detalle": "La persona especificada ya existe en el sistema",
  "timestamp": "2025-09-08T10:30:00"
}
```

**Códigos de Error**:
- `ERR_CONFLICT_001`: Entidades duplicadas
- `ERR_VALIDATION_001`: Datos inválidos
- `ERR_EXT_001`: Errores en servicios externos
- `ERR_ONB_001`: Errores de onboarding
- `ERR_999`: Errores inesperados

## 🏛️ Principios Arquitectónicos

### Implementados ✅
- **Microservices Architecture**: Servicios independientes y especializados
- **Database per Service**: Cada servicio tiene su propia base de datos
- **Single Responsibility**: Cada servicio maneja un dominio específico
- **API-First Design**: APIs bien documentadas con OpenAPI
- **Reactive Programming**: WebClient para comunicación no bloqueante
- **Dependency Injection**: Constructores en lugar de @Autowired
- **Fail Fast**: Validaciones tempranas y manejo de errores

### Considerados para Futuro 🔮
- **API Gateway**: Punto único de entrada (Spring Cloud Gateway)
- **Service Discovery**: Eureka o Consul para registro de servicios
- **Circuit Breaker**: Resilience4j para tolerancia a fallos
- **Distributed Tracing**: Sleuth + Zipkin para observabilidad
- **Configuration Server**: Spring Cloud Config para gestión centralizada
- **Event-Driven Architecture**: Mensajería asíncrona con RabbitMQ/Kafka

## 🐳 Containerización (Docker)

### Docker Compose para Desarrollo
```bash
cd tools/
docker-compose up -d
```

**Incluye**:
- 🗄️ MySQL 8.0 con configuración personalizada
- 🔧 Configuración automática de esquemas
- 📊 Datos de prueba precargados
- 🌐 Puertos expuestos para desarrollo local

## 📈 Monitoreo y Observabilidad

### Endpoints de Actuator
Todos los servicios incluyen:
- `/actuator/health` - Estado del servicio
- `/actuator/info` - Información del servicio
- `/actuator/metrics` - Métricas de rendimiento

### Logging Estructurado
- **Niveles**: DEBUG para desarrollo, INFO para producción
- **Formato**: JSON estructurado para parsing automático
- **Contexto**: Incluye request IDs para trazabilidad

## ⚡ Rendimiento y Escalabilidad

### Optimizaciones Implementadas
- **Lazy Loading**: Entidades JPA optimizadas
- **Connection Pooling**: Configuración HikariCP
- **Timeout Management**: Timeouts configurables para WebClient
- **Stateless Services**: Servicios sin estado para escalabilidad horizontal

### Métricas Clave
- **Tiempo de respuesta**: < 500ms para operaciones CRUD
- **Throughput**: > 100 requests/segundo por servicio
- **Disponibilidad**: 99.9% uptime objetivo

## 🔒 Seguridad

### Medidas Implementadas
- **Validación de entrada**: Bean Validation en todos los DTOs
- **SQL Injection Prevention**: JPA con parámetros preparados
- **Error Handling**: No exposición de información sensible
- **CORS Configuration**: Configuración para desarrollo local

### Consideraciones Futuras
- **JWT Authentication**: Tokens para autenticación
- **OAuth2 Integration**: Integración con proveedores externos
- **Rate Limiting**: Limitación de requests por IP
- **API Versioning**: Versionado de APIs para evolución

## 🚀 Deployment y DevOps

### Estrategias de Deployment
- **Blue-Green**: Deploy sin downtime
- **Rolling Updates**: Actualizaciones graduales
- **Canary Releases**: Testing con subconjunto de usuarios

### Automatización
```bash
# Script de inicio completo
./start-microservices.bat

# Verificación de salud de todos los servicios
curl http://localhost:8080/actuator/health
curl http://localhost:8081/actuator/health
curl http://localhost:8082/actuator/health
```

## 🧩 Casos de Uso Principales

### 1. 🆕 Onboarding Completo de Cliente
```bash
POST http://localhost:8080/api/onboarding
```
**Flujo**: Persona → Cliente → Cuenta → Depósito Inicial

### 2. 👤 Gestión de Personas y Clientes
```bash
# Personas
GET/POST/PUT/DELETE http://localhost:8081/api/personas

# Clientes
GET/POST/PUT/DELETE http://localhost:8081/api/clientes
```

### 3. 💰 Operaciones Bancarias
```bash
# Cuentas
GET/POST/PUT/DELETE http://localhost:8082/api/cuentas

# Movimientos
GET/POST/PUT/DELETE http://localhost:8082/api/movimientos
```

### 4. 📊 Reportes y Consultas
```bash
# Reporte completo de cuenta
GET http://localhost:8082/api/reportes/cuenta/{numeroCuenta}

# Movimientos con filtros de fecha
GET http://localhost:8082/api/movimientos/cuenta/{numeroCuenta}/fechas?desde=2025-01-01&hasta=2025-12-31
```

## 💡 Lecciones Aprendidas y Best Practices

### ✅ Mejores Prácticas Aplicadas
1. **Separación de Responsabilidades**: Cada servicio maneja un dominio específico
2. **Comunicación Asíncrona**: WebClient reactivo para mejor rendimiento
3. **Documentación Automática**: OpenAPI generada desde código
4. **Testing Integral**: Postman collection con casos completos
5. **Observabilidad**: Logs estructurados y métricas de Actuator
6. **Configuración Externa**: Variables de entorno para flexibilidad

### 🎯 Optimizaciones Realizadas
- **Pooling de Conexiones**: HikariCP para mejor gestión de BD
- **Validaciones Tempranas**: Fail-fast para mejor UX
- **Manejo de Errores**: Códigos estructurados y mensajes claros
- **Idempotencia**: Operaciones seguras para retry

## 🆘 Troubleshooting

### Problemas Comunes
1. **Puerto ocupado**: Verificar que puertos 8080, 8081, 8082 estén libres
2. **Base de datos no conecta**: Verificar MySQL corriendo y credenciales
3. **Servicios no se comunican**: Verificar URLs en application.properties
4. **Timeouts**: Revisar logs de WebClient para errores de conectividad

### Comandos de Diagnóstico
```bash
# Verificar puertos
netstat -an | findstr "8080 8081 8082"

# Verificar logs
tail -f */logs/application.log

# Verificar conectividad entre servicios
curl http://localhost:8081/actuator/health
curl http://localhost:8082/actuator/health
```

## 📞 Contacto y Soporte

### Documentación Adicional
- **Cada microservicio**: README.md en su directorio
- **Mejoras de Onboarding**: `onboarding-service/ONBOARDING_IMPROVEMENTS.md`
- **Colección Postman**: `tools/POSTMAN_COLLECTION_UPDATES.md`
- **Configuración Docker**: `tools/docker-compose.yml`

### Recursos de Desarrollo
- **Swagger UIs**: Documentación interactiva en cada servicio
- **Actuator Endpoints**: Monitoreo y métricas en tiempo real
- **Scripts SQL**: Setup automático de base de datos
- **Postman Collection**: Testing automatizado completo

---

## 🎉 ¡Proyecto Completamente Funcional!

Este proyecto representa una implementación completa de microservicios bancarios con:
- ✅ **3 microservicios independientes** y especializados
- ✅ **APIs RESTful completas** con documentación OpenAPI
- ✅ **Comunicación inter-servicios** robusta y reactiva
- ✅ **Proceso de onboarding orquestado** end-to-end
- ✅ **Manejo avanzado de errores** y validaciones
- ✅ **Testing automatizado** con Postman
- ✅ **Configuración flexible** para múltiples entornos
- ✅ **Monitoreo y observabilidad** integrados



**¡Listo para desarrollo, testing y deployment!** 🚀
````
## 🧹 Mantenibilidad y Convenciones (Refactor 2025-09)

### Principios Adoptados
- Separación clara Request vs Response cuando hay diferencia semántica (p.ej. creación de cliente vs datos expuestos).  
- No exponer campos sensibles (contrasena) en DTO de respuesta.  
- Evitar lógica de negocio en DTOs (solo validaciones de formato / dominio simple con Bean Validation).  
- Idempotencia en creación de cuenta y movimiento inicial (se evita doble acreditación del saldo).  

### DTO Clave
- `CuentaDTO`: Creación y representación. `saldoDisponible` puede ser null en el request; el servidor lo inicializa.  
- `MovimientoDTO`: El cliente envía únicamente `movimientodescripcion`, `tipomovimiento`, `montomovimiento`. Campos `id`, `fecha`, `hora`, `saldo`, `esReverso` son calculados/derivados.  
- `ClienteDTO` / `ClienteResponseDTO`: Se mantiene separación para proteger campos de entrada (p.ej. contrasena) y permitir evolución independiente.  

### Eliminaciones / Deprecaciones
- `personas-clientes-service`: `OnboardingRequestDTO` eliminado (no tenía referencias).  
- `ErrorDTO` centralizado en módulo `common-lib` y removido de servicios individuales.  

### Reglas de Extensión
1. Antes de agregar un nuevo DTO, validar si un DTO existente puede ampliarse sin romper compatibilidad.  
2. Nuevos campos deben ser opcionales (nullables) por defecto en requests para no romper clientes.  
3. No colocar lógica de transformación en controladores; usar servicios o converters.  

### Errores y Manejo de Excepciones
- `ErrorDTO` repetido por servicio por simplicidad de despliegue. Futuro: extraer a módulo común si se consolida repositorio multi-módulo.  

### Futuras Mejores (Opcionales)
- Unificar `ErrorDTO` en librería compartida.  
- Reemplazar conversor manual por MapStruct si crece complejidad.  
- Introducir un flag explícito para detectar depósito inicial en lugar de heurística basada en descripción.  

### Auditoría de Doble Saldo (Resumen)
Se detectó doble acreditación potencial al crear cuenta + movimiento inicial. Solución: detección en `MovimientoServiceImpl` para omitir acreditación si coincide patrón de depósito inicial ya reflejado en `saldodisponible`.

