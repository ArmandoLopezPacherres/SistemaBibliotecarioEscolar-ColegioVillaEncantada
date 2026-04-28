package sistema_biblioteca.demo.service;

import sistema_biblioteca.demo.model.Usuario;
import java.util.List;

public interface UsuarioService {

    List<Usuario> listarUsuarios();

    Usuario guardarUsuario(Usuario usuario);

    Usuario obtenerPorId(Long id);

    void eliminarUsuario(Long id);
}