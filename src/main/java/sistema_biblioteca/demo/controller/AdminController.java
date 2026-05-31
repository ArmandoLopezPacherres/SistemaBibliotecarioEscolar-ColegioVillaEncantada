package sistema_biblioteca.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import sistema_biblioteca.demo.repository.UsuarioRepository;
import java.security.Principal;

@Controller
public class AdminController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    private void cargarUsuarioEnModelo(Model model, Principal principal) {
        if (principal != null) {
            usuarioRepository.findByCodigo(principal.getName()).ifPresent(usuario -> {
                model.addAttribute("usuario", usuario);
            });
        }
    }

    @GetMapping("/panel-admin")
    public String panelAdmin(Model model, Principal principal) {
        cargarUsuarioEnModelo(model, principal);
        return "Administrador/Admin-dashboard";
    }

    @GetMapping("/panel-admin/usuarios")
    public String gestionUsuarios(Model model, Principal principal) {
        cargarUsuarioEnModelo(model, principal);
        return "Administrador/GestionUsuario";
    }

    @GetMapping("/panel-admin/catalogo")
    public String gestionCatalogo(Model model, Principal principal) {
        cargarUsuarioEnModelo(model, principal);
        return "Administrador/GestionCatalogo";
    }

    @GetMapping("/panel-admin/reportes")
    public String reportes(Model model, Principal principal) {
        cargarUsuarioEnModelo(model, principal);
        return "Administrador/Reportes";
    }

    @GetMapping("/panel-admin/perfil")
    public String perfil(Model model, Principal principal) {
        cargarUsuarioEnModelo(model, principal);
        return "Administrador/Perfil";
    }
}
