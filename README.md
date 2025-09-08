# Banking Microservices Project

## Descripción
Proyecto de microservicios bancarios desarrollado con Spring Boot, separado en servicios independientes para gestión de personas/clientes y cuentas/movimientos.

## Arquitectura de Microservicios

### 1. Personas-Clientes Service
- **Puerto**: 8081
- **Base de Datos**: `banking_personas_clientes`
- **Responsabilidades**: Gestión de personas y clientes
- **Ubicación**: `./personas-clientes-service/`

### 2. Cuentas-Movimientos Service
- **Puerto**: 8082
- **Base de Datos**: `banking_cuentas_movimientos`
- **Responsabilidades**: Gestión de cuentas bancarias y movimientos financieros
- **Ubicación**: `./cuentas-movimientos-service/`

## Tecnologías Utilizadas
- **Framework**: Spring Boot 3.5.5
- **Java**: 21
- **Base de Datos**: MySQL 8
- **Documentación API**: SpringDoc OpenAPI 2.7.0
- **Mapeo de Objetos**: ModelMapper 3.2.0
- **Comunicación Inter-Servicios**: Spring Cloud OpenFeign 4.0.4
- **Herramienta de Build**: Maven

## Configuración del Entorno

### Requisitos Previos
- Java 21+
- Maven 3.6+
- MySQL 8+

### Variables de Entorno
Cada microservicio puede configurarse mediante las siguientes variables de entorno:

```bash
# Base de datos
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/[database_name]
SPRING_DATASOURCE_USERNAME=zheing
SPRING_DATASOURCE_PASSWORD=Pa$$w0rd

# JPA
SPRING_JPA_HIBERNATE_DDL_AUTO=none
SPRING_JPA_SHOW_SQL=true

# Servidor
SERVER_PORT=[port_number]

# Zona horaria
TZ=America/Guayaquil
```

## Configuración de Base de Datos

### Crear esquemas de base de datos
```sql
-- Para Personas-Clientes Service
CREATE DATABASE IF NOT EXISTS banking_personas_clientes;

-- Para Cuentas-Movimientos Service
CREATE DATABASE IF NOT EXISTS banking_cuentas_movimientos;
```

### Scripts SQL
Los scripts de configuración se encuentran en:
- `tools/1_create_schema.sql` - Creación de esquemas
- `tools/2_insert_sample_data.sql` - Datos de prueba
- `microservices-setup.sql` - Configuración completa de microservicios

## Ejecución de los Servicios

### 1. Iniciar Personas-Clientes Service
```bash
cd personas-clientes-service
mvn spring-boot:run
```
El servicio estará disponible en: http://localhost:8081

### 2. Iniciar Cuentas-Movimientos Service
```bash
cd cuentas-movimientos-service
mvn spring-boot:run
```
El servicio estará disponible en: http://localhost:8082

## APIs y Documentación

### Personas-Clientes Service
- **Swagger UI**: http://localhost:8081/swagger-ui.html
- **API Docs**: http://localhost:8081/v3/api-docs
- **Health Check**: http://localhost:8081/actuator/health

### Cuentas-Movimientos Service
- **Swagger UI**: http://localhost:8082/swagger-ui.html
- **API Docs**: http://localhost:8082/v3/api-docs
- **Health Check**: http://localhost:8082/actuator/health

## Comunicación Entre Servicios

Los microservicios se comunican mediante:
- **HTTP REST**: Llamadas síncronas usando Feign Client
- **Puerto de comunicación**: 8081 ↔ 8082

### Ejemplo de Comunicación
```java
// En Cuentas-Movimientos Service
@FeignClient(name = "personas-clientes", url = "http://localhost:8081")
public interface PersonasClientesClient {
    @GetMapping("/api/clientes/{id}")
    ClienteDTO obtenerCliente(@PathVariable Long id);
}
```

## Estructura del Proyecto
```
banking-microservices/
├── personas-clientes-service/          # Microservicio de Personas y Clientes
│   ├── src/main/java/...
│   ├── src/main/resources/
│   ├── pom.xml
│   └── README.md
├── cuentas-movimientos-service/        # Microservicio de Cuentas y Movimientos
│   ├── src/main/java/...
│   ├── src/main/resources/
│   ├── pom.xml
│   └── README.md
├── tools/                              # Scripts y herramientas
│   ├── 1_create_schema.sql
│   ├── 2_insert_sample_data.sql
│   ├── docker-compose.yml
│   └── banking-api.postman_collection.json
├── microservices-setup.sql            # Setup completo de microservicios
└── README.md                          # Este archivo
```

## Comandos Útiles

### Construcción y Testing
```bash
# Compilar todos los servicios
cd personas-clientes-service && mvn clean compile
cd ../cuentas-movimientos-service && mvn clean compile

# Ejecutar tests
cd personas-clientes-service && mvn test
cd ../cuentas-movimientos-service && mvn test

# Generar JARs
cd personas-clientes-service && mvn clean package
cd ../cuentas-movimientos-service && mvn clean package
```

### Scripts de Inicio Rápido
```bash
# Usar el script de Windows (si está disponible)
start-microservices.bat

# O ejecutar manualmente en terminales separadas
cd personas-clientes-service && mvn spring-boot:run
cd cuentas-movimientos-service && mvn spring-boot:run
```

## Testing

### Colección de Postman
Una colección completa de Postman está disponible en:
`tools/banking-api.postman_collection.json`

### Endpoints de Prueba

#### Personas-Clientes Service (8081)
- `GET /api/personas` - Listar personas
- `POST /api/personas` - Crear persona
- `GET /api/clientes` - Listar clientes
- `POST /api/clientes` - Crear cliente

#### Cuentas-Movimientos Service (8082)
- `GET /api/cuentas` - Listar cuentas
- `POST /api/cuentas` - Crear cuenta
- `GET /api/movimientos` - Listar movimientos
- `POST /api/movimientos` - Crear movimiento

## Consideraciones de Desarrollo

### Principios Aplicados
- **Separación de Responsabilidades**: Cada microservicio maneja un dominio específico
- **Independencia**: Cada servicio tiene su propia base de datos
- **Inyección de Dependencias**: Uso de constructores en lugar de @Autowired
- **Mapeo Automático**: ModelMapper para conversiones DTO-Entity
- **Documentación Automática**: OpenAPI/Swagger para todas las APIs

### Patrones Implementados
- **Microservices Architecture**
- **Database per Service**
- **API Gateway Pattern** (consideración futura)
- **Service Discovery** (consideración futura)

## Próximos Pasos

### Mejoras Sugeridas
1. **API Gateway**: Punto único de entrada
2. **Service Discovery**: Eureka o Consul
3. **Circuit Breaker**: Resilience4j
4. **Distributed Tracing**: Sleuth + Zipkin
5. **Configuration Server**: Spring Cloud Config
6. **Container Deployment**: Docker + Kubernetes

### Monitoring y Observabilidad
- Spring Boot Actuator (ya implementado)
- Micrometer + Prometheus (futuro)
- ELK Stack para logs (futuro)

## Contacto y Soporte
Para consultas sobre el proyecto, revisar la documentación individual de cada microservicio en sus respectivos directorios.
