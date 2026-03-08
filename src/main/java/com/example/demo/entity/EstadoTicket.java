package com.example.demo.entity;

/**
 * Enumeración que representa los posibles estados de un ticket de soporte.
 *
 * <p>El ciclo de vida típico de un ticket sigue esta progresión:</p>
 * <pre>
 *   ABIERTO → EN_PROCESO → RESUELTO → CERRADO
 * </pre>
 *
 * <p>Cada estado tiene un significado concreto dentro del flujo de trabajo
 * del sistema de gestión de tickets:</p>
 * <ul>
 *   <li>{@link #ABIERTO}     – el ticket acaba de crearse y aún no se ha atendido.</li>
 *   <li>{@link #EN_PROCESO}  – un agente está trabajando activamente en él.</li>
 *   <li>{@link #RESUELTO}    – el problema ha sido solucionado, pendiente de confirmación.</li>
 *   <li>{@link #CERRADO}     – el ticket ha finalizado y no admite más cambios.</li>
 * </ul>
 *
 * <p>Se persiste en base de datos como {@code STRING} gracias a la anotación
 * {@code @Enumerated(EnumType.STRING)} en la entidad {@link Ticket}.</p>
 *
 * @author Equipo de Desarrollo
 * @version 1.0
 * @since 1.0
 * @see Ticket
 */
public enum EstadoTicket {

    /**
     * El ticket ha sido creado y está pendiente de ser atendido.
     * Es el estado inicial asignado automáticamente al crear un ticket.
     */
    ABIERTO,

    /**
     * Un agente de soporte está trabajando activamente en la resolución del ticket.
     */
    EN_PROCESO,

    /**
     * El problema reportado ha sido solucionado.
     * El ticket puede pasar a {@link #CERRADO} una vez el usuario confirme la solución.
     */
    RESUELTO,

    /**
     * El ticket ha sido finalizado definitivamente y no admite más modificaciones.
     */
    CERRADO
}
