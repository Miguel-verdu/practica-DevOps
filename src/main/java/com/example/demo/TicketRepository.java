package com.example.demo;

import com.example.demo.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio JPA para la entidad {@link Ticket}.
 *
 * <p>Extiende {@link JpaRepository} para heredar de forma automática las
 * operaciones CRUD estándar sin necesidad de implementarlas manualmente.
 * Spring Data JPA genera en tiempo de ejecución una implementación concreta
 * de esta interfaz.</p>
 *
 * <p>Operaciones heredadas disponibles (selección):</p>
 * <ul>
 *   <li>{@code save(Ticket)} – persiste o actualiza un ticket.</li>
 *   <li>{@code findById(Long)} – busca un ticket por su clave primaria.</li>
 *   <li>{@code findAll()} – devuelve todos los tickets almacenados.</li>
 *   <li>{@code deleteById(Long)} – elimina un ticket por su clave primaria.</li>
 *   <li>{@code count()} – devuelve el número total de tickets.</li>
 * </ul>
 *
 * <p>Para añadir consultas personalizadas basta con declarar métodos siguiendo
 * las convenciones de nombres de Spring Data (query derivation) o usar la
 * anotación {@code @Query} con JPQL/SQL nativo. Por ejemplo:</p>
 * <pre>
 *   List&lt;Ticket&gt; findByEstado(EstadoTicket estado);
 *   List&lt;Ticket&gt; findByTituloContainingIgnoreCase(String texto);
 * </pre>
 *
 * @author Miguel Verdú Pacheco
 * @version 1.0
 * @since 1.0
 * @see Ticket
 * @see JpaRepository
 */
@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
}
