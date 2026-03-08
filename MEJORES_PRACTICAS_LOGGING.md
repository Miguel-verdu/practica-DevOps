# MEJORES_PRACTICAS_LOGGING.md - Guía de Logging Seguro

**Versión:** 1.0  
**Última actualización:** Febrero 11, 2026  
**Propósito:** Estándares de logging sin exponer datos sensibles

---

## 📋 Tabla de Contenidos

1. [Principios Fundamentales](#principios-fundamentales)
2. [Qué Registrar](#qué-registrar)
3. [Qué NO Registrar](#qué-no-registrar)
4. [Patrones de Logging Seguros](#patrones-de-logging-seguros)
5. [Enmascaramiento de Datos](#enmascaramiento-de-datos)
6. [Niveles de Log](#niveles-de-log)
7. [Rastrabilidad con IDs de Operación](#rastrabilidad-con-ids-de-operación)

---

## Principios Fundamentales

### 🔒 Regla # 1: Nunca registres secretos

```java
// ❌ INCORRECTO
logger.info("Usuario: {}, Contraseña: {}", usuario.getEmail(), usuario.getPassword());
logger.info("API Key: {}", apiKey);
logger.debug("Token: {}", jwtToken);

// ✅ CORRECTO
logger.info("Usuario autenticado: {}", usuario.getId());
logger.info("Autenticación exitosa para usuario ID: {}", usuario.getId());
```

### 🔒 Regla # 2: Identifica, no expongas

```java
// ❌ INCORRECTO
logger.info("Email del cliente: {}", "juan@email.com");
logger.warn("Teléfono bloqueado: {}", "+34612345678");

// ✅ CORRECTO
logger.info("Email enmascarado: {}", LoggingUtil.enmascarar("juan@email.com"));
logger.warn("Teléfono bloqueado: {}", LoggingUtil.enmascarar("+34612345678"));
```

### 🔒 Regla # 3: Incluye contexto de rastrabilidad

```java
// ❌ INCORRECTO
logger.info("Ticket creado");

// ✅ CORRECTO
String operacionId = LoggingUtil.registrarInicio("crear_ticket");
logger.info("{} - Ticket creado con ID: {}, Estado: {}", 
            operacionId, ticket.getId(), ticket.getEstado());
```

---

## Qué Registrar

### ✅ Siempre registra:

1. **Inicio de operaciones importantes**
   ```java
   String idOp = LoggingUtil.registrarInicio("transferencia_fondos");
   logger.info("{} - Iniciando transferencia", idOp);
   ```

2. **Cambios de estado**
   ```java
   logger.info("{} - Estado actualizado: {} -> {}", 
               idOp, estadoAnterior, estadoNuevo);
   ```

3. **Acceso a recursos críticos**
   ```java
   logger.info("{} - Accediendo a base de datos para usuario ID: {}", 
               idOp, userId);
   ```

4. **Errors y excepciones**
   ```java
   logger.error("{} - Error al procesar: {}", idOp, e.getMessage());
   logger.debug("{} - Stack trace:", idOp, e);
   ```

5. **Acciones administrativas**
   ```java
   logger.warn("{} - Usuario eliminado. ID: {}", idOp, userId);
   ```

6. **Métricas de rendimiento**
   ```java
   long inicio = System.currentTimeMillis();
   // ... operación ...
   long duracion = System.currentTimeMillis() - inicio;
   logger.info("{} - Operación completada en {}ms", idOp, duracion);
   ```

### Ejemplos Completos

#### Ejemplo 1: Crear Recurso
```java
public Ticket crearTicket(Ticket ticket) {
    String idOp = LoggingUtil.registrarInicio("crear_ticket");
    logger.info("{} - Validando datos del ticket", idOp);
    
    try {
        // Registrar solo metadatos, no el contenido sensible
        logger.debug("{} - Longitud de título: {} caracteres", 
                     idOp, ticket.getTitulo().length());
        
        Ticket guardado = ticketRepository.save(ticket);
        
        logger.info("{} - Ticket creado exitosamente. ID: {}, Estado: {}",
                   idOp, guardado.getId(), guardado.getEstado());
        
        return guardado;
    } catch (Exception e) {
        logger.error("{} - Error al crear ticket: {}", 
                     idOp, e.getMessage());
        throw e;
    } finally {
        LoggingUtil.limpiarContexto();
    }
}
```

#### Ejemplo 2: Actualizar Recurso
```java
public Ticket actualizarTicket(Long id, TicketUpdates updates) {
    String idOp = LoggingUtil.registrarInicio("actualizar_ticket");
    logger.info("{} - Actualizando ticket ID: {}", idOp, id);
    
    try {
        Ticket ticket = obtenerOThrow(id);
        String estadoAnterior = ticket.getEstado().toString();
        
        // Registrar cambios sin exponer valores
        if (updates.getEstado() != null) {
            logger.debug("{} - Cambio de estado pendiente", idOp);
            ticket.setEstado(updates.getEstado());
        }
        
        Ticket actualizado = ticketRepository.save(ticket);
        logger.info("{} - Ticket actualizado. Estado: {} -> {}", 
                   idOp, estadoAnterior, actualizado.getEstado());
        
        return actualizado;
    } catch (Exception e) {
        logger.error("{} - Error al actualizar: {}", idOp, e.getMessage());
        throw e;
    } finally {
        LoggingUtil.limpiarContexto();
    }
}
```

---

## Qué NO Registrar

### ❌ NUNCA registres:

| Dato | Razón | Alternativa |
|------|-------|-------------|
| Contraseñas | Secreto crítico | Usar hash de contraseña o solo "autenticación exitosa" |
| Tokens JWT | Secreto de sesión | Usar ID de sesión enmascarado |
| Números de tarjeta | PCI-DSS compliance | Usar últimos 4 dígitos enmascarados |
| SSN / DNI completo | Privacidad personal | Usar enmascarado: `XXX-XX-1234` |
| Direcciones IP privadas innecesarias | Privacidad de red | Solo si es crítico para debugging |
| Contenido de archivos privados | Privacidad | Solo mencionar que se procesó |
| Datos de ubicación exacta | GDPR compliance | Usar región general |
| Datos médicos / financieros | Confidencialidad | Usar ID anonimizado |

### Ejemplos de Qué NO Hacer

```java
// ❌ CRÍTICO: Nunca en logs
logger.info("Contraseña: {}", password);
logger.debug("Token: {}", jwtToken);
logger.info("Tarjeta: {}", cardNumber);
logger.warn("SSN: {}", ssn);

// ❌ Evitar: Demasiado detalle
logger.debug("Contenido completo del archivo: {}", fileContent);
logger.debug("Datos del paciente: {}", patientData);

// ❌ Evitar: Información de user-agent, IP completa
logger.info("User-Agent: {}", request.getHeader("User-Agent"));
logger.info("IP Cliente: {}", request.getRemoteAddr());

// ❌ Evitar: Stack traces en producción
logger.info("Error:", exception);  // Stack trace completo
```

---

## Patrones de Logging Seguros

### Patrón 1: Identificación Segura

```java
// Para usuarios
public void procesarUsuario(User user) {
    String idOp = LoggingUtil.registrarInicio("procesar_usuario");
    
    // ✅ CORRECTO: Usar ID, no email
    logger.info("{} - Procesando usuario ID: {}", idOp, user.getId());
    
    // Si necesitas el email:
    // ✅ CORRECTO: Enmascarado
    logger.debug("{} - Email: {}", idOp, LoggingUtil.enmascarar(user.getEmail()));
}
```

### Patrón 2: Cambios de Estado

```java
public void cambiarEstado(Long resourceId, EstadoAntiguo, EstadoNuevo) {
    String idOp = LoggingUtil.registrarInicio("cambiar_estado");
    
    logger.info("{} - Cambio de estado para recurso ID: {}", idOp, resourceId);
    logger.info("{} - {} -> {}", idOp, estadoAntiguo, estadoNuevo);
}
```

### Patrón 3: Operaciones con Datos Sensibles

```java
public void procesarPago(Payment payment) {
    String idOp = LoggingUtil.registrarInicio("procesar_pago");
    
    try {
        // ❌ INCORRECTO
        // logger.info("{} - Procesando pago: {}", idOp, payment);
        
        // ✅ CORRECTO: Solo metadatos
        logger.info("{} - Procesando pago ID: {}", idOp, payment.getId());
        logger.debug("{} - Monto: {} (moneda: EUR)", idOp, payment.getAmount());
        
        // Nunca:
        // logger.debug("Card: {}", payment.getCardNumber());
        // logger.debug("CVV: {}", payment.getCvv());
        
        procesarPagoInterno(payment);
        
        logger.info("{} - Pago completado exitosamente", idOp);
        
    } catch (PaymentException e) {
        logger.error("{} - Error en procesamiento: {}", idOp, e.getMessage());
        // ✅ CORRECTO: Sin detalles de tarjeta
    }
}
```

### Patrón 4: Auditoría de Acceso

```java
public void auditarAcceso(Usuario usuario, Recurso recurso, Accion accion) {
    String idOp = LoggingUtil.registrarInicio("acceso_recurso");
    LoggingUtil.establecerUsuario(usuario.getId());
    
    try {
        logger.warn("{} - Acceso: {} a {} realizado por usuario ID: {}", 
                    idOp, accion, recurso.getId(), usuario.getId());
        
        // Registrar resultado
        if (permitido) {
            logger.info("{} - Acceso concedido", idOp);
        } else {
            logger.warn("{} - Acceso denegado", idOp);
        }
    } finally {
        LoggingUtil.limpiarContexto();
    }
}
```

---

## Enmascaramiento de Datos

### Función `LoggingUtil.enmascarar()`

```java
public static String enmascarar(String valor) {
    if (valor == null || valor.length() <= 2) {
        return "***";
    }
    return valor.substring(0, 2) + "*".repeat(Math.max(0, valor.length() - 4)) + 
           (valor.length() > 2 ? valor.substring(valor.length() - 2) : "");
}
```

### Ejemplos de Enmascaramiento

| Entrada | Salida |
|---------|--------|
| `juan.perez@email.com` | `ju***..**m` |
| `+34612345678` | `+3***5678` |
| `4532-1111-2222-3333` | `45***.***3333` |
| `ABC123DEF456` | `AB***.***456` |
| `password123` | `pa***.***23` |

### Uso en Logs

```java
String email = usuario.getEmail();
String emailEnmascarado = LoggingUtil.enmascarar(email);

logger.info("Usuario con email: {}", emailEnmascarado);
// Output: Usuario con email: ju***..**m
```

---

## Niveles de Log

### DEBUG (Desarrollo)
Información detallada para debugging. **SOLO EN DESARROLLO**

```java
// Aceptable en DEBUG:
logger.debug("{} - Longitud del título: {} caracteres", idOp, title.length());
logger.debug("{} - Validaciones pasadas: email OK, datos OK", idOp);
logger.debug("{} - Stack trace:", exception);
```

### INFO (Información General)
Eventos importantes para el funcionamiento normal.

```java
logger.info("{} - Aplicación iniciada correctamente", idOp);
logger.info("{} - Ticket creado. ID: {}", idOp, ticket.getId());
logger.info("{} - Base de datos conectada", idOp);
```

### WARN (Advertencia)
Eventos que requieren atención pero no son críticos.

```java
logger.warn("{} - Ticket no encontrado. ID: {}", idOp, ticketId);
logger.warn("{} - Memoria en {}%", idOp, porcentaje);
logger.warn("{} - Usuario eliminado. ID: {}", idOp, userId);
```

### ERROR (Error)
Operación fallida pero la aplicación continúa.

```java
logger.error("{} - Error al guardar ticket: {}", idOp, e.getMessage());
logger.error("{} - Conexión a BD fallida", idOp);
```

### Configuración por Perfil

```properties
# src/main/resources/application.properties (Desarrollo)
logging.level.com.example.demo=DEBUG
logging.level.org.springframework=INFO

# application-prod.properties (Producción)
logging.level.com.example.demo=WARN
logging.level.org.springframework=WARN
```

---

## Rastrabilidad con IDs de Operación

### ¿Por qué IDs de Operación?

Permite seguir una solicitud a través de múltiples componentes:

```
Cliente solicita: POST /tickets
    ↓ [operacion_123]
HolaController.crearTicket() 
    ↓ [operacion_123]
TicketService.crearTicket()
    ↓ [operacion_123]
TicketRepository.save()
    ↓ [operacion_123]
Base de Datos
    ↓ [operacion_123]
Respuesta al cliente

Logs:
[operacion_123] INFO TicketController - POST /tickets
[operacion_123] INFO TicketService - Validando ticket
[operacion_123] INFO TicketService - Guardando en BD
[operacion_123] INFO TicketRepository - INSERT ...
[operacion_123] INFO TicketController - Respuesta 201
```

### Implementación

```java
// 1. Generar ID en punto de entrada (Controller)
@PostMapping
public ResponseEntity<Ticket> crearTicket(@RequestBody Ticket ticket) {
    String idOp = LoggingUtil.registrarInicio("crear_ticket");
    logger.info("{} - Inicio de request POST /tickets", idOp);
    
    try {
        // 2. Se propaga automáticamente en MDC
        Ticket resultado = ticketService.crearTicket(ticket);
        logger.info("{} - Response 201", idOp);
        return ResponseEntity.status(201).body(resultado);
    } finally {
        LoggingUtil.limpiarContexto();
    }
}

// 3. En service, el MDC ya contiene operacion_id
@Service
public class TicketService {
    
    public Ticket crearTicket(Ticket ticket) {
        // No necesita pasarlo, está en MDC
        logger.info("Guardando ticket"); 
        // Se loguea como: [operacion_123] INFO Guardando ticket
        
        return ticketRepository.save(ticket);
    }
}
```

### Formato de Log con Operacion_id

```xml
<!-- logback-spring.xml -->
<property name="LOG_PATTERN" 
          value="%d{HH:mm:ss.SSS} [%X{operacion_id}] %-5level %logger{36} - %msg%n"/>
```

Resultado:
```
10:35:20.123 [12345-abcd-6789] INFO  TicketService - Ticket creado
10:35:20.456 [12345-abcd-6789] DEBUG TicketService - Email: ju***..**m
10:35:20.789 [12345-abcd-6789] INFO  TicketController - Response 201
```

### Búsqueda en Logs

```bash
# Buscar toda una operación
grep "operacion_123" logs/application.log

# Ver solo errores de una operación
grep "operacion_123" logs/errors.log

# Timeline de una operación
grep "operacion_123" logs/application.log | awk '{print $1, $2, $NF}'
```

---

## Checklist de Implementación

Cuando implementes logging en una nueva función:

```
- [ ] Generar ID de operación con LoggingUtil.registrarInicio()
- [ ] Registrar inicio de función
- [ ] Logs en DEBUG para valores derivados (longitud, conteo)
- [ ] Logs en INFO para eventos importantes (creación, cambio de estado)
- [ ] Logs en WARN para eventos inusales (recurso no encontrado)
- [ ] Logs en ERROR solo para excepciones reales
- [ ] NO registrar secretos, contraseñas, tokens
- [ ] NO registrar números completos de tarjeta, SSN, etc.
- [ ] Enmascarar emails, teléfonos si es necesario
- [ ] Limpiar MDC en finally para evitar leaks
- [ ] Verificar que no haya stack traces completos en respuestas HTTP
- [ ] Probar que los logs rotan correctamente
```

---

## 📞 Preguntas Frecuentes

### P: ¿Puedo registrar IDs de usuario?
**R:** Sí, los IDs (números) son seguros. Evita registrar usernames o emails completos.

### P: ¿Qué pasa si olvido limpiar el MDC?
**R:** Podría contaminar logs de siguientes requests con contexto de uno anterior. Siempre limpia en `finally`.

### P: ¿Cómo debuggeo si no puedo ver valores sensibles?
**R:** Usa desarrollo local, registra en DEBUG la longitud/tipo del dato, y usa debugger de IDE.

### P: ¿Es lento registrar tantos logs?
**R:** No significativamente. Logback es muy optimizado. El costo es menor que el beneficio para diagnóstico.

### P: ¿Dónde pongo los logs de terceros?
**R:** Usa `logback-spring.xml` para contolar el nivel de cada package. Busca para obtener ejemplos.

---

**Última actualización:** Febrero 11, 2026  
**Autor:** Equipo de Seguridad y DevOps
