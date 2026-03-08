package com.example.demo.service;

import com.example.demo.TicketRepository;
import com.example.demo.entity.EstadoTicket;
import com.example.demo.entity.Ticket;
import com.example.demo.util.LoggingUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

/**
 * Servicio que contiene la lógica de negocio para la gestión de tickets de soporte.
 *
 * <p>Actúa como capa intermedia entre los controladores REST y el repositorio
 * de persistencia {@link TicketRepository}. Todas las operaciones registran
 * su actividad mediante SLF4J y propagan las excepciones hacia arriba para
 * que el {@code GlobalExceptionHandler} las gestione de forma centralizada.</p>
 *
 * <p>Operaciones disponibles:</p>
 * <ul>
 *   <li>{@link #crearTicket(Ticket)}                           – persiste un nuevo ticket.</li>
 *   <li>{@link #obtenerTodos()}                                – lista todos los tickets.</li>
 *   <li>{@link #obtenerPorId(Long)}                           – busca un ticket por ID.</li>
 *   <li>{@link #actualizarEstado(Long, EstadoTicket)}         – cambia el estado de un ticket.</li>
 *   <li>{@link #eliminarTicket(Long)}                         – elimina un ticket existente.</li>
 * </ul>
 *
 * @author Equipo de Desarrollo
 * @version 1.0
 * @since 1.0
 * @see TicketRepository
 * @see Ticket
 * @see EstadoTicket
 */
@Service
public class TicketService {

    /**
     * Logger de la clase para registrar el ciclo de vida de cada operación sobre tickets.
     */
    private static final Logger logger = LoggerFactory.getLogger(TicketService.class);

    /**
     * Repositorio JPA para persistir y recuperar tickets de la base de datos.
     * Inyectado automáticamente por Spring.
     */
    @Autowired
    private TicketRepository ticketRepository;

    /**
     * Persiste un nuevo ticket en la base de datos.
     *
     * <p>El ticket recibido ya ha sido validado por Bean Validation en el controlador.
     * Este método lo guarda mediante {@link TicketRepository#save(Object)} y devuelve
     * la instancia enriquecida con el ID generado por la base de datos.</p>
     *
     * @param ticket objeto {@link Ticket} con los datos a persistir; no debe ser {@code null}
     *               y su título no debe ser {@code null} (se usa para calcular su longitud en el log).
     * @return el {@link Ticket} recién guardado, incluyendo el ID asignado por la base de datos.
     * @throws RuntimeException si ocurre algún error durante la persistencia; la excepción
     *                          se registra y se relanza para que el controlador la gestione.
     */
    public Ticket crearTicket(Ticket ticket) {
        String idOp = LoggingUtil.registrarInicio("crear_ticket");
        logger.info("{} - Longitud del título: {} caracteres", idOp,
                   ticket.getTitulo() != null ? ticket.getTitulo().length() : 0);

        try {
            Ticket nuevoTicket = ticketRepository.save(ticket);
            logger.info("{} - Ticket creado exitosamente. ID: {}, Estado: {}",
                       idOp, nuevoTicket.getId(), nuevoTicket.getEstado());
            return nuevoTicket;
        } catch (Exception e) {
            logger.error("{} - Error al crear ticket: {}", idOp, e.getMessage());
            throw e;
        } finally {
            LoggingUtil.limpiarContexto();
        }
    }

    /**
     * Recupera la lista completa de todos los tickets almacenados en el sistema.
     *
     * <p>Si no existe ningún ticket, devuelve una lista vacía (nunca {@code null}).</p>
     *
     * @return {@link List} con todos los {@link Ticket} existentes, posiblemente vacía.
     * @throws RuntimeException si ocurre algún error al consultar la base de datos.
     */
    public List<Ticket> obtenerTodos() {
        String idOp = LoggingUtil.registrarInicio("listar_tickets");
        try {
            List<Ticket> tickets = ticketRepository.findAll();
            logger.info("{} - Se obtuvieron {} tickets de la base de datos", idOp, tickets.size());
            return tickets;
        } catch (Exception e) {
            logger.error("{} - Error al obtener tickets: {}", idOp, e.getMessage());
            throw e;
        } finally {
            LoggingUtil.limpiarContexto();
        }
    }

