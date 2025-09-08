# Cuentas y Movimientos Microservice

## Descripción
Microservicio completamente autónomo para la gestión de cuentas bancarias y movimientos financieros. Este servicio no depende de otros microservicios y gestiona de forma independiente todas las operaciones relacionadas con cuentas y movimientos.

## Tecnologías
- Spring Boot 3.5.5
- Java 21
- MySQL 8
- ModelMapper 3.2.0
- SpringDoc OpenAPI 2.7.0
- Spring Boot Actuator (monitoring)

## Base de Datos
- Nombre: `bankingdb` (tabla: cuentas, movimientos)
- Puerto: 3306
- Usuario: configurado por variable de entorno `SPRING_DATASOURCE_USERNAME` (default: zheing)
- Contraseña: configurado por variable de entorno `SPRING_DATASOURCE_PASSWORD` (default: Pa$$w0rd)

## Configuración

### Variables de Entorno
```bash
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/bankingdb?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
SPRING_DATASOURCE_USERNAME=zheing
SPRING_DATASOURCE_PASSWORD=Pa$$w0rd
SPRING_JPA_HIBERNATE_DDL_AUTO=none
SPRING_JPA_SHOW_SQL=true
SERVER_PORT=8082
TZ=America/Guayaquil
```

## Autonomía del Servicio

Este microservicio ha sido diseñado para ser **completamente autónomo** y no depende de otros servicios:

- ✅ **Gestión independiente**: Maneja todas las operaciones de cuentas y movimientos sin llamadas externas
- ✅ **Base de datos propia**: Utiliza sus propias tablas (cuentas, movimientos) 
- ✅ **Sin dependencias de microservicios**: No utiliza Feign Clients ni comunicación entre servicios
- ✅ **API completa**: Proporciona endpoints completos para CRUD y reportes
- ✅ **Validaciones internas**: Todas las validaciones se realizan internamente
- ✅ **Transacciones atómicas**: Manejo completo de transacciones financieras

## Ejecución

### Compilar el proyecto
```bash
mvn clean compile
```

### Ejecutar tests
```bash
mvn test
```

### Ejecutar la aplicación
```bash
mvn spring-boot:run
```

### Generar JAR
```bash
mvn clean package
```

## APIs Disponibles

### Swagger UI
- URL: http://localhost:8082/swagger-ui.html
- API Docs: http://localhost:8082/v3/api-docs

### Endpoints Principales

#### Cuentas
- `GET /api/cuentas` - Listar todas las cuentas
- `GET /api/cuentas/{numeroCuenta}` - Obtener cuenta por número
- `GET /api/cuentas/activas` - Obtener cuentas activas
- `GET /api/cuentas/tipo/{tipoCuenta}` - Obtener cuentas por tipo
- `POST /api/cuentas` - Crear nueva cuenta
- `PUT /api/cuentas/{numeroCuenta}` - Actualizar cuenta
- `DELETE /api/cuentas/{numeroCuenta}` - Eliminar cuenta

#### Movimientos
- `GET /api/movimientos` - Listar todos los movimientos
- `GET /api/movimientos/{id}` - Obtener movimiento por ID
- `GET /api/movimientos/cuenta/{numeroCuenta}` - Obtener movimientos por cuenta
- `GET /api/movimientos/cuenta/{numeroCuenta}/fechas` - Obtener movimientos por cuenta y fechas
- `POST /api/movimientos/cuenta/{numeroCuenta}` - Realizar nuevo movimiento
- `PUT /api/movimientos/{id}` - Actualizar movimiento
- `DELETE /api/movimientos/{id}` - Eliminar movimiento

#### Reportes
- `GET /api/reportes/cuenta/{numeroCuenta}` - Reporte completo de cuenta y movimientos

### Health Check
- `GET /actuator/health` - Estado del servicio
- `GET /actuator/info` - Información del servicio
- `GET /actuator/metrics` - Métricas del servicio

## Estructura del Proyecto
```
src/
├── main/
│   ├── java/com/wquimis/demo/cuentasmovimientos/
│   │   ├── CuentasMovimientosApplication.java
│   │   ├── dto/
│   │   │   ├── ClienteDTO.java
│   │   │   ├── CuentaDTO.java
│   │   │   ├── MovimientoDTO.java
│   │   │   └── ReporteMovimientoDTO.java
│   │   ├── entities/
│   │   │   ├── Cuenta.java
│   │   │   └── Movimiento.java
│   │   ├── repository/
│   │   │   ├── CuentaRepository.java
│   │   │   └── MovimientoRepository.java
│   │   └── services/
│   │       ├── CuentaService.java
│   │       ├── CuentaServiceImpl.java
│   │       ├── MovimientoService.java
│   │       └── MovimientoServiceImpl.java
│   └── resources/
│       └── application.properties
```

## Base de Datos

### Esquema utilizado
```sql
USE bankingdb;
```

### Tablas principales
- `cuentas`: Información de cuentas bancarias independientes
- `movimientos`: Transacciones y movimientos financieros

## Características del Servicio Autónomo

### Ventajas de la Autonomía
- **Escalabilidad independiente**: Se puede escalar sin afectar otros servicios
- **Mantenimiento simplificado**: Cambios no impactan otros microservicios
- **Resiliencia**: No depende de la disponibilidad de otros servicios
- **Desarrollo ágil**: Equipo puede trabajar independientemente
- **Despliegue independiente**: Deploy sin coordinación con otros servicios

### Modelo de Datos Autónomo
- Cuentas con identificación única por número de cuenta
- Movimientos vinculados directamente a cuentas
- Sin referencias externas a clientes o personas
- Datos necesarios incluidos en el contexto de la cuenta

## Notas de Desarrollo
- Inyección de dependencias mediante constructores (no @Autowired)
- Uso de ModelMapper para conversión DTO-Entity  
- Validaciones y reglas de negocio completamente internas
- Manejo de errores sin dependencias externas
- Separación clara de responsabilidades
- Configuración mediante variables de entorno para diferentes ambientes

## Consideraciones de Despliegue
- **Servicio completamente independiente**: No requiere otros servicios para funcionar
- **Base de datos propia**: Utiliza su esquema dedicado
- **Puerto dedicado**: 8082 (configurable)
- **Monitoreo integrado**: Actuator endpoints para health checks
- **Documentación automática**: Swagger UI disponible
