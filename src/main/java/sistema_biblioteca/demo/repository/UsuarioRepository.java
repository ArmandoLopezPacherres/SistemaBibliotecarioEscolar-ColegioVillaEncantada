package sistema_biblioteca.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sistema_biblioteca.demo.model.Usuario;
import sistema_biblioteca.demo.model.enums.RolUsuario;
import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByCodigo(String codigo);

    List<Usuario> findByRol(RolUsuario rol);

    List<Usuario> findByCodigoContainingIgnoreCase(String codigo);

    List<Usuario> findByRolAndPuntosLecturaGreaterThanOrderByPuntosLecturaDesc(RolUsuario rol, Integer puntosLectura);
}