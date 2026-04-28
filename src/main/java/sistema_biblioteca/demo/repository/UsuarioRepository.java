package sistema_biblioteca.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sistema_biblioteca.demo.model.Usuario;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByCodigo(String codigo);
}