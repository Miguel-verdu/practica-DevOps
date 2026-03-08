package com.example.demo;

import com.example.demo.entity.Ticket;
import com.example.demo.entity.EstadoTicket;
import com.example.demo.service.TicketService;
import com.example.demo.util.LoggingUtil;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

/**
 * Controlador REST para la gestión completa de tickets de soporte.
 *
 * <p>Expone los endpoints CRUD bajo la ruta base {@code /tickets} y delega
 * toda la lógica de negocio en {@link TicketService}. Cada operación registra
 * su actividad mediante SLF4J y gestiona de forma independiente sus propias
 * excepciones, devolviendo códigos HTTP semánticamente correctos.</p>
 *
 * <p>Endpoints disponibles:</p>
 * <ul>
 *   <li>{@code GET    /tickets}           – lista todos los tickets.</li>
 *   <li>{@code GET    /tickets/{id}}      – obtiene un ticket por su ID.</li>
 *   <li>{@code POST   /tickets}           – crea un nuevo ticket.</li>
 *   <li>{@code PUT    /tickets/{id}/estado} – actualiza el estado de un ticket.</li>
 *   <li>{@code DELETE /tickets/{id}}      – elimina un ticket existente.</li>
 * </ul>
 *
 * @author Miguel Verdú Pacheco
 * @version 1.0
 * @since 1.0
 * @see TicketService
 * @see Ticket
 */
@RestController
@RequestMapping("/tickets")
public class HolaController {

    /**
     * Logger de la clase para registrar el ciclo de vida de cada petición.
     */
    private static final Logger logger = LoggerFactory.getLogger(HolaController.class);

    /**
     * Servicio de negocio que gestiona las operaciones sobre tickets.
     * Inyectado automáticamente por Spring.
     */
    @Autowired
    private TicketService ticketService;

