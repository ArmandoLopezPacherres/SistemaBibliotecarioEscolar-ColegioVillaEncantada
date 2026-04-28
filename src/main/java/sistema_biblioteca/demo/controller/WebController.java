package sistema_biblioteca.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import sistema_biblioteca.demo.repository.UsuarioRepository;
import java.security.Principal;

@Controller
public class WebController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    private void cargarUsuarioEnModelo(Model model, Principal principal) {
        if (principal != null) {
            usuarioRepository.findByCodigo(principal.getName()).ifPresent(usuario -> {
                model.addAttribute("usuario", usuario);
            });
        }
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/about")
    public String about() {
        return "about";
    }

    @GetMapping("/news")
    public String news() {
        return "news";
    }

    @GetMapping("/schedules")
    public String schedules() {
        return "schedules";
    }

    @GetMapping("/dashboard")
    public String dashboard(org.springframework.security.core.Authentication authentication) {
        String rol = authentication.getAuthorities().iterator().next().getAuthority();
        if (rol.equals("ROLE_ADMINISTRADOR")) {
            return "redirect:/panel-admin";
        } else if (rol.equals("ROLE_BIBLIOTECARIO")) {
            return "redirect:/panel-bibliotecario";
        } else if (rol.equals("ROLE_PROFESOR")) {
            return "redirect:/panel-profesor";
        } else if (rol.equals("ROLE_ESTUDIANTE")) {
            return "redirect:/panel-estudiante";
        }
        return "redirect:/";
    }

    @GetMapping("/panel-admin")
    public String panelAdmin(Model model, Principal principal) {
        cargarUsuarioEnModelo(model, principal);
        return "panel-admin";
    }

    @GetMapping("/panel-bibliotecario")
    public String panelBibliotecario(Model model, Principal principal) {
        cargarUsuarioEnModelo(model, principal);
        return "panel-bibliotecario";
    }

    @GetMapping("/panel-profesor")
    public String panelProfesor(Model model, Principal principal) {
        cargarUsuarioEnModelo(model, principal);
        return "panel-profesor";
    }

    @GetMapping("/panel-estudiante")
    public String panelEstudiante(Model model, Principal principal) {
        cargarUsuarioEnModelo(model, principal);
        return "panel-estudiante";
    }
}
