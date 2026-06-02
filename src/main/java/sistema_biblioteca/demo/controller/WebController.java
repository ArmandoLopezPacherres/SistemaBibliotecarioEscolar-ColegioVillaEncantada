package sistema_biblioteca.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import sistema_biblioteca.demo.repository.UsuarioRepository;
import sistema_biblioteca.demo.service.PrestamoService;
import java.security.Principal;

@Controller
public class WebController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PrestamoService prestamoService;

    private void cargarUsuarioEnModelo(Model model, Principal principal) {
        if (principal != null) {
            usuarioRepository.findByCodigo(principal.getName()).ifPresent(usuario -> {
                model.addAttribute("usuario", usuario);
            });
        }
    }

    @GetMapping("/")
    public String index(Principal principal) {
        if (principal != null) return "redirect:/dashboard";
        return "index";
    }

    @GetMapping("/login")
    public String login(Principal principal, jakarta.servlet.http.HttpServletResponse response) {
        if (principal != null) {
            return "redirect:/dashboard";
        }
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");
        return "login";
    }

    @GetMapping("/about")
    public String about(Principal principal) {
        if (principal != null) return "redirect:/dashboard";
        return "about";
    }

    @GetMapping("/news")
    public String news(Principal principal) {
        if (principal != null) return "redirect:/dashboard";
        return "news";
    }

    @GetMapping("/schedules")
    public String schedules(Principal principal) {
        if (principal != null) return "redirect:/dashboard";
        return "schedules";
    }

    @GetMapping("/dashboard")
    public String dashboard(org.springframework.security.core.Authentication authentication) {
        String rol = authentication.getAuthorities().iterator().next().getAuthority();
        if (rol.equals("ROLE_ADMINISTRADOR")) {
            return "redirect:/panel-admin";
        } else if (rol.equals("ROLE_BIBLIOTECARIO")) {
            return "redirect:/panel-bibliotecario/lectores";
        } else if (rol.equals("ROLE_PROFESOR")) {
            return "redirect:/panel-profesor/buscar-libro";
        } else if (rol.equals("ROLE_ESTUDIANTE")) {
            return "redirect:/estudiante/buscar-libro";
        }
        return "redirect:/";
    }

    @GetMapping("/panel-estudiante")
    public String panelEstudiante(Model model, Principal principal) {
        cargarUsuarioEnModelo(model, principal);
        return "panel-estudiante";
    }

    @GetMapping("/mis-prestamos")
    public String misPrestamos(Model model, Principal principal) {
        cargarUsuarioEnModelo(model, principal);
        if (principal != null) {
            usuarioRepository.findByCodigo(principal.getName()).ifPresent(usuario -> {
                model.addAttribute("prestamos", prestamoService.listarPorUsuario(usuario.getId()));
            });
        }
        return "mis-prestamos";
    }
}