    /**
     * Busca un ticket concreto por su identificador único.
     *
     * <p>Devuelve un {@link Optional} vacío si no existe ningún ticket con ese ID,
     * en lugar de lanzar una excepción, siguiendo el principio de ausencia explícita.</p>
     *
     * @param id identificador único del ticket a buscar; no debe ser {@code null}.
     * @return {@link Optional} que contiene el {@link Ticket} si se encuentra,
     *         o {@link Optional#empty()} si no existe ningún ticket con ese ID.
     * @throws RuntimeException si ocurre algún error al consultar la base de datos.
     */
    public Optional<Ticket> obtenerPorId(Long id) {
        String idOp = LoggingUtil.registrarInicio("obtener_ticket");
        logger.debug("{} - Buscando ticket con ID: {}", idOp, id);

        try {
            Optional<Ticket> ticket = ticketRepository.findById(id);
            if (ticket.isPresent()) {
                logger.info("{} - Ticket encontrado. ID: {}, Estado: {}",
                           idOp, id, ticket.get().getEstado());
            } else {
                logger.warn("{} - Ticket no encontrado. ID: {}", idOp, id);
            }
            return ticket;
        } catch (Exception e) {
            logger.error("{} - Error al obtener ticket: {}", idOp, e.getMessage());
            throw e;
        } finally {
            LoggingUtil.limpiarContexto();
        }
    }

    /**
     * Actualiza el estado de un ticket existente.
     *
     * <p>Busca el ticket por su ID, registra el cambio de estado (estado anterior → nuevo)
     * y persiste la entidad actualizada. Si el ticket no existe, lanza una
     * {@link IllegalArgumentException} con un mensaje descriptivo.</p>
     *
     * <p>Ejemplo de transición válida:</p>
     * <pre>
     *   actualizarEstado(1L, EstadoTicket.EN_PROCESO)
     *   // ABIERTO → EN_PROCESO
     * </pre>
     *
     * @param id          identificador único del ticket a actualizar; no debe ser {@code null}.
     * @param nuevoEstado nuevo valor de {@link EstadoTicket} a asignar; no debe ser {@code null}.
     * @return el {@link Ticket} con el estado actualizado tal como quedó en la base de datos.
     * @throws IllegalArgumentException si no existe ningún ticket con el ID proporcionado.
     * @throws RuntimeException         si ocurre algún otro error durante la persistencia.
     */
    public Ticket actualizarEstado(Long id, EstadoTicket nuevoEstado) {
        String idOp = LoggingUtil.registrarInicio("actualizar_estado_ticket");
        logger.info("{} - Cambiando estado del ticket ID: {} a: {}", idOp, id, nuevoEstado);

        try {
            Optional<Ticket> ticketOpt = ticketRepository.findById(id);
            if (ticketOpt.isEmpty()) {
                logger.error("{} - Ticket no encontrado para actualizar. ID: {}", idOp, id);
                throw new IllegalArgumentException("Ticket no encontrado con ID: " + id);
            }

            Ticket ticket = ticketOpt.get();
            EstadoTicket estadoAnterior = ticket.getEstado();
            ticket.setEstado(nuevoEstado);
            Ticket actualizado = ticketRepository.save(ticket);

            logger.info("{} - Estado actualizado. ID: {}, {} -> {}",
                       idOp, id, estadoAnterior, nuevoEstado);
            return actualizado;
        } catch (Exception e) {
            logger.error("{} - Error al actualizar estado: {}", idOp, e.getMessage());
            throw e;
        } finally {
            LoggingUtil.limpiarContexto();
        }
    }

    /**
     * Elimina permanentemente un ticket del sistema dado su ID.
     *
     * <p>Antes de eliminar, verifica que el ticket existe. Si no se encuentra,
     * lanza una {@link IllegalArgumentException}. La operación de borrado
     * es irreversible.</p>
     *
     * @param id identificador único del ticket a eliminar; no debe ser {@code null}.
     * @throws IllegalArgumentException si no existe ningún ticket con el ID proporcionado.
     * @throws RuntimeException         si ocurre algún error durante la eliminación en base de datos.
     */
    public void eliminarTicket(Long id) {
        String idOp = LoggingUtil.registrarInicio("eliminar_ticket");
        logger.warn("{} - Eliminando ticket. ID: {}", idOp, id);

        try {
            Optional<Ticket> ticket = ticketRepository.findById(id);
            if (ticket.isEmpty()) {
                logger.error("{} - Ticket no encontrado para eliminar. ID: {}", idOp, id);
                throw new IllegalArgumentException("Ticket no encontrado");
            }
            ticketRepository.deleteById(id);
            logger.info("{} - Ticket eliminado exitosamente. ID: {}", idOp, id);
        } catch (Exception e) {
            logger.error("{} - Error al eliminar ticket: {}", idOp, e.getMessage());
            throw e;
        } finally {
            LoggingUtil.limpiarContexto();
        }
    }
}
