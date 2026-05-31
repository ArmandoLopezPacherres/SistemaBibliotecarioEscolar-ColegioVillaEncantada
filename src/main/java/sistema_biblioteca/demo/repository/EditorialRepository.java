package sistema_biblioteca.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sistema_biblioteca.demo.model.Editorial;

@Repository
public interface EditorialRepository extends JpaRepository<Editorial, Long> {
}
