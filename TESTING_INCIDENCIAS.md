# TESTING_INCIDENCIAS.md - Guía de Simulación y Testing

**Versión:** 1.0  
**Última actualización:** Febrero 11, 2026  
**Propósito:** Reproducir y validar las 3 incidencias simuladas

---

## 📋 Índice

1. [Incidencia 1: Error de Validación](#incidencia-1-error-de-validación)
2. [Incidencia 2: Recurso No Encontrado](#incidencia-2-recurso-no-encontrado)
3. [Incidencia 3: Degradación de Memoria](#incidencia-3-degradación-de-memoria)

---

## Incidencia 1: Error de Validación

### Descripción
El servidor rechaza solicitudes con datos inválidos, retornando validaciones con máximo detalle en los logs.

### Estados HTTP Esperados
- `400 Bad Request` - Datos inválidos

### Casos de Prueba

#### Test 1.1: Título muy corto
```bash
curl -X POST http://localhost:8080/tickets \
  -H "Content-Type: application/json" \
  -d '{
    "titulo": "Abc",
    "descripcion": "Una descripción válida con suficientes caracteres"
  }'
```

**Respuesta esperada:**
```json
{
  "timestamp": "2026-02-11T10:45:30.123",
  "status": 400,
  "mensaje": "Error de validación en los datos proporcionados",
  "errores": {
    "titulo": "El título debe tener entre 5 y 100 caracteres"
  }
}
```

**Logs esperados en application.log:**
```
[operacion_xxx] WARN ValidationException: 1 errores de validación detectados
[operacion_xxx] DEBUG   - Campo 'titulo': El título debe tener entre 5 y 100 caracteres
```

#### Test 1.2: Descripción vacía
```bash
curl -X POST http://localhost:8080/tickets \
  -H "Content-Type: application/json" \
  -d '{
    "titulo": "Título válido con suficientes caracteres",
    "descripcion": ""
  }'
```

**Respuesta esperada:**
```json
{
  "timestamp": "2026-02-11T10:45:35.123",
  "status": 400,
  "mensaje": "Error de validación en los datos proporcionados",
  "errores": {
    "descripcion": "La descripción es obligatoria"
  }
}
```

#### Test 1.3: Múltiples errores
```bash
curl -X POST http://localhost:8080/tickets \
  -H "Content-Type: application/json" \
  -d '{
    "titulo": "No",
    "descripcion": "Corta"
  }'
```

**Respuesta esperada:**
```json
{
  "timestamp": "2026-02-11T10:45:40.123",
  "status": 400,
  "mensaje": "Error de validación en los datos proporcionados",
  "errores": {
    "titulo": "El título debe tener entre 5 y 100 caracteres",
    "descripcion": "La descripción debe tener al menos 10 caracteres"
  }
}
```

**Verificación en logs:**
```bash
grep "ValidationException" logs/application.log
tail -50 logs/application.log | grep "operacion_"
```

### Script de Prueba Automatizada

```bash
#!/bin/bash

echo "=== TEST 1.1: Título muy corto ==="
RESPONSE=$(curl -s -X POST http://localhost:8080/tickets \
  -H "Content-Type: application/json" \
  -d '{"titulo":"Abc","descripcion":"Descripción válida aquí"}')
echo "$RESPONSE" | jq .
echo "Status esperado: 400"
echo ""

echo "=== TEST 1.2: Descripción vacía ==="
RESPONSE=$(curl -s -X POST http://localhost:8080/tickets \
  -H "Content-Type: application/json" \
  -d '{"titulo":"Título válido suficientemente largo","descripcion":""}')
echo "$RESPONSE" | jq .
echo ""

echo "=== TEST 1.3: Múltiples errores ==="
RESPONSE=$(curl -s -X POST http://localhost:8080/tickets \
  -H "Content-Type: application/json" \
  -d '{"titulo":"No","descripcion":"Corta"}')
echo "$RESPONSE" | jq .
echo ""

echo "=== Verificación en logs ==="
tail -20 logs/application.log
```

### Criterios de Éxito

- ✅ Código de respuesta: `400 Bad Request`
- ✅ JSON de respuesta contiene campo "errores"
- ✅ Cada campo inválido tiene su mensaje
- ✅ Logs registran en application.log con operacion_id
- ✅ No se crea el ticket en BD

---

## Incidencia 2: Recurso No Encontrado

### Descripción
Intentar acceder/modificar un recurso que no existe resulta en error 404/500 con trazas en logs.

### Estados HTTP Esperados
- `404 Not Found` - Recurso no existe
- `500 Internal Server Error` - Error en operación

### Casos de Prueba

#### Test 2.1: GET de ticket inexistente
```bash
curl -X GET http://localhost:8080/tickets/9999
```

**Respuesta esperada:**
```
HTTP/1.1 404 Not Found
```

**Logs esperados:**
```
[operacion_yyy] INFO GET /tickets/9999 - Solicitud para obtener ticket
[operacion_yyy] WARN GET /tickets/9999 - Ticket no encontrado (404)
```

#### Test 2.2: Actualizar estado de ticket inexistente
```bash
curl -X PUT "http://localhost:8080/tickets/9999/estado?nuevoEstado=EN_PROGRESO"
```

**Respuesta esperada:**
```json
{
  "timestamp": "2026-02-11T10:50:15.123",
  "status": 500,
  "mensaje": "Ticket no encontrado con ID: 9999"
}
```

**Logs esperados en errors.log:**
```
[operacion_zzz] ERROR PUT /tickets/9999/estado - Error: Ticket no encontrado con ID: 9999
```

#### Test 2.3: Eliminar ticket inexistente
```bash
curl -X DELETE http://localhost:8080/tickets/9999
```

**Respuesta esperada:**
```json
{
  "timestamp": "2026-02-11T10:50:20.123",
  "status": 500,
  "mensaje": "Error interno del servidor"
}
```

### Script de Prueba Automatizada

```bash
#!/bin/bash

echo "=== Limpiar BD (opcional) ==="
# Si deseas empezar con BD vacía:
# Reinicia la aplicación (BD en memoria se limpia)

echo ""
echo "=== TEST 2.1: GET ticket inexistente ==="
curl -i -X GET http://localhost:8080/tickets/9999
echo ""

echo "=== TEST 2.2: PUT estado ticket inexistente ==="
RESPONSE=$(curl -s -X PUT "http://localhost:8080/tickets/9999/estado?nuevoEstado=EN_PROGRESO")
echo "$RESPONSE" | jq .
echo ""

echo "=== TEST 2.3: DELETE ticket inexistente ==="
RESPONSE=$(curl -s -X DELETE http://localhost:8080/tickets/9999)
echo "$RESPONSE" | jq .
echo ""

echo "=== Verificación en logs ==="
grep "9999" logs/application.log logs/errors.log
```

### Criterios de Éxito

- ✅ GET retorna 404
- ✅ PUT/DELETE retornan error apropiado
- ✅ Logs contienen ID de operación para rastreo
- ✅ Mensaje de error es claro sin exponer datos internos
- ✅ No hay stack traces completos en JSON de respuesta

---

## Incidencia 3: Degradación de Memoria

### Descripción
Crear gran cantidad de registros causa degradación de rendimiento y consumo de memoria.

### Estados HTTP Esperados
- `201 Created` - Tickets creados exitosamente
- `503 Service Unavailable` - Si memoria está crítica

### Casos de Prueba

#### Test 3.1: Baseline de memoria
```bash
curl -s http://localhost:8080/admin/diagnostico | jq '.diagnostico.memoria'
```

**Respuesta esperada (aplicación fresca):**
```json
{
  "total_mb": 512,
  "usada_mb": 128,
  "libre_mb": 384,
  "porcentaje_uso": "25.00%",
  "estado": "✓ NORMAL"
}
```

#### Test 3.2: Crear 100 tickets
```bash
#!/bin/bash
echo "=== Creando 100 tickets ==="
for i in {1..100}; do
  curl -s -X POST http://localhost:8080/tickets \
    -H "Content-Type: application/json" \
    -d "{
      \"titulo\": \"Ticket número $i con titulo largo para ocupar memoria\",
      \"descripcion\": \"Descripción del ticket $i que contiene información adicional para ocupar más espacio en memoria\",
      \"estado\": \"ABIERTO\"
    }" > /dev/null &
  
  # Mostrar progreso cada 10 tickets
  if [ $((i % 10)) -eq 0 ]; then
    echo "Creados: $i tickets"
  fi
done
wait
echo "✓ 100 tickets creados"
```

**Verificar creación:**
```bash
curl -s http://localhost:8080/tickets | jq 'length'
# Expected: 100
```

#### Test 3.3: Monitorear degradación
```bash
#!/bin/bash
echo "=== Monitoreo de Memoria ==="

for i in {1..10}; do
  echo "Medición $i:"
  curl -s http://localhost:8080/admin/diagnostico | jq '.diagnostico.memoria | {usada_mb, porcentaje_uso, estado}'
  echo ""
  sleep 2
done
```

#### Test 3.4: Medir tiempo de respuesta
```bash
#!/bin/bash
echo "=== Tiempo de Respuesta ==="

# Con pocos registros
echo "GET /tickets (100 registros):"
time curl -s -X GET http://localhost:8080/tickets > /dev/null

# Crear 1000 tickets más
echo ""
echo "Creando 1000 tickets más..."
for i in {101..1100}; do
  curl -s -X POST http://localhost:8080/tickets \
    -H "Content-Type: application/json" \
    -d "{
      \"titulo\": \"Ticket $i\",
      \"descripcion\": \"Descripción del ticket $i para test de rendimiento\"
    }" > /dev/null &
done
wait

# Con muchos registros
echo ""
echo "GET /tickets (1100 registros):"
time curl -s -X GET http://localhost:8080/tickets > /dev/null
```

### Script Completo de Simulación

```bash
#!/bin/bash

echo "╔════════════════════════════════════════════════════════╗"
echo "║  SIMULACIÓN INCIDENCIA 3: DEGRADACIÓN DE MEMORIA       ║"
echo "╚════════════════════════════════════════════════════════╝"
echo ""

echo "1️⃣  Estado inicial del sistema"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
INICIAL=$(curl -s http://localhost:8080/admin/diagnostico/health)
echo "$INICIAL" | jq .

MEMORIA_INICIAL=$(curl -s http://localhost:8080/admin/diagnostico | jq '.diagnostico.memoria.porcentaje_uso')
echo "Memoria inicial: $MEMORIA_INICIAL"
echo ""

echo "2️⃣  Creando 500 tickets..."
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

for i in {1..500}; do
  curl -s -X POST http://localhost:8080/tickets \
    -H "Content-Type: application/json" \
    -d "{
      \"titulo\": \"Ticket Test $i - Simulación de carga\",
      \"descripcion\": \"Descripción del ticket número $i para simular una carga en el sistema y monitorear su comportamiento bajo estrés. Contenido adicional para ocupar memoria.\",
      \"estado\": \"ABIERTO\"
    }" > /dev/null &
  
  if [ $((i % 50)) -eq 0 ]; then
    echo "  Progreso: $i/500 ✓"
  fi
done
wait
echo "✓ 500 tickets creados"
echo ""

echo "3️⃣  Análisis post-carga"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

TOTAL=$(curl -s http://localhost:8080/tickets | jq 'length')
echo "Total de tickets: $TOTAL"

MEMORIA_POST=$(curl -s http://localhost:8080/admin/diagnostico | jq '.diagnostico.memoria.porcentaje_uso')
echo "Memoria después: $MEMORIA_POST"

echo ""
echo "Diagnóstico completo:"
curl -s http://localhost:8080/admin/diagnostico | jq '.diagnostico'

echo ""
echo "4️⃣  Verificación de rendimiento"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "Tiempo para GET /tickets:"
time curl -s -X GET http://localhost:8080/tickets | jq 'length' > /dev/null

echo ""
echo "5️⃣  Análisis de logs"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "Últimas operaciones registradas:"
tail -20 logs/application.log | grep "operacion_id"

echo ""
echo "✓ Simulación completada"
```

### Criterios de Éxito

- ✅ Se crean 500+ tickets exitosamente
- ✅ Memoria se incrementa notoriamente (< 90% en desarrollo)
- ✅ Tiempo de respuesta se incrementa (pero < 5s para GET)
- ✅ Health check sigue retornando estado (aunque tal vez ralentizado)
- ✅ Logs se rotan correctamente sin perder información
- ✅ No hay excepciones de OutOfMemory

### Comportamiento Esperado vs Problemas

| Métrica | Esperado | Problema |
|---------|----------|----------|
| Memoria inicial | 25-30% | < 20% |
| Memoria post-500 tickets | 45-65% | > 85% |
| Tiempo GET /tickets | < 200ms | > 1000ms |
| Health check | ✓ SALUDABLE | ✗ PROBLEMAS |
| Logs rotos | No | Sí = problema |

---

## 📊 Resumen de Testing

### Checklist de Validación

```
Incidencia 1: Error de Validación
- [ ] Test 1.1 pasa (título corto)
- [ ] Test 1.2 pasa (descripción vacía)
- [ ] Test 1.3 pasa (múltiples errores)
- [ ] Logs contienen detalles
- [ ] No se crean tickets inválidos

Incidencia 2: Recurso No Encontrado
- [ ] Test 2.1 pasa (GET 404)
- [ ] Test 2.2 pasa (PUT error)
- [ ] Test 2.3 pasa (DELETE error)
- [ ] Logs tienen operacion_id
- [ ] Mensajes de error claros

Incidencia 3: Degradación Memoria
- [ ] Se crean 500 tickets
- [ ] Memoria se incrementa
- [ ] Rendimiento aceptable
- [ ] Health check OK
- [ ] Logs se rotan
```

---

**Última actualización:** Febrero 11, 2026  
**Usado para:** Testing manual y validación de funcionalidad
