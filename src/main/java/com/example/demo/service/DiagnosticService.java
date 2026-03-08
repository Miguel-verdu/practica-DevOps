package com.example.demo.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootVersion;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import java.util.*;

/**
 * Servicio de diagnóstico para detectar problemas en la aplicación
 */
@Service
public class DiagnosticService {

    private static final Logger logger = LoggerFactory.getLogger(DiagnosticService.class);

    @Autowired(required = false)
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private Environment environment;

    /**
     * Ejecuta un diagnóstico completo
     */
    public Map<String, Object> ejecutarDiagnostico() {
        Map<String, Object> diagnostico = new LinkedHashMap<>();
        logger.info("=== INICIANDO DIAGNÓSTICO DEL SISTEMA ===");

        // 1. Verificar conexión a BD
        diagnostico.put("conexion_bd", verificarConexionBD());

        // 2. Verificar memoria
        diagnostico.put("memoria", verificarMemoria());

        // 3. Verificar configuración
        diagnostico.put("configuracion", obtenerConfiguracion());

        // 4. Verificar logs
        diagnostico.put("logs", verificarLogs());

        logger.info("=== DIAGNÓSTICO COMPLETADO ===");
        return diagnostico;
    }

    /**
     * Verifica la conexión a base de datos
     */
    private Map<String, Object> verificarConexionBD() {
        Map<String, Object> resultado = new LinkedHashMap<>();
        try {
            if (jdbcTemplate != null) {
                jdbcTemplate.queryForObject("SELECT 1", Integer.class);
                resultado.put("estado", "CONECTADA");
                resultado.put("tipo", environment.getProperty("spring.datasource.url"));
                logger.info("✓ Base de datos: CONECTADA");
            } else {
                resultado.put("estado", "NO CONFIGURADA");
                logger.warn("✗ Base de datos: NO CONFIGURADA");
            }
        } catch (Exception e) {
            resultado.put("estado", "ERROR");
            resultado.put("error", e.getMessage());
            logger.error("✗ Base de datos: ERROR - {}", e.getMessage());
        }
        return resultado;
    }

    /**
     * Verifica el uso de memoria
     */
    private Map<String, Object> verificarMemoria() {
        Map<String, Object> resultado = new LinkedHashMap<>();
        Runtime runtime = Runtime.getRuntime();
        long memoriaTotal = runtime.totalMemory();
        long memoriaLibre = runtime.freeMemory();
        long memoriaUsada = memoriaTotal - memoriaLibre;
        double porcentajeUso = (memoriaUsada / (double) memoriaTotal) * 100;

        resultado.put("total_mb", memoriaTotal / (1024 * 1024));
        resultado.put("usada_mb", memoriaUsada / (1024 * 1024));
        resultado.put("libre_mb", memoriaLibre / (1024 * 1024));
        resultado.put("porcentaje_uso", String.format("%.2f%%", porcentajeUso));

        String estado = porcentajeUso > 90 ? "⚠ CRÍTICO" : (porcentajeUso > 75 ? "⚠ ALTO" : "✓ NORMAL");
        resultado.put("estado", estado);
        logger.info("Memoria: {} ({})", resultado.get("estado"), 
                   resultado.get("porcentaje_uso"));

        return resultado;
    }

    /**
     * Obtiene información de configuración relevante
     */
    private Map<String, Object> obtenerConfiguracion() {
        Map<String, Object> resultado = new LinkedHashMap<>();
        resultado.put("spring_version", SpringBootVersion.getVersion());
        resultado.put("java_version", System.getProperty("java.version"));
        resultado.put("profile_activo", Arrays.toString(environment.getActiveProfiles()));
        resultado.put("puerto", environment.getProperty("server.port", "8080"));
        logger.info("Configuración cargada: Spring Boot {}, Java {}", 
                   resultado.get("spring_version"), resultado.get("java_version"));
        return resultado;
    }

    /**
     * Verifica la configuración de logs
     */
    private Map<String, Object> verificarLogs() {
        Map<String, Object> resultado = new LinkedHashMap<>();
        resultado.put("nivel_log", environment.getProperty("logging.level.root", "INFO"));
        resultado.put("nivel_aplicacion", environment.getProperty("logging.level.com.example.demo", "INFO"));
        resultado.put("archivo_log", environment.getProperty("logging.file.name", "Sin archivo configurado"));
        logger.info("Logs: Nivel {}, Archivo: {}", 
                   resultado.get("nivel_log"), resultado.get("archivo_log"));
        return resultado;
    }

    /**
     * Realiza un health check rápido
     */
    public boolean healthCheck() {
        try {
            logger.debug("Ejecutando health check...");
            if (jdbcTemplate != null) {
                jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            }
            logger.debug("Health check: OK");
            return true;
        } catch (Exception e) {
            logger.error("Health check falló: {}", e.getMessage());
            return false;
        }
    }
}
