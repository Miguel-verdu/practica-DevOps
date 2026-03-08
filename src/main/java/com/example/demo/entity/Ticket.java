package com.example.demo.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * Entidad JPA que representa un ticket de soporte dentro del sistema.
 *
 * <p>Cada ticket recoge una incidencia o solicitud de un usuario y se persiste
 * en la tabla {@code tickets} de la base de datos. Al instanciarse, se asignan
 * automáticamente la fecha de creación y el estado inicial {@link EstadoTicket#ABIERTO}.</p>
 *
 * <p>Restricciones de validación aplicadas (Bean Validation):</p>
 * <ul>
 *   <li>{@code titulo}      – obligatorio, entre 5 y 100 caracteres.</li>
 *   <li>{@code descripcion} – obligatoria, entre 10 y 1000 caracteres.</li>
 *   <li>{@code estado}      – obligatorio, debe ser un valor de {@link EstadoTicket}.</li>
 * </ul>
 *
 * @author Equipo de Desarrollo
 * @version 1.0
 * @since 1.0
 * @see EstadoTicket
 */
@Entity
@Table(name = "tickets")
public class Ticket {

    /**
     * Identificador único del ticket generado automáticamente por la base de datos.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Título breve y descriptivo del ticket.
     * Debe tener entre 5 y 100 caracteres y no puede estar en blanco.
     */
    @NotBlank(message = "El título es obligatorio")
    @Size(min = 5, max = 100, message = "El título debe tener entre 5 y 100 caracteres")
    @Column(nullable = false)
    private String titulo;

    /**
     * Descripción detallada del problema o solicitud reportada.
     * Debe tener entre 10 y 1000 caracteres y no puede estar en blanco.
     */
    @NotBlank(message = "La descripción es obligatoria")
    @Size(min = 10, max = 1000, message = "La descripción debe tener al menos 10 caracteres")
    @Column(length = 1000)
    private String descripcion;

    /**
     * Estado actual del ticket dentro de su ciclo de vida.
     * Se almacena como texto en la base de datos. No puede ser nulo.
     *
     * @see EstadoTicket
     */
    @Enumerated(EnumType.STRING)
    @NotNull(message = "El estado es obligatorio")
    private EstadoTicket estado;

    /**
     * Fecha y hora exacta en que el ticket fue creado.
     * Se asigna automáticamente en el constructor y no puede ser nula.
     */
    @Column(nullable = false)
    private LocalDateTime fechaCreacion;

    // --- CONSTRUCTOR ---

    /**
     * Constructor por defecto que inicializa el ticket con valores predeterminados.
     *
     * <p>Al crear un ticket:</p>
     * <ul>
     *   <li>{@code fechaCreacion} se establece al instante actual ({@link LocalDateTime#now()}).</li>
     *   <li>{@code estado} se fija en {@link EstadoTicket#ABIERTO} como punto de partida.</li>
     * </ul>
     */
    public Ticket() {
        this.fechaCreacion = LocalDateTime.now();
        this.estado = EstadoTicket.ABIERTO;
    }

    // --- GETTERS Y SETTERS ---

    /**
     * Devuelve el identificador único del ticket.
     *
     * @return el ID generado por la base de datos, o {@code null} si el ticket
     *         aún no ha sido persistido.
     */
    public Long getId() {
        return id;
    }

    /**
     * Establece el identificador del ticket.
     * Normalmente lo gestiona JPA de forma automática.
     *
     * @param id el nuevo identificador a asignar.
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Devuelve el título del ticket.
     *
     * @return cadena con el título, nunca {@code null} ni en blanco en un ticket válido.
     */
    public String getTitulo() {
        return titulo;
    }

    /**
     * Establece el título del ticket.
     *
     * @param titulo texto breve que describe la incidencia; debe tener entre 5 y 100 caracteres.
     */
    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    /**
     * Devuelve la descripción detallada del ticket.
     *
     * @return cadena con la descripción completa del problema o solicitud.
     */
    public String getDescripcion() {
        return descripcion;
    }

    /**
     * Establece la descripción detallada del ticket.
     *
     * @param descripcion texto extenso con los detalles de la incidencia;
     *                    debe tener entre 10 y 1000 caracteres.
     */
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    /**
     * Devuelve el estado actual del ticket.
     *
     * @return valor de {@link EstadoTicket} que representa la fase actual del ticket.
     */
    public EstadoTicket getEstado() {
        return estado;
    }

    /**
     * Actualiza el estado del ticket.
     *
     * @param estado nuevo estado a asignar; no puede ser {@code null}.
     * @see EstadoTicket
     */
    public void setEstado(EstadoTicket estado) {
        this.estado = estado;
    }

    /**
     * Devuelve la fecha y hora de creación del ticket.
     *
     * @return {@link LocalDateTime} con el instante en que se creó el ticket.
     */
    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    /**
     * Establece la fecha y hora de creación del ticket.
     * Normalmente no es necesario llamar a este método manualmente,
     * ya que el constructor lo inicializa de forma automática.
     *
     * @param fechaCreacion fecha y hora de creación a asignar; no debería ser {@code null}.
     */
    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
}
