package sistema_biblioteca.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sistema_biblioteca.demo.model.Libro;

@Repository
public interface LibroRepository extends JpaRepository<Libro, Long> {
}
