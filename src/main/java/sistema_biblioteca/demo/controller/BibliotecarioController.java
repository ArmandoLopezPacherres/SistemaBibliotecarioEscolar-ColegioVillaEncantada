package sistema_biblioteca.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import sistema_biblioteca.demo.model.Usuario;
import sistema_biblioteca.demo.model.Prestamo;
import sistema_biblioteca.demo.model.Libro;
import sistema_biblioteca.demo.model.enums.RolUsuario;
import sistema_biblioteca.demo.model.enums.EstadoPrestamo;
import sistema_biblioteca.demo.repository.UsuarioRepository;
import sistema_biblioteca.demo.repository.PrestamoRepository;
import sistema_biblioteca.demo.repository.LibroRepository;
import java.security.Principal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/panel-bibliotecario")
public class BibliotecarioController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PrestamoRepository prestamoRepository;

    @Autowired
    private LibroRepository libroRepository;

    private void cargarUsuarioEnModelo(Model model, Principal principal) {
        if (principal != null) {
            usuarioRepository.findByCodigo(principal.getName()).ifPresent(usuario -> {
                model.addAttribute("usuario", usuario);
            });
        }
    }

    @GetMapping
    public String panelBibliotecario() {
        return "redirect:/panel-bibliotecario/prestamos";
    }

    @GetMapping("/prestamos")
    public String prestamos(Model model, Principal principal) {
        cargarUsuarioEnModelo(model, principal);
        return "Bibliotecario/prestamos";
    }

    @GetMapping("/devoluciones")
    public String devoluciones(Model model, Principal principal) {
        cargarUsuarioEnModelo(model, principal);
        return "Bibliotecario/devoluciones";
    }

    @GetMapping("/inventario")
    public String inventario(Model model, Principal principal) {
        cargarUsuarioEnModelo(model, principal);
        model.addAttribute("libros", libroRepository.findAll());
        return "Bibliotecario/inventario";
    }

    @GetMapping("/inventario/buscar")
    @ResponseBody
    public ResponseEntity<List<Libro>> buscarLibrosAjax(@RequestParam("query") String query) {
        List<Libro> libros;
        if (query == null || query.trim().isEmpty()) {
            libros = libroRepository.findAll();
        } else {
            libros = libroRepository.findByTituloContainingIgnoreCaseOrAutorNombreCompletoContainingIgnoreCase(query.trim(), query.trim());
        }
        return ResponseEntity.ok(libros);
    }

    private String calcularEstadoLector(Usuario usuario) {
        if (usuario == null || usuario.getId() == null) {
            return "INACTIVO";
        }
        List<Prestamo> prestamos = prestamoRepository.findByUsuarioId(usuario.getId());
        if (prestamos == null || prestamos.isEmpty()) {
            return "INACTIVO";
        }

        LocalDate hoy = LocalDate.now();
        boolean tieneActivo = false;

        for (Prestamo p : prestamos) {
            if (p.getEstado() == EstadoPrestamo.RETRASADO) {
                return "MULTADO";
            }
            if (p.getEstado() == EstadoPrestamo.ACTIVO) {
                if (p.getFechaDevolucionEsperada() != null && hoy.isAfter(p.getFechaDevolucionEsperada())) {
                    return "MULTADO";
                }
                tieneActivo = true;
            }
        }

        if (tieneActivo) {
            return "ACTIVO";
        }
        return "INACTIVO";
    }

    @GetMapping("/lectores")
    public String lectores(
            @RequestParam(value = "search", required = false) String search,
            Model model,
            Principal principal) {
        cargarUsuarioEnModelo(model, principal);

        List<Usuario> lectores = new ArrayList<>();
        if (search != null && !search.trim().isEmpty()) {
            List<Usuario> matches = usuarioRepository.findByCodigoContainingIgnoreCase(search.trim());
            lectores = matches.stream()
                    .filter(u -> u.getRol() == RolUsuario.ESTUDIANTE || u.getRol() == RolUsuario.PROFESOR)
                    .toList();
            for (Usuario u : lectores) {
                u.setEstadoLector(calcularEstadoLector(u));
            }
            model.addAttribute("searchValue", search);
        }

        model.addAttribute("lectores", lectores);
        return "Bibliotecario/lectores";
    }

    @GetMapping("/lectores/buscar")
    @ResponseBody
    public ResponseEntity<List<Usuario>> buscarLectorAjax(@RequestParam("codigo") String codigo) {
        List<Usuario> matches = usuarioRepository.findByCodigoContainingIgnoreCase(codigo);
        List<Usuario> lectores = matches.stream()
                .filter(u -> u.getRol() == RolUsuario.ESTUDIANTE || u.getRol() == RolUsuario.PROFESOR)
                .toList();
        for (Usuario u : lectores) {
            u.setEstadoLector(calcularEstadoLector(u));
        }
        return ResponseEntity.ok(lectores);
    }

    @GetMapping("/multas")
    public String multas(Model model, Principal principal) {
        cargarUsuarioEnModelo(model, principal);
        return "Bibliotecario/multas";
    }

    @GetMapping("/notificaciones")
    public String notificaciones(Model model, Principal principal) {
        cargarUsuarioEnModelo(model, principal);
        return "Bibliotecario/notificaciones";
    }

    @GetMapping("/perfil")
    public String perfil(Model model, Principal principal) {
        cargarUsuarioEnModelo(model, principal);
        return "Bibliotecario/perfil";
    }

    @GetMapping("/solicitudes")
    public String solicitudes(Model model, Principal principal) {
        cargarUsuarioEnModelo(model, principal);
        return "Bibliotecario/solicitudes";
    }
}
