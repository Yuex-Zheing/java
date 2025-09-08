# Parametrización de Endpoints Externos

## ✅ Cambios Realizados

### 1. **Archivo `application.properties`**
```properties
# External Services Configuration
external.services.personas-clientes.base-url=${PERSONAS_CLIENTES_URL:http://localhost:8081}
external.services.cuentas-movimientos.base-url=${CUENTAS_MOVIMIENTOS_URL:http://localhost:8082}
```

### 2. **Clase de Configuración `ExternalServicesConfig.java`**
- **Propósito**: Centralizar la configuración de URLs externas
- **Características**:
  - Usa `@ConfigurationProperties` para mapear automáticamente propiedades
  - Métodos helper para construir URLs completas con `/api` prefix
  - Valores por defecto para desarrollo local
  - Estructura anidada para organizar servicios

### 3. **Servicios Actualizados**
#### URLs Parametrizadas:
- **Personas**: `${base-url}/api/personas`
- **Clientes**: `${base-url}/api/clientes` 
- **Cuentas**: `${base-url}/api/cuentas`
- **Movimientos**: `${base-url}/api/movimientos`

### 4. **Aplicación Principal**
- Agregado `@EnableConfigurationProperties` para habilitar binding automático

## 🎯 **Beneficios**

### **Flexibilidad de Configuración**
```bash
# Desarrollo local (default)
PERSONAS_CLIENTES_URL=http://localhost:8081
CUENTAS_MOVIMIENTOS_URL=http://localhost:8082

# Producción
PERSONAS_CLIENTES_URL=https://personas-api.empresa.com
CUENTAS_MOVIMIENTOS_URL=https://cuentas-api.empresa.com

# Testing
PERSONAS_CLIENTES_URL=http://personas-test:8081
CUENTAS_MOVIMIENTOS_URL=http://cuentas-test:8082
```

### **Gestión Centralizada**
- Una sola clase para manejar todas las URLs externas
- Fácil mantenimiento y actualización
- URLs construidas automáticamente con prefijos correctos

### **Configuración por Ambiente**
- Variables de entorno para diferentes ambientes
- Valores por defecto para desarrollo
- Compatible con Docker, Kubernetes, etc.

## 📋 **Uso**

### **Variables de Entorno**
```bash
# Windows
set PERSONAS_CLIENTES_URL=http://mi-servidor:8081
set CUENTAS_MOVIMIENTOS_URL=http://mi-servidor:8082

# Linux/Mac
export PERSONAS_CLIENTES_URL=http://mi-servidor:8081
export CUENTAS_MOVIMIENTOS_URL=http://mi-servidor:8082
```

### **Docker Compose**
```yaml
services:
  onboarding-service:
    environment:
      - PERSONAS_CLIENTES_URL=http://personas-service:8081
      - CUENTAS_MOVIMIENTOS_URL=http://cuentas-service:8082
```

### **Kubernetes ConfigMap**
```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: onboarding-config
data:
  PERSONAS_CLIENTES_URL: "http://personas-service:8081"
  CUENTAS_MOVIMIENTOS_URL: "http://cuentas-service:8082"
```

El servicio ahora es completamente configurable y puede adaptarse a cualquier ambiente sin cambios de código.