    /**
     * Devuelve la lista completa de todos los tickets almacenados en el sistema.
     *
     * <p>Consulta {@link TicketService#obtenerTodos()} y serializa el resultado
     * como un array JSON. Si no existe ningún ticket, se devuelve una lista vacía
     * ({@code []}) con código {@code 200 OK}.</p>
     *
     * <p>Ejemplo de respuesta:</p>
     * <pre>
     * HTTP 200 OK
     * [
     *   { "id": 1, "titulo": "Error de login", "estado": "ABIERTO" },
     *   { "id": 2, "titulo": "Fallo en pago",  "estado": "EN_PROCESO" }
     * ]
     * </pre>
     *
     * @return {@link ResponseEntity} con código {@code 200 OK} y la lista de tickets,
     *         o {@code 500 Internal Server Error} si ocurre un error inesperado.
     */
    @GetMapping
    public ResponseEntity<List<Ticket>> listarTickets() {
        logger.info("GET /tickets - Solicitud para listar todos los tickets");
        try {
            List<Ticket> tickets = ticketService.obtenerTodos();
            logger.info("GET /tickets - Respuesta OK: {} tickets retornados", tickets.size());
            return ResponseEntity.ok(tickets);
        } catch (Exception e) {
            logger.error("GET /tickets - Error interno: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Busca y devuelve un ticket concreto identificado por su ID.
     *
     * <p>Si el ticket existe se devuelve con código {@code 200 OK};
     * si no se encuentra, se responde con {@code 404 Not Found} sin cuerpo.</p>
     *
     * <p>Ejemplo de respuesta exitosa:</p>
     * <pre>
     * HTTP 200 OK
     * { "id": 1, "titulo": "Error de login", "estado": "ABIERTO" }
     * </pre>
     *
     * @param id identificador único del ticket a recuperar. Debe ser un
     *           número entero positivo que corresponda a un ticket existente.
     * @return {@link ResponseEntity} con código {@code 200 OK} y el ticket encontrado,
     *         {@code 404 Not Found} si no existe ningún ticket con ese ID,
     *         o {@code 500 Internal Server Error} ante cualquier otro error.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Ticket> obtenerTicket(@PathVariable Long id) {
        logger.info("GET /tickets/{} - Solicitud para obtener ticket", id);
        try {
            Optional<Ticket> ticket = ticketService.obtenerPorId(id);
            if (ticket.isPresent()) {
                logger.info("GET /tickets/{} - Respuesta OK", id);
                return ResponseEntity.ok(ticket.get());
            } else {
                logger.warn("GET /tickets/{} - Ticket no encontrado (404)", id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("GET /tickets/{} - Error interno: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Crea un nuevo ticket de soporte a partir de los datos proporcionados en el cuerpo.
     *
     * <p>El cuerpo de la petición es validado automáticamente por Bean Validation
     * ({@code @Valid}). Si la validación falla, el {@code GlobalExceptionHandler}
     * intercepta la excepción y devuelve los errores de campo. Si la validación
     * pasa, el ticket es persistido y se devuelve con su ID asignado.</p>
     *
     * <p>Ejemplo de cuerpo de petición:</p>
     * <pre>
     * POST /tickets
     * Content-Type: application/json
     * {
     *   "titulo": "Error de login",
     *   "descripcion": "El usuario no puede iniciar sesión con su contraseña."
     * }
     * </pre>
     *
     * <p>Ejemplo de respuesta exitosa:</p>
     * <pre>
     * HTTP 201 Created
     * { "id": 3, "titulo": "Error de login", "estado": "ABIERTO" }
     * </pre>
     *
     * @param ticket objeto {@link Ticket} deserializado del cuerpo JSON de la petición
     *               y validado con las restricciones definidas en la entidad.
     * @return {@link ResponseEntity} con código {@code 201 Created} y el ticket
     *         recién creado (incluyendo el ID generado), o {@code 500 Internal Server Error}
     *         si ocurre un error durante la persistencia.
     */
    @PostMapping
    public ResponseEntity<Ticket> crearTicket(@Valid @RequestBody Ticket ticket) {
        String idOp = LoggingUtil.registrarInicio("POST /tickets");
        logger.info("{} - Creando nuevo ticket, Título: {} caracteres",
                   idOp, ticket.getTitulo().length());
        try {
            Ticket nuevoTicket = ticketService.crearTicket(ticket);
            logger.info("{} - Ticket creado exitosamente. ID: {}", idOp, nuevoTicket.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevoTicket);
        } catch (Exception e) {
            logger.error("{} - Error al crear ticket: {}", idOp, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } finally {
            LoggingUtil.limpiarContexto();
        }
    }

    /**
     * Actualiza el estado de un ticket existente.
     *
     * <p>El nuevo estado se recibe como parámetro de consulta ({@code ?nuevoEstado=})
     * y debe coincidir exactamente (ignorando mayúsculas/minúsculas) con uno de los
     * valores del enum {@link EstadoTicket}. Si el valor no es válido, se devuelve
     * {@code 400 Bad Request}.</p>
     *
     * <p>Ejemplo de petición:</p>
     * <pre>
     * PUT /tickets/1/estado?nuevoEstado=CERRADO
     * </pre>
     *
     * <p>Ejemplo de respuesta exitosa:</p>
     * <pre>
     * HTTP 200 OK
     * { "id": 1, "titulo": "Error de login", "estado": "CERRADO" }
     * </pre>
     *
     * @param id          identificador único del ticket cuyo estado se desea cambiar.
     * @param nuevoEstado cadena de texto con el nuevo estado deseado. Se convierte
     *                    a mayúsculas antes de mapearse al enum {@link EstadoTicket}.
     * @return {@link ResponseEntity} con código {@code 200 OK} y el ticket actualizado,
     *         {@code 400 Bad Request} si {@code nuevoEstado} no corresponde a ningún
     *         valor de {@link EstadoTicket}, o {@code 500 Internal Server Error} ante
     *         cualquier otro error inesperado.
     */
    @PutMapping("/{id}/estado")
    public ResponseEntity<Ticket> actualizarEstado(@PathVariable Long id,
                                                    @RequestParam String nuevoEstado) {
        logger.info("PUT /tickets/{}/estado - Actualizando estado a: {}", id, nuevoEstado);
        try {
            Ticket actualizado = ticketService.actualizarEstado(id,
                    com.example.demo.entity.EstadoTicket.valueOf(nuevoEstado.toUpperCase()));
            logger.info("PUT /tickets/{}/estado - Respuesta OK", id);
            return ResponseEntity.ok(actualizado);
        } catch (IllegalArgumentException e) {
            logger.warn("PUT /tickets/{}/estado - Estado inválido: {}", id, nuevoEstado);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("PUT /tickets/{}/estado - Error: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Elimina permanentemente un ticket del sistema dado su ID.
     *
     * <p>Si la eliminación se realiza con éxito, se responde con
     * {@code 204 No Content} sin cuerpo en la respuesta, siguiendo
     * las convenciones REST para operaciones de borrado.</p>
     *
     * <p>Ejemplo de petición:</p>
     * <pre>
     * DELETE /tickets/1
     * </pre>
     *
     * <p>Ejemplo de respuesta exitosa:</p>
     * <pre>
     * HTTP 204 No Content
     * </pre>
     *
     * @param id identificador único del ticket que se desea eliminar.
     * @return {@link ResponseEntity} con código {@code 204 No Content} si la
     *         eliminación fue exitosa, o {@code 500 Internal Server Error} si
     *         ocurre algún error durante el proceso.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarTicket(@PathVariable Long id) {
        logger.warn("DELETE /tickets/{} - Solicitando eliminación", id);
        try {
            ticketService.eliminarTicket(id);
            logger.info("DELETE /tickets/{} - Eliminado exitosamente", id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            logger.error("DELETE /tickets/{} - Error: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
