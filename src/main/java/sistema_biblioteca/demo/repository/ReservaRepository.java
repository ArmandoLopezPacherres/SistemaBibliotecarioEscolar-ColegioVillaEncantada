package sistema_biblioteca.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sistema_biblioteca.demo.model.Reserva;
import sistema_biblioteca.demo.model.enums.EstadoReserva;
import java.util.List;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {
    List<Reserva> findByEstado(EstadoReserva estado);

    @Query("SELECT r FROM Reserva r WHERE r.estado = :estado AND " +
           "(LOWER(r.usuario.nombre) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(r.libro.titulo) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Reserva> buscarPendientesPorUsuarioOLibro(@Param("estado") EstadoReserva estado, @Param("query") String query);

    List<Reserva> findByUsuarioId(Long usuarioId);
}
