package sistema_biblioteca.demo.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import sistema_biblioteca.demo.model.Usuario;
import sistema_biblioteca.demo.repository.UsuarioRepository;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String codigo) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByCodigo(codigo)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con el codigo: " + codigo));

        return User.builder()
                .username(usuario.getCodigo())
                .password(usuario.getPassword())
                .roles(usuario.getRol().name())
                .build();
    }
}
