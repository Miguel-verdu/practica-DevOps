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

@Service
public class TicketService {

    private static final Logger logger = LoggerFactory.getLogger(TicketService.class);

    @Autowired
    private TicketRepository ticketRepository;

    /**
     * Crea un nuevo ticket con logging
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
     * Obtiene todos los tickets
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
     * Obtiene un ticket por ID
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
     * Actualiza el estado de un ticket
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
     * Elimina un ticket
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
            logger.error("{}} - Error al eliminar ticket: {}", idOp, e.getMessage());
            throw e;
        } finally {
            LoggingUtil.limpiarContexto();
        }
    }
}
