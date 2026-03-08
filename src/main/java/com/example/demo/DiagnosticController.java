package com.example.demo;

import com.example.demo.service.DiagnosticService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Controlador REST para el diagnóstico y monitoreo del sistema de tickets.
 *
 * <p>Expone endpoints de administración bajo la ruta base
 * {@code /admin/diagnostico}. Estos endpoints permiten obtener
 * información sobre el estado interno de la aplicación y comprobar
 * rápidamente si el servicio se encuentra operativo.</p>
 *
 * <p>Ejemplo de uso:</p>
 * <pre>
 *   GET /admin/diagnostico        → diagnóstico completo
 *   GET /admin/diagnostico/health → health check rápido
 * </pre>
 *
 * @author Miguel Verdú Pacheco
 * @version 1.0
 * @since 1.0
 * @see DiagnosticService
 */
@RestController
@RequestMapping("/admin/diagnostico")
public class DiagnosticController {

    /**
     * Logger de la clase para registrar eventos de diagnóstico.
     */
    private static final Logger logger = LoggerFactory.getLogger(DiagnosticController.class);

    /**
     * Servicio que contiene la lógica de negocio del diagnóstico.
     * Inyectado automáticamente por Spring.
     */
    @Autowired
    private DiagnosticService diagnosticService;

    /**
     * Ejecuta un diagnóstico completo del sistema y devuelve los resultados.
     *
     * <p>Invoca {@link DiagnosticService#ejecutarDiagnostico()} para recopilar
     * métricas e información de estado de todos los subsistemas. La respuesta
     * incluye un {@code timestamp} con el momento exacto de la consulta y
     * el mapa de resultados del diagnóstico.</p>
     *
     * <p>Ejemplo de respuesta JSON:</p>
     * <pre>
     * {
     *   "timestamp": "2024-01-15T10:30:00",
     *   "diagnostico": {
     *     "baseDatos": "OK",
     *     "memoria": "512MB libre"
     *   }
     * }
     * </pre>
     *
     * @return {@link ResponseEntity} con código HTTP {@code 200 OK} y un
     *         {@code Map} que contiene {@code timestamp} (LocalDateTime) y
     *         {@code diagnostico} (mapa con los resultados del análisis).
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> diagnostico() {
        logger.info("======== DIAGNÓSTICO SOLICITADO ========");

        Map<String, Object> diagnostico = diagnosticService.ejecutarDiagnostico();

        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("diagnostico", diagnostico);

        logger.info("======== DIAGNÓSTICO COMPLETADO ========");

        return ResponseEntity.ok(response);
    }

    /**
     * Realiza un health check rápido para comprobar si el sistema está operativo.
     *
     * <p>Consulta {@link DiagnosticService#healthCheck()} y construye una
     * respuesta ligera indicando el estado actual del servicio. Es útil
     * para balanceadores de carga o herramientas de monitoreo externas
     * que necesitan saber si la aplicación puede atender peticiones.</p>
     *
     * <p>Ejemplo de respuesta cuando el sistema está sano:</p>
     * <pre>
     * HTTP 200 OK
     * {
     *   "timestamp": "2024-01-15T10:30:00",
     *   "estado": "SALUDABLE",
     *   "codigo": "200"
     * }
     * </pre>
     *
     * <p>Ejemplo de respuesta cuando hay problemas:</p>
     * <pre>
     * HTTP 503 Service Unavailable
     * {
     *   "timestamp": "2024-01-15T10:30:00",
     *   "estado": "PROBLEMAS",
     *   "codigo": "503"
     * }
     * </pre>
     *
     * @return {@link ResponseEntity} con código HTTP {@code 200 OK} si el sistema
     *         está saludable, o {@code 503 Service Unavailable} en caso contrario.
     *         El cuerpo incluye {@code timestamp}, {@code estado} y {@code codigo}.
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        boolean saludable = diagnosticService.healthCheck();

        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("estado", saludable ? "SALUDABLE" : "PROBLEMAS");
        response.put("codigo", saludable ? "200" : "503");

        logger.info("Health check: Estado = {}", saludable ? "SALUDABLE" : "PROBLEMAS");

        return saludable ?
               ResponseEntity.ok(response) :
               ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }
}
