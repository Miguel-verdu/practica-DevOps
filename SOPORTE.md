# SOPORTE.md - Guía de Diagnóstico y Resolución de Incidencias

**Versión:** 1.0  
**Última actualización:** Febrero 11, 2026  
**Aplicación:** Sistema de Gestión de Tickets  

---

## 📋 Tabla de Contenidos

1. [Introducción](#introducción)
2. [Arquitectura de Logging](#arquitectura-de-logging)
3. [Checklist de Diagnóstico](#checklist-de-diagnóstico)
4. [Endpoint de Diagnóstico](#endpoint-de-diagnóstico)
5. [Incidencias Simuladas y Resolución](#incidencias-simuladas-y-resolución)
6. [Procedimientos Comunes](#procedimientos-comunes)
7. [Archivos de Log](#archivos-de-log)

---

## Introducción

Este documento proporciona una guía completa para diagnosticar y resolver problemas en el sistema de gestión de tickets. El sistema implementa un robusto sistema de logging con traceabilidad completa de operaciones.

### Características de Logging Implementadas

✅ **Rastreo de Operaciones:** Cada operación tiene un ID único  
✅ **Contexto MDC:** Información adicional en cada log  
✅ **Sin Datos Sensibles:** Enmascaramiento de información personal  
✅ **Múltiples Destinos:** Consola, archivo general, archivo de errores  
✅ **Rotación de Logs:** Gestión automática de tamaño y antigüedad  
✅ **Niveles por Módulo:** Control granular del nivel de logging  

---

## Arquitectura de Logging

### Componentes Principales

```
┌─────────────────────────────────────────────────────┐
│         Aplicación (Controllers, Services)           │
├─────────────────────────────────────────────────────┤
│                    SLF4J Logger                       │
├─────────────────────────────────────────────────────┤
│         Logback (Implementación de SLF4J)            │
├─────────────────────────────────────────────────────┤
│  Console  │  File  │  Error File  │  App File        │
└─────────────────────────────────────────────────────┘
```

### Flujo de Información

1. **Aplicación genera log:** Controller, Service o Utility llama a `logger.info/warn/error(...)`
2. **LoggingUtil añade contexto:** Se genera ID de operación único y se almacena en MDC
3. **Logback procesa:** Formatea el mensaje según la configuración
4. **Se distribuye a múltiples destinos:**
   - **Consola:** Vista en tiempo real durante desarrollo
   - **application.log:** Todos los logs de la aplicación
   - **errors.log:** Solo WARN y ERROR
   - **Spring logs:** Logs de frameworks Spring Framework

---

## Checklist de Diagnóstico

Use esta lista para diagnosticar problemas de forma sistemática:

### 🔍 Verificación Básica

- [ ] **Aplicación iniciada correctamente**
  - Verificar en consola que Spring Boot levanta sin errores
  - Buscar: `Started DemoApplication`
  
- [ ] **Base de datos conectada**
  - Ejecutar: `curl http://localhost:8080/admin/diagnostico/health`
  - Esperar: `"estado": "SALUDABLE"`

- [ ] **Puertos disponibles**
  - Aplicación: puerto 8080
  - H2 Console: puerto 8080/h2-console
  - Verificar con: `netstat -ano | findstr 8080` (Windows)

### 📊 Verificación de Logging

- [ ] **Archivo de logs existe**
  - Ubicación: `logs/application.log`
  - Ubicación alternativa: Definida en `logging.file.name`

- [ ] **Rotación de logs activa**
  - Archivos con patrón: `application-2026-02-11.0.log`
  - Tamaño máximo: 10MB por archivo
  - Historico: 10 días

- [ ] **Niveles de log correctos**
  - Desarrollo: DEBUG en com.example.demo
  - Producción: INFO/WARN
  - Verificar en `logback-spring.xml`

### 🔧 Verificación de Funcionalidad

- [ ] **Endpoints de tickets funcionan**
  ```bash
  # Listar
  curl -X GET http://localhost:8080/tickets
  
  # Crear
  curl -X POST http://localhost:8080/tickets \
    -H "Content-Type: application/json" \
    -d '{"titulo":"Test","descripcion":"Test ticket","estado":"ABIERTO"}'
  
  # Obtener ticket específico
  curl -X GET http://localhost:8080/tickets/1
  ```

- [ ] **IDs de operación visibles en logs**
  - Buscar en application.log: `[operacion_id]` debe estar presente
  - Ejemplo: `[12345-abcd-6789] INFO ...`

- [ ] **Manejo de excepciones funciona**
  - Enviar dato inválido: `curl -X POST ... -d '{"titulo":"short"}'`
  - Verificar respuesta con errores validados
  - Comprobar log en application.log

### 📈 Verificación de Rendimiento

- [ ] **Memoria en niveles normales**
  - Ejecutar: `curl http://localhost:8080/admin/diagnostico`
  - Comprobar: `"porcentaje_uso" < 80%`

- [ ] **Sin memory leaks evidentes**
  - Monitorear: `logs/application.log`
  - Buscar: excepciones de OutOfMemory

- [ ] **Tiempo de respuesta aceptable**
  - GET /tickets: < 100ms
  - POST /tickets: < 200ms

---

## Endpoint de Diagnóstico

La aplicación proporciona dos endpoints para monitoreo:

### GET /admin/diagnostico

**Propósito:** Diagnóstico completo del sistema

**Respuesta ejemplo:**
```json
{
  "timestamp": "2026-02-11T10:30:45.123",
  "diagnostico": {
    "conexion_bd": {
      "estado": "CONECTADA",
      "tipo": "jdbc:h2:mem:testdb"
    },
    "memoria": {
      "total_mb": 512,
      "usada_mb": 256,
      "libre_mb": 256,
      "porcentaje_uso": "50.00%",
      "estado": "✓ NORMAL"
    },
    "configuracion": {
      "spring_version": "3.x.x",
      "java_version": "17.x.x",
      "profile_activo": "[]",
      "puerto": "8080"
    },
    "logs": {
      "nivel_log": "INFO",
      "nivel_aplicacion": "DEBUG",
      "archivo_log": "logs/application.log"
    }
  }
}
```

### GET /admin/diagnostico/health

**Propósito:** Health check rápido

**Respuesta exitosa:**
```json
{
  "timestamp": "2026-02-11T10:30:45.123",
  "estado": "SALUDABLE",
  "codigo": "200"
}
```

**Respuesta con problemas:**
```json
{
  "timestamp": "2026-02-11T10:30:45.123",
  "estado": "PROBLEMAS",
  "codigo": "503"
}
```

---

## Incidencias Simuladas y Resolución

### 🚨 Incidencia 1: Error de Validación en Creación de Ticket

**Escenario:** Usuario intenta crear un ticket con título muy corto

**Síntomas:**
- Código de respuesta: `400 Bad Request`
- Mensaje: "Error de validación en los datos proporcionados"
- Logs en application.log:
  ```
  [operacion_123-abc] WARN ValidationException: 2 errores de validación detectados
  [operacion_123-abc] DEBUG   - Campo 'titulo': El título debe tener entre 5 y 100 caracteres
  [operacion_123-abc] DEBUG   - Campo 'descripcion': La descripción debe tener al menos 10 caracteres
  ```

**Pasos para Reproducir:**
```bash
curl -X POST http://localhost:8080/tickets \
  -H "Content-Type: application/json" \
  -d '{"titulo":"No","descripcion":"Corta"}'
```

**Diagnóstico:**
1. Revisar el error retornado en JSON:
   ```json
   {
     "timestamp": "2026-02-11T10:35:20.456",
     "status": 400,
     "mensaje": "Error de validación en los datos proporcionados",
     "errores": {
       "titulo": "El título debe tener entre 5 y 100 caracteres",
       "descripcion": "La descripción debe tener al menos 10 caracteres"
     }
   }
   ```
2. Buscar en logs: `grep "operacion_123-abc" logs/application.log`
3. Identificar campos problemáticos

**Resolución:**
```bash
# Opción 1: Enviar datos válidos
curl -X POST http://localhost:8080/tickets \
  -H "Content-Type: application/json" \
  -d '{
    "titulo": "Título válido con suficientes caracteres",
    "descripcion": "Esta es una descripción válida que cumple con los requisitos mínimos",
    "estado": "ABIERTO"
  }'

# Opción 2: Consultar restricciones en Ticket.java
# - titulo: min 5, max 100 caracteres
# - descripcion: min 10, max 1000 caracteres
# - estado: debe ser ABIERTO, EN_PROGRESO, CERRADO, RECHAZADO
```

**Validación de Resolución:**
- ✓ Ticket creado exitosamente (código 201)
- ✓ Log muestra: `[operacion_xxx] INFO Ticket creado exitosamente. ID: 1`
- ✓ GET /tickets/1 retorna el ticket

---

### 🚨 Incidencia 2: Ticket No Encontrado al Actualizar

**Escenario:** Usuario intenta actualizar un ticket que no existe

**Síntomas:**
- Código de respuesta: `500 Internal Server Error`
- Logs en errors.log:
  ```
  [operacion_456-def] ERROR PUT /tickets/999/estado - Error: Ticket no encontrado con ID: 999
  ```

**Pasos para Reproducir:**
```bash
# Intentar actualizar ticket inexistente
curl -X PUT "http://localhost:8080/tickets/999/estado?nuevoEstado=EN_PROGRESO"
```

**Diagnóstico:**
1. Buscar en logs/application.log:
   ```
   [operacion_456-def] INFO PUT /tickets/999/estado - Actualizando estado a: EN_PROGRESO
   [operacion_456-def] WARN TicketService:125 - Ticket no encontrado para actualizar. ID: 999
   [operacion_456-def] ERROR PUT /tickets/999/estado - Error: Ticket no encontrado con ID: 999
   ```
2. Confirmar que el ticket no existe:
   ```bash
   curl -X GET http://localhost:8080/tickets/999
   # Retorna 404
   ```
3. Consultar ID de operación para rastreo completo

**Resolución:**

**Opción A: Obtener lista de tickets válidos**
```bash
curl -X GET http://localhost:8080/tickets | jq '.[] | {id, titulo}'
```

**Opción B: Crear un ticket primero**
```bash
curl -X POST http://localhost:8080/tickets \
  -H "Content-Type: application/json" \
  -d '{
    "titulo": "Ticket de prueba",
    "descripcion": "Para actualizar estado después",
    "estado": "ABIERTO"
  }'

# Luego actualizar el ticket con ID retornado (ej: 1)
curl -X PUT "http://localhost:8080/tickets/1/estado?nuevoEstado=EN_PROGRESO"
```

**Opción C: Implementar validación mejorada (código)**
Actualizar `TicketService.actualizarEstado()`:
```java
// Actualmente lanza IllegalArgumentException
// Sugerencia: Cambiar a lanzar una excepción custom con código 404
```

**Validación de Resolución:**
- ✓ Actualización exitosa (código 200)
- ✓ Log muestra cambio de estado: `ID: 1, ABIERTO -> EN_PROGRESO`
- ✓ GET /tickets/1 muestra estado actualizado

---

### 🚨 Incidencia 3: Degradación de Rendimiento / Memoria

**Escenario:** Aplicación se ralentiza después de crear muchos tickets

**Síntomas:**
- Respuestas lentas (>1000ms)
- Memoria creciente en application.log
- En logs: `"porcentaje_uso": "85%"`

**Pasos para Reproducir:**
```bash
# Script para crear 1000 tickets rápidamente
for i in {1..1000}; do
  curl -X POST http://localhost:8080/tickets \
    -H "Content-Type: application/json" \
    -d "{
      \"titulo\": \"Ticket número $i titulo largo para prueba\",
      \"descripcion\": \"Descripción del ticket número $i que contiene información de prueba del sistema\",
      \"estado\": \"ABIERTO\"
    }" &
done
wait
```

**Diagnóstico:**

1. **Verificar memoria:**
   ```bash
   curl http://localhost:8080/admin/diagnostico | jq '.diagnostico.memoria'
   ```
   Esperar respuesta como:
   ```json
   {
     "total_mb": 512,
     "usada_mb": 450,
     "libre_mb": 62,
     "porcentaje_uso": "87.89%",
     "estado": "⚠ ALTO"
   }
   ```

2. **Revisar logs para memory leaks:**
   ```bash
   grep -i "OutOfMemory\|gc\|memory" logs/application.log
   ```

3. **Analizar tiempo de respuesta:**
   ```bash
   time curl -X GET http://localhost:8080/tickets
   ```

4. **Verificar cantidad de registros:**
   ```bash
   curl -X GET http://localhost:8080/tickets | jq 'length'
   ```

**Causa Raíz Probable:**
- Base de datos está cargando todos los tickets en memoria (H2 en-memory)
- No hay paginación implementada
- Logs no se están truncando

**Resolución (Opciones):**

**Opción A: Implementar Paginación** (RECOMENDADO)
En `HolaController.java`:
```java
@GetMapping
public ResponseEntity<Page<Ticket>> listarTickets(
    @RequestParam(defaultValue = "0") int pagina,
    @RequestParam(defaultValue = "20") int tamaño) {
    Page<Ticket> tickets = ticketService.obtenerPaginados(
        PageRequest.of(pagina, tamaño));
    return ResponseEntity.ok(tickets);
}
```

**Opción B: Limpiar tickets antiguos**
```bash
# Usar DELETE /tickets/{id} para tickets cerrados antiguos
curl -X DELETE http://localhost:8080/tickets/1
curl -X DELETE http://localhost:8080/tickets/2
# ... etc
```

**Opción C: Aumento de memoria JVM**
En `application.properties` o al iniciar:
```bash
java -Xmx1024m -Xms512m -jar demo-0.0.1-SNAPSHOT.jar
```

**Opción D: Implementar búsqueda con filtros**
En `TicketService`:
```java
public List<Ticket> buscarPorEstado(EstadoTicket estado) {
    return ticketRepository.findByEstado(estado);
}
```

**Validación de Resolución:**

Después de implementar solución:
```bash
# Verificar memoria
curl http://localhost:8080/admin/diagnostico | jq '.diagnostico.memoria.porcentaje_uso'
# Debe estar < 75%

# Verificar respuesta rápida
time curl -X GET http://localhost:8080/tickets?pagina=0&tamaño=20
# Debe ser < 100ms

# Revisar logs
tail -f logs/application.log
# No debe haber warnings de memoria
```

---

## Procedimientos Comunes

### Limpiar Archivos de Log

```bash
# Eliminar archivo principal
rm logs/application.log

# Eliminar archivo de errores
rm logs/errors.log

# La aplicación los recreará automáticamente
```

### Cambiar Nivel de Logging

**Opción 1: Archivo `application.properties`**
```properties
# Para debugging intenso:
logging.level.com.example.demo=TRACE

# Para producción:
logging.level.com.example.demo=WARN
```

**Opción 2: Archivo `logback-spring.xml`**
```xml
<logger name="com.example.demo" level="DEBUG" additivity="false">
    <appender-ref ref="CONSOLE"/>
    <appender-ref ref="APP_FILE"/>
</logger>
```

### Rastrear una Operación Específica

```bash
# En application.log, buscar por operación_id
grep "operacion_id_especifico" logs/application.log

# Ejemplo: obtener todas las operaciones de un usuario
grep "\[user_id-hash\]" logs/application.log
```

### Monitoreo en Tiempo Real

```bash
# Ver logs en vivo (Linux/Mac)
tail -f logs/application.log

# Ver logs en vivo (Windows PowerShell)
Get-Content logs/application.log -Wait -Tail 20
```

---

## Archivos de Log

| Archivo | Contenido | Rotación | Máximo |
|---------|-----------|----------|--------|
| `application.log` | Todos los logs de aplicación | Por día/tamaño | 10 días, 100MB total |
| `errors.log` | Solo WARN y ERROR | Por día/tamaño | 10 días, sin límite total |
| `spring.log` | Sistema (fallback) | Rotación manual | Por defecto |

### Ubicaciones Posibles

```
# Configuración por defecto
logs/application.log

# Variable de entorno LOG_PATH
C:\logs\application.log  # Windows
/var/log/application.log # Linux
```

---

## 📞 Contactar a Soporte

Si después de seguir estos pasos no resuelves el problema:

1. **Recopila información:**
   - Salida completa de `/admin/diagnostico`
   - Últimas líneas de `logs/application.log` y `logs/errors.log`
   - Pasos exactos para reproducir el problema

2. **Crea un ticket con:**
   - Descripción clara del problema
   - Mensajes de error exactos
   - Información del diagnóstico
   - Logs relevantes

3. **Importante:**
   - ⚠️ No incluyas datos sensibles en los logs
   - ✓ Los logs ya están enmascarados
   - ✓ Los IDs de operación facilitan el rastreo

---

**Última actualización:** Febrero 11, 2026  
**Autor:** Equipo de Desarrollo  
**Sistema:** Gestión de Tickets v1.0
