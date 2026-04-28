package sistema_biblioteca.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sistema_biblioteca.demo.model.Prestamo;

public interface PrestamoRepository extends JpaRepository<Prestamo, Long> {
}