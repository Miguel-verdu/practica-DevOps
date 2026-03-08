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
 * Servicio que recopila información de diagnóstico y monitoreo del sistema.
 *
 * <p>Proporciona dos niveles de inspección del estado de la aplicación:</p>
 * <ul>
 *   <li><b>Diagnóstico completo</b> ({@link #ejecutarDiagnostico()}): analiza la base de datos,
 *       la memoria JVM, la configuración activa y el sistema de logs.</li>
 *   <li><b>Health check rápido</b> ({@link #healthCheck()}): comprobación ligera de
 *       disponibilidad, pensada para balanceadores de carga o herramientas de monitoreo.</li>
 * </ul>
 *
 * <p>Este servicio es consumido por {@code DiagnosticController} y sus métodos privados
 * no forman parte de la API pública, pero están documentados para facilitar el mantenimiento.</p>
 *
 * @author Equipo de Desarrollo
 * @version 1.0
 * @since 1.0
 */
@Service
public class DiagnosticService {

    /**
     * Logger de la clase para registrar el progreso y resultado de cada diagnóstico.
     */
    private static final Logger logger = LoggerFactory.getLogger(DiagnosticService.class);

    /**
     * Plantilla JDBC para ejecutar consultas de comprobación contra la base de datos.
     * Es opcional ({@code required = false}): si no hay datasource configurado,
     * permanece {@code null} y los métodos de BD lo gestionan sin lanzar excepción.
     */
    @Autowired(required = false)
    private JdbcTemplate jdbcTemplate;

    /**
     * Entorno de Spring que permite acceder a las propiedades de configuración
     * ({@code application.properties} / variables de entorno / perfiles activos).
     */
    @Autowired
    private Environment environment;

    /**
     * Ejecuta un diagnóstico completo del sistema y devuelve los resultados agrupados.
     *
     * <p>Realiza las siguientes comprobaciones en orden:</p>
     * <ol>
     *   <li>Conexión a base de datos ({@link #verificarConexionBD()}).</li>
     *   <li>Uso de memoria JVM ({@link #verificarMemoria()}).</li>
     *   <li>Versiones y configuración activa ({@link #obtenerConfiguracion()}).</li>
     *   <li>Niveles y destino del sistema de logs ({@link #verificarLogs()}).</li>
     * </ol>
     *
     * <p>Ejemplo de resultado:</p>
     * <pre>
     * {
     *   "conexion_bd":   { "estado": "CONECTADA", "tipo": "jdbc:h2:mem:..." },
     *   "memoria":       { "total_mb": 256, "usada_mb": 120, "estado": "✓ NORMAL" },
     *   "configuracion": { "spring_version": "3.2.0", "java_version": "17.0.9" },
     *   "logs":          { "nivel_log": "INFO", "archivo_log": "Sin archivo configurado" }
     * }
     * </pre>
     *
     * @return {@link Map} con cuatro entradas ({@code conexion_bd}, {@code memoria},
     *         {@code configuracion}, {@code logs}), cada una conteniendo a su vez
     *         un mapa con los detalles de esa área.
     */
    public Map<String, Object> ejecutarDiagnostico() {
        Map<String, Object> diagnostico = new LinkedHashMap<>();
        logger.info("=== INICIANDO DIAGNÓSTICO DEL SISTEMA ===");

        diagnostico.put("conexion_bd", verificarConexionBD());
        diagnostico.put("memoria", verificarMemoria());
        diagnostico.put("configuracion", obtenerConfiguracion());
        diagnostico.put("logs", verificarLogs());

        logger.info("=== DIAGNÓSTICO COMPLETADO ===");
        return diagnostico;
    }

    /**
     * Comprueba si la base de datos está accesible ejecutando una consulta mínima.
     *
     * <p>Ejecuta {@code SELECT 1} a través de {@link JdbcTemplate}. Si {@code jdbcTemplate}
     * es {@code null} (no hay datasource configurado), informa el estado como
     * {@code "NO CONFIGURADA"} sin lanzar excepción.</p>
     *
     * @return {@link Map} con las claves:
     *         <ul>
     *           <li>{@code estado} – {@code "CONECTADA"}, {@code "NO CONFIGURADA"} o {@code "ERROR"}.</li>
     *           <li>{@code tipo}   – URL del datasource (solo si está conectada).</li>
     *           <li>{@code error}  – mensaje de excepción (solo si hay error).</li>
     *         </ul>
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
     * Analiza el uso de memoria de la JVM en el momento de la llamada.
     *
     * <p>Obtiene los valores de {@link Runtime#getRuntime()} y calcula el porcentaje
     * de memoria usada. Clasifica el resultado en tres niveles:</p>
     * <ul>
     *   <li>{@code "✓ NORMAL"}   – uso por debajo del 75 %.</li>
     *   <li>{@code "⚠ ALTO"}     – uso entre el 75 % y el 90 %.</li>
     *   <li>{@code "⚠ CRÍTICO"}  – uso superior al 90 %.</li>
     * </ul>
     *
     * @return {@link Map} con las claves {@code total_mb}, {@code usada_mb},
     *         {@code libre_mb}, {@code porcentaje_uso} y {@code estado}.
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
     * Recopila información sobre las versiones y la configuración activa de la aplicación.
     *
     * <p>Incluye la versión de Spring Boot, la versión de Java, los perfiles de Spring
     * activos y el puerto en el que escucha el servidor (por defecto {@code 8080}).</p>
     *
     * @return {@link Map} con las claves {@code spring_version}, {@code java_version},
     *         {@code profile_activo} y {@code puerto}.
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
     * Obtiene la configuración actual del sistema de logging de la aplicación.
     *
     * <p>Consulta las propiedades {@code logging.level.root},
     * {@code logging.level.com.example.demo} y {@code logging.file.name}
     * del entorno de Spring para informar sobre los niveles y el destino de los logs.</p>
     *
     * @return {@link Map} con las claves {@code nivel_log}, {@code nivel_aplicacion}
     *         y {@code archivo_log}. Si alguna propiedad no está definida, se devuelve
     *         un valor por defecto descriptivo.
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
     * Realiza una comprobación rápida de disponibilidad del sistema.
     *
     * <p>Si hay base de datos configurada, ejecuta {@code SELECT 1} para verificar
     * que la conexión responde. Si no hay datasource, devuelve {@code true} directamente,
     * ya que el resto del sistema puede estar operativo sin base de datos.</p>
     *
     * <p>Este método está diseñado para ser llamado con alta frecuencia (por ejemplo,
     * desde un balanceador de carga) sin impacto significativo en el rendimiento.</p>
     *
     * @return {@code true} si el sistema está operativo y la base de datos responde
     *         correctamente (o no está configurada); {@code false} si la consulta
     *         de comprobación lanza alguna excepción.
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
