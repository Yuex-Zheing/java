# Personas y Clientes Microservice

## Descripción
Microservicio independiente para la gestión de personas y clientes del sistema bancario.

## Tecnologías
- Spring Boot 3.5.5
- Java 21
- MySQL 8
- ModelMapper 3.2.0
- SpringDoc OpenAPI 2.7.0

## Base de Datos
- Nombre: `banking_personas_clientes`
- Puerto: 3306
- Usuario: configurado por variable de entorno `SPRING_DATASOURCE_USERNAME` (default: zheing)
- Contraseña: configurado por variable de entorno `SPRING_DATASOURCE_PASSWORD` (default: Pa$$w0rd)

## Configuración

### Variables de Entorno
```bash
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/banking_personas_clientes?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
SPRING_DATASOURCE_USERNAME=zheing
SPRING_DATASOURCE_PASSWORD=Pa$$w0rd
SPRING_JPA_HIBERNATE_DDL_AUTO=none
SPRING_JPA_SHOW_SQL=true
SERVER_PORT=8081
TZ=America/Guayaquil
```

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
- URL: http://localhost:8081/swagger-ui.html
- API Docs: http://localhost:8081/v3/api-docs

### Endpoints Principales

#### Personas
- `GET /api/personas` - Listar todas las personas
- `GET /api/personas/{id}` - Obtener persona por ID
- `POST /api/personas` - Crear nueva persona
- `PUT /api/personas/{id}` - Actualizar persona
- `DELETE /api/personas/{id}` - Eliminar persona

#### Clientes
- `GET /api/clientes` - Listar todos los clientes
- `GET /api/clientes/{id}` - Obtener cliente por ID
- `POST /api/clientes` - Crear nuevo cliente
- `PUT /api/clientes/{id}` - Actualizar cliente
- `DELETE /api/clientes/{id}` - Eliminar cliente

### Health Check
- `GET /actuator/health` - Estado del servicio
- `GET /actuator/info` - Información del servicio
- `GET /actuator/metrics` - Métricas del servicio

## Estructura del Proyecto
```
src/
├── main/
│   ├── java/com/wquimis/demo/personasclientes/
│   │   ├── PersonasClientesApplication.java
│   │   ├── config/
│   │   │   └── ModelMapperConfig.java
│   │   ├── controller/
│   │   │   └── PersonaClienteController.java
│   │   ├── dto/
│   │   │   ├── ClienteDTO.java
│   │   │   └── PersonaDTO.java
│   │   ├── entities/
│   │   │   ├── Cliente.java
│   │   │   └── Persona.java
│   │   ├── repository/
│   │   │   ├── ClienteRepository.java
│   │   │   └── PersonaRepository.java
│   │   └── services/
│   │       ├── ClienteService.java
│   │       └── PersonaService.java
│   └── resources/
│       └── application.properties
```

## Base de Datos

### Crear esquema
```sql
CREATE DATABASE IF NOT EXISTS banking_personas_clientes;
USE banking_personas_clientes;
```

### Crear tablas (ejecutadas automáticamente con ddl-auto)
- `personas`: Información personal básica
- `clientes`: Información específica del cliente bancario

## Notas de Desarrollo
- Inyección de dependencias mediante constructores (no @Autowired)
- Uso de ModelMapper para conversión DTO-Entity
- Separación clara de responsabilidades
- Configuración mediante variables de entorno para diferentes ambientes
