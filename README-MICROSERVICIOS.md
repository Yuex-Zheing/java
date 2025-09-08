# Microservicios Banking - Separación Personas/Clientes y Cuentas/Movimientos

## Arquitectura

El proyecto ha sido dividido en dos microservicios independientes:

### 1. personas-clientes-service (Puerto 8081)
**Responsabilidades:**
- Gestión de personas (CRUD)
- Gestión de clientes (CRUD)
- Proceso de onboarding completo
- Autenticación y autorización de clientes

**Endpoints principales:**
- `GET /api/personas` - Obtener todas las personas
- `POST /api/personas` - Crear una nueva persona
- `GET /api/clientes` - Obtener todos los clientes
- `POST /api/clientes` - Crear un nuevo cliente
- `POST /api/onboarding` - Proceso completo de onboarding

### 2. cuentas-movimientos-service (Puerto 8082)
**Responsabilidades:**
- Gestión de cuentas bancarias (CRUD)
- Gestión de movimientos/transacciones (CRUD)
- Cálculo de saldos
- Reportes de movimientos

**Endpoints principales:**
- `GET /api/cuentas` - Obtener todas las cuentas
- `POST /api/cuentas` - Crear una nueva cuenta
- `GET /api/movimientos` - Obtener todos los movimientos
- `POST /api/movimientos` - Realizar un nuevo movimiento

## Comunicación entre Microservicios

El microservicio de cuentas-movimientos se comunica con el de personas-clientes usando **OpenFeign** para:
- Validar la existencia de clientes al crear cuentas
- Obtener información de clientes para reportes

## Bases de Datos

Cada microservicio tiene su propia base de datos:
- `banking_personas_clientes` - Para el microservicio de personas y clientes
- `banking_cuentas_movimientos` - Para el microservicio de cuentas y movimientos

## Ejecución

### Requisitos
- Java 21
- MySQL 8.0+
- Maven 3.8+

### Pasos para ejecutar:

1. **Crear las bases de datos:**
```sql
CREATE DATABASE banking_personas_clientes;
CREATE DATABASE banking_cuentas_movimientos;
```

2. **Ejecutar personas-clientes-service:**
```bash
cd personas-clientes-service
mvn spring-boot:run
```

3. **Ejecutar cuentas-movimientos-service:**
```bash
cd cuentas-movimientos-service
mvn spring-boot:run
```

### URLs de acceso:
- Personas-Clientes Swagger: http://localhost:8081/swagger-ui.html
- Cuentas-Movimientos Swagger: http://localhost:8082/swagger-ui.html

## Mejoras Implementadas

1. **ModelMapper:** Simplifica la conversión entre entidades y DTOs
2. **Inyección por Constructor:** Reemplaza @Autowired para mejorar la testabilidad
3. **Separación de Responsabilidades:** Cada microservicio tiene una responsabilidad específica
4. **Comunicación Asíncrona:** Preparado para integrar Kafka en el futuro
5. **Documentación Automática:** Swagger/OpenAPI integrado en ambos servicios

## Próximos Pasos

1. Implementar Kafka para comunicación asíncrona
2. Agregar Spring Cloud Gateway como API Gateway
3. Implementar Circuit Breaker con Resilience4j
4. Agregar monitoreo con Micrometer y Prometheus
5. Implementar autenticación JWT distribuida
