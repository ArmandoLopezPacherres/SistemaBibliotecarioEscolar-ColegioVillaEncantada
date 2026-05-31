package sistema_biblioteca.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import sistema_biblioteca.demo.repository.UsuarioRepository;
import java.security.Principal;

@Controller
@RequestMapping("/panel-bibliotecario")
public class BibliotecarioController {

    @Autowired
    private UsuarioRepository usuarioRepository;

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
        return "Bibliotecario/inventario";
    }

    @GetMapping("/lectores")
    public String lectores(Model model, Principal principal) {
        cargarUsuarioEnModelo(model, principal);
        return "Bibliotecario/lectores";
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
