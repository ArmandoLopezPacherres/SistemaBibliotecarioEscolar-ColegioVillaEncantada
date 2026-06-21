package sistema_biblioteca.demo.service;

import sistema_biblioteca.demo.model.Usuario;
import sistema_biblioteca.demo.model.enums.RolUsuario;
import java.util.List;

public interface UsuarioService {

    List<Usuario> listarUsuarios();

    List<Usuario> buscarPorRol(RolUsuario rol);

    Usuario guardarUsuario(Usuario usuario);

    Usuario obtenerPorId(Long id);

    void eliminarUsuario(Long id);

    boolean existePorCodigo(String codigo, Long idExcluir);

    List<Usuario> obtenerTodosLectoresEstudiantes();
}