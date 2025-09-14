# Catálogo Estándar de Códigos de Error

Este documento define la convención unificada de códigos de error y su uso en los tres microservicios:
- personas-clientes-service
- cuentas-movimientos-service
- onboarding-service

## 1. Formato de la Respuesta de Error
Todos los servicios exponen actualmente `ErrorDTO` con la siguiente estructura:
```
{
  "codigo": "<STRING>",
  "mensaje": "<Descripción corta orientada a máquina/operador>",
  "detalle": "<Explicación adicional orientada a usuario/diagnóstico>",
  "timestamp": 1736946920000
}
```
Reglas:
- `codigo` siempre presente salvo situaciones internas no controladas (evitar null).
- `mensaje` breve (<=120 chars) y sin stacktraces.
- `detalle` puede proveer contexto adicional (parametros, entidad, restricciones).
- `timestamp` en milisegundos epoch (long) UTC del momento de creación.

## 2. Convención de Códigos
Prefijo semántico + número de 3 dígitos. Grupos sugeridos:
- `GEN_`  Errores genéricos / infrastructura.
- `VAL_`  Validaciones de entrada / dominio.
- `NOT_`  Recursos no encontrados.
- `DUP_`  Conflictos por duplicados / unicidad.
- `ACC_`  Acceso / estado (inactivo, prohibido lógica negocio).
- `MOV_`  Movimientos.
- `CUE_`  Cuentas.
- `PER_`  Personas.
- `CLI_`  Clientes.
- `ONB_`  Flujo de onboarding.
- `EXT_`  Errores externos (WebClient) clasificados.
- `REP_`  Reportes.

Rango numérico recomendado (no rígido, facilita lectura):
- 000–099: Genéricos
- 100–199: Validaciones
- 200–299: No encontrado
- 300–399: Duplicados / conflicto
- 400–499: Estado / reglas negocio
- 500–599: Integraciones externas
- 900–999: Fallback / sin clasificación específica

## 3. Catálogo Actual (Existente + Propuesto)
| Código Actual | Reemplazo Sugerido | Contexto / Uso | HTTP Sugerido | Estado |
|---------------|--------------------|----------------|---------------|--------|
| ERR_001       | NOT_200_PERSONA / NOT_201_CUENTA / NOT_202_MOVIMIENTO (según entidad) | Recurso no encontrado | 404 | Propuesto refinar |
| ERR_002       | ACC_400_CUENTA_INACTIVA o VAL_100_TIPO_CUENTA_INVALIDO (separar casos) | Cuenta inactiva o tipo inválido | 400 / 422 | Propuesto separar |
| ERR_003       | MOV_401_SALDO_INSUFICIENTE | Saldo insuficiente retiro / reverso | 422 | Propuesto refinar |
| ERR_004       | MOV_402_NO_REVERSIBLE | Movimiento no reversible | 409/422 | Propuesto refinar |
| ERR_008       | CUE_300_DUPLICADA | Número de cuenta existente | 409 | Propuesto refinar |
| ERR_999       | GEN_000_ERROR_INTERNO | Error genérico no clasificado | 500 | Mantener como fallback |
| SUCCESS       | (sin cambio) o usar HTTP 200 sin ErrorDTO | Operación exitosa (casos puntuales) | 200 | Evitar usar ErrorDTO para éxito |

### Códigos Implementados en Código (Actualizados)
Implementados ya en controladores:

| Código | HTTP | Uso Actual |
|--------|------|-----------|
| GEN_000_ERROR_INTERNO | 500 | Fallback genérico en todos los servicios |
| NOT_201_CUENTA | 404 | Cuenta no encontrada (cuentas / reportes / movimientos) |
| NOT_202_MOVIMIENTO | 404 | Movimiento no encontrado |
| VAL_100_TIPO_CUENTA_INVALIDO | 400 | Tipo de cuenta inválido |
| ACC_401_CUENTA_INACTIVA | 422 | Operar cuenta inactiva |
| MOV_401_SALDO_INSUFICIENTE | 422 | Saldo insuficiente (retiro / reverso) |
| MOV_402_NO_REVERSIBLE | 409 | Movimiento no reversible |
| CUE_300_DUP_TIPO_NUMERO | 409 | Número de cuenta duplicado |
| ONB_300_DUP_ENTIDAD | 409 | Entidad duplicada (persona/cliente/cuenta en onboarding) |
| ONB_100_VALIDACION_FALLIDA | 400 | Validación fallida en onboarding |
| EXT_500_SERVICIO_EXTERNO | 502 | Error en servicio externo (onboarding) |
| ONB_500_ERROR_PROCESO | 500 | Error interno del proceso onboarding |

### Nuevos códigos propuestos aún no implementados
| Código | Descripción | HTTP | Caso |
|--------|-------------|------|------|
| PER_300_DUP_IDENTIFICACION | Persona con identificación existente | 409 | Creación persona |
| CLI_300_DUP_NOMBRE_USUARIO | Cliente con nombreUsuario existente | 409 | Creación cliente |
| CUE_300_DUP_TIPO_CLIENTE   | Cliente ya posee cuenta de ese tipo | 409 | Onboarding validación tipo |
| ONB_100_VALIDACION_FALLIDA | Error de validación en request onboarding | 400 | Validaciones iniciales |
| EXT_500_PERSONAS_5XX       | Falla 5xx servicio personas | 502 / 503 | WebClient personas |
| EXT_501_CUENTAS_5XX        | Falla 5xx servicio cuentas | 502 / 503 | WebClient cuentas |
| EXT_510_TIMEOUT            | Timeout al consumir servicio externo | 504 | Timeout Reactor |
| VAL_101_SALDO_INICIAL_NEG  | Saldo inicial negativo | 400 | Onboarding / cuenta |
| ACC_401_CUENTA_INACTIVA    | Intento operar cuenta inactiva | 422 | Movimiento / retiro |
| REP_200_CUENTA_NO_MOVS     | Reporte sin movimientos (informativo) | 200 | Reportes |

## 4. Mapeo HTTP vs Código (Guía)
| Categoría         | HTTP | Ejemplo Código |
|-------------------|------|----------------|
| Validación entrada| 400  | VAL_100_TIPO_CUENTA_INVALIDO |
| Dato inconsistente| 409  | CUE_300_DUP_TIPO_CLIENTE |
| Recurso faltante  | 404  | NOT_201_CUENTA |
| Regla negocio     | 422  | MOV_401_SALDO_INSUFICIENTE |
| Externo no disponible | 503 | EXT_500_PERSONAS_5XX |
| Timeout externo   | 504  | EXT_510_TIMEOUT |
| Interno genérico  | 500  | GEN_000_ERROR_INTERNO |

## 5. Recomendaciones de Evolución
1. Sustituir gradualmente códigos `ERR_xxx` por la convención semántica (feature flag o cambios faseados por servicio).
2. Mantener una capa de traducción temporal: si llega `ERR_001` aún retornar ambos campos (`codigo":"ERR_001","alias":"NOT_201_CUENTA"`) si se requiere backward compatibility (opcional).
3. Añadir pruebas que verifiquen mapeos código ↔ HTTP por endpoint crítico.
4. Documentar en cada README de servicio sólo el subconjunto que usa ese servicio y referenciar este documento central.
5. Evitar reutilizar el mismo código para entidades distintas (en lugar de `ERR_001` genérico, distinguir `NOT_200_PERSONA`, `NOT_201_CUENTA`).

## 6. Ejemplos de Respuestas Estandarizadas
### Recurso No Encontrado
```
HTTP/1.1 404 Not Found
{
  "codigo": "NOT_201_CUENTA",
  "mensaje": "Cuenta no encontrada",
  "detalle": "No existe cuenta con número 991234",
  "timestamp": 1736946920000
}
```
### Duplicado
```
HTTP/1.1 409 Conflict
{
  "codigo": "PER_300_DUP_IDENTIFICACION",
  "mensaje": "Persona ya registrada",
  "detalle": "Identificación 0102030405 ya existe",
  "timestamp": 1736946920000
}
```
### Error Externo 5xx
```
HTTP/1.1 503 Service Unavailable
{
  "codigo": "EXT_500_PERSONAS_5XX",
  "mensaje": "Fallo servicio personas",
  "detalle": "HTTP 500 - Error en servicio externo: Duplicate entry '0102030405' for key ...",
  "timestamp": 1736946920000
}
```
### Validación
```
HTTP/1.1 400 Bad Request
{
  "codigo": "VAL_101_SALDO_INICIAL_NEG",
  "mensaje": "Saldo inicial inválido",
  "detalle": "El saldo inicial -10.00 debe ser >= 0",
  "timestamp": 1736946920000
}
```

## 7. Checklist para Nuevos Códigos
- [ ] ¿Encaja en una categoría existente? Si no, evaluar creación de prefijo nuevo.
- [ ] ¿Es único y no ambiguo entre entidades? (Evitar usar un código genérico para dos dominios distintos.)
- [ ] ¿El mensaje es claro sin exponer datos sensibles?
- [ ] ¿El detalle no filtra stack-trace ni SQL bruto?
- [ ] ¿HTTP status corresponde a la semántica (no usar 500 para validaciones)?

## 8. Plan de Migración Sugerido (Opcional)
Fase 1: Introducir nuevos códigos en endpoints recién modificados, mantener antiguos donde no se ha tocado.
Fase 2: Refactor paulatino de controladores para reemplazar `ERR_xxx` por nuevos.
Fase 3: Eliminar alias / compatibilidad legacy una vez clientes actualicen consumo.

## 9. Glosario Rápido
| Prefijo | Significa |
|---------|-----------|
| GEN     | Genérico / infraestructura |
| VAL     | Validación entrada/dominio |
| NOT     | Not found |
| DUP     | Duplicados (puede preferirse entidad específica en lugar de DUP genérico) |
| PER     | Persona |
| CLI     | Cliente |
| CUE     | Cuenta |
| MOV     | Movimiento |
| ACC     | Acceso / estado lógico |
| ONB     | Flujo de onboarding |
| EXT     | Error en servicio externo |
| REP     | Reportes |

---
**Nota:** Este documento describe el estándar objetivo; la base de código actual usa aún varios `ERR_xxx`. Adoptar gradualmente según prioridad de endpoints.
