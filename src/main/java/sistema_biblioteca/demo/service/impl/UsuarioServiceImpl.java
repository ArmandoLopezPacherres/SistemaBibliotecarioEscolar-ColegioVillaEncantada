package sistema_biblioteca.demo.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sistema_biblioteca.demo.model.Usuario;
import sistema_biblioteca.demo.model.enums.RolUsuario;
import sistema_biblioteca.demo.repository.UsuarioRepository;
import sistema_biblioteca.demo.service.UsuarioService;

import java.util.List;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public List<Usuario> listarUsuarios() {
        return usuarioRepository.findAll();
    }

    @Override
    public List<Usuario> buscarPorRol(RolUsuario rol) {
        return usuarioRepository.findByRol(rol);
    }

    @Override
    public Usuario guardarUsuario(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }

    @Override
    public Usuario obtenerPorId(Long id) {
        return usuarioRepository.findById(id).orElse(null);
    }

    @Override
    public void eliminarUsuario(Long id) {
        usuarioRepository.findById(id).ifPresent(usuario -> {
            usuario.setActivo(false);
            usuarioRepository.save(usuario);
        });
    }

    @Override
    public boolean existePorCodigo(String codigo, Long idExcluir) {
        return usuarioRepository.findByCodigo(codigo)
            .filter(u -> !u.getId().equals(idExcluir))
            .isPresent();
    }

    @Override
    public List<Usuario> obtenerTodosLectoresEstudiantes() {
        return usuarioRepository.findByRolAndPuntosLecturaGreaterThanOrderByPuntosLecturaDesc(RolUsuario.ESTUDIANTE, 0);
    }
}