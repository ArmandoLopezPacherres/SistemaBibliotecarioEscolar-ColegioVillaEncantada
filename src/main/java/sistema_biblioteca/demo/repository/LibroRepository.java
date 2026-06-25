package sistema_biblioteca.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sistema_biblioteca.demo.model.Libro;
import java.util.List;

@Repository
public interface LibroRepository extends JpaRepository<Libro, Long> {
    List<Libro> findByTituloContainingIgnoreCaseOrAutorNombreCompletoContainingIgnoreCase(String titulo, String autorNombre);
    List<Libro> findByTituloContainingIgnoreCaseOrAutorNombreCompletoContainingIgnoreCase(String titulo, String autorNombre, org.springframework.data.domain.Pageable pageable);
    List<Libro> findByActivoTrue();

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("UPDATE Libro l SET l.stock = l.stock - :cantidad WHERE l.id = :id AND l.stock >= :cantidad")
    int descontarStock(@org.springframework.data.repository.query.Param("id") Long id, @org.springframework.data.repository.query.Param("cantidad") int cantidad);
}
