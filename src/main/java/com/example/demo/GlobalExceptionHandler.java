package com.example.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Manejador global de excepciones para toda la aplicación de gestión de tickets.
 *
 * <p>Gracias a la anotación {@code @ControllerAdvice}, esta clase intercepta
 * las excepciones lanzadas por cualquier controlador REST y las transforma en
 * respuestas HTTP con formato JSON coherente, evitando que el cliente reciba
 * trazas de pila o mensajes de error sin estructura.</p>
 *
 * <p>Todas las respuestas de error siguen la misma estructura:</p>
 * <pre>
 * {
 *   "timestamp": "2024-01-15T10:30:00",
 *   "status":    400,
 *   "mensaje":   "Descripción del error",
 *   "errores":   { ... }   // solo en errores de validación
 * }
 * </pre>
 *
 * <p>Jerarquía de handlers (del más específico al más general):</p>
 * <ol>
 *   <li>{@link #handleValidationExceptions} – errores de validación de campos.</li>
 *   <li>{@link #handleIllegalArgument} – argumentos de negocio inválidos.</li>
 *   <li>{@link #handleGenericException} – cualquier otra excepción no prevista.</li>
 * </ol>
 *
 * @author Miguel Verdú Pacheco
 * @version 1.0
 * @since 1.0
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Logger de la clase para registrar los errores capturados.
     */
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Maneja los errores de validación de campos en el cuerpo de las peticiones.
     *
     * <p>Se activa cuando Spring no puede vincular o validar los datos de entrada
     * anotados con {@code @Valid} o {@code @Validated} en un controlador.
     * Recopila todos los errores de campo y los incluye en la respuesta para
     * que el cliente pueda corregirlos de una sola vez.</p>
     *
     * <p>Ejemplo de respuesta:</p>
     * <pre>
     * HTTP 400 Bad Request
     * {
     *   "timestamp": "2024-01-15T10:30:00",
     *   "status": 400,
     *   "mensaje": "Error de validación en los datos proporcionados",
     *   "errores": {
     *     "titulo": "no debe estar vacío",
     *     "descripcion": "debe tener entre 10 y 500 caracteres"
     *   }
     * }
     * </pre>
     *
     * @param ex excepción lanzada por Spring cuando la validación de un argumento
     *           del método del controlador falla.
     * @return {@link ResponseEntity} con código HTTP {@code 400 Bad Request} y
     *         un mapa que contiene {@code timestamp}, {@code status}, {@code mensaje}
     *         y {@code errores} (mapa campo → mensaje de error).
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
            errors.put(error.getField(), error.getDefaultMessage()));

        logger.warn("ValidationException: {} errores de validación detectados",
                   errors.size());
        errors.forEach((field, message) ->
            logger.debug("  - Campo '{}': {}", field, message));

        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("mensaje", "Error de validación en los datos proporcionados");
        response.put("errores", errors);

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Maneja excepciones causadas por argumentos de negocio inválidos.
     *
     * <p>Se activa cuando alguna capa de la aplicación (servicio, repositorio, etc.)
     * lanza explícitamente una {@link IllegalArgumentException} para señalar que
     * un valor proporcionado no cumple las reglas de negocio (por ejemplo, un estado
     * de ticket desconocido o un identificador fuera de rango).</p>
     *
     * <p>Ejemplo de respuesta:</p>
     * <pre>
     * HTTP 400 Bad Request
     * {
     *   "timestamp": "2024-01-15T10:30:00",
     *   "status": 400,
     *   "mensaje": "El estado 'PENDIENTE' no es válido para este ticket"
     * }
     * </pre>
     *
     * @param ex excepción que contiene el mensaje descriptivo del argumento inválido.
     * @return {@link ResponseEntity} con código HTTP {@code 400 Bad Request} y
     *         un mapa con {@code timestamp}, {@code status} y {@code mensaje}.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        logger.error("IllegalArgumentException: {}", ex.getMessage());

        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("mensaje", ex.getMessage());

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Maneja cualquier excepción no capturada por los handlers más específicos.
     *
     * <p>Actúa como red de seguridad para errores inesperados o no previstos
     * durante el procesamiento de una petición. Registra la traza completa del
     * error en el log para facilitar la depuración, y devuelve al cliente un
     * mensaje genérico sin exponer información interna sensible.</p>
     *
     * <p>Ejemplo de respuesta:</p>
     * <pre>
     * HTTP 500 Internal Server Error
     * {
     *   "timestamp": "2024-01-15T10:30:00",
     *   "status": 500,
     *   "mensaje": "Error interno del servidor"
     * }
     * </pre>
     *
     * @param ex excepción genérica no manejada por ningún otro handler.
     * @return {@link ResponseEntity} con código HTTP {@code 500 Internal Server Error}
     *         y un mapa con {@code timestamp}, {@code status} y {@code mensaje} genérico.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        logger.error("Exception no manejada: {}", ex.getMessage(), ex);

        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        response.put("mensaje", "Error interno del servidor");

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
