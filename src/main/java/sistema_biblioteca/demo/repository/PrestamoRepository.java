package sistema_biblioteca.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sistema_biblioteca.demo.model.Prestamo;
import java.util.List;

public interface PrestamoRepository extends JpaRepository<Prestamo, Long> {

    List<Prestamo> findByUsuarioId(Long usuarioId);
}