package sistema_biblioteca.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sistema_biblioteca.demo.model.Prestamo;
import java.util.List;

public interface PrestamoRepository extends JpaRepository<Prestamo, Long> {

    List<Prestamo> findByUsuarioId(Long usuarioId);

    List<Prestamo> findByUsuarioIdAndEstadoInAndEntregadoTrue(Long usuarioId, List<sistema_biblioteca.demo.model.enums.EstadoPrestamo> estados);

    long countByUsuarioIdAndEstadoInAndEntregadoTrue(Long usuarioId, List<sistema_biblioteca.demo.model.enums.EstadoPrestamo> estados);

    long countByEstado(sistema_biblioteca.demo.model.enums.EstadoPrestamo estado);

    @org.springframework.data.jpa.repository.Query("SELECT COUNT(DISTINCT p.usuario.id) FROM Prestamo p WHERE p.estado = :estado")
    long countDistinctUsuariosByEstado(@org.springframework.data.repository.query.Param("estado") sistema_biblioteca.demo.model.enums.EstadoPrestamo estado);
}