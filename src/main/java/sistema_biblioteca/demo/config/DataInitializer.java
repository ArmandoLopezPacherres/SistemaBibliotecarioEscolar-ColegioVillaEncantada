package sistema_biblioteca.demo.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import sistema_biblioteca.demo.model.Usuario;
import sistema_biblioteca.demo.repository.UsuarioRepository;
import sistema_biblioteca.demo.model.enums.RolUsuario;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        crearUsuarioSiNoExiste("A12345678", "Administrador Principal", RolUsuario.ADMINISTRADOR);
        crearUsuarioSiNoExiste("B87654321", "Bibliotecario Turno Mañana", RolUsuario.BIBLIOTECARIO);
        crearUsuarioSiNoExiste("P11223344", "Profesor de Literatura", RolUsuario.PROFESOR);
        crearUsuarioSiNoExiste("E55667788", "Estudiante Regular", RolUsuario.ESTUDIANTE);
    }

    private void crearUsuarioSiNoExiste(String codigo, String nombre, RolUsuario rol) {
        if (usuarioRepository.findByCodigo(codigo).isEmpty()) {
            Usuario usuario = new Usuario();
            usuario.setNombre(nombre);
            usuario.setCodigo(codigo);
            usuario.setPassword(passwordEncoder.encode("1234"));
            usuario.setRol(rol);

            usuarioRepository.save(usuario);
            System.out.println("Usuario de prueba creado -> Código: " + codigo + " | Rol: " + rol.name());
        }
    }
}
