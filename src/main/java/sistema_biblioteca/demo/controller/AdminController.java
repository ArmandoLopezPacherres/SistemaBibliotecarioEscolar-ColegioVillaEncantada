package sistema_biblioteca.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import sistema_biblioteca.demo.dto.UsuarioDTO;
import sistema_biblioteca.demo.model.Usuario;
import sistema_biblioteca.demo.model.enums.RolUsuario;
import sistema_biblioteca.demo.repository.UsuarioRepository;
import sistema_biblioteca.demo.service.UsuarioService;
import java.security.Principal;

@Controller
public class AdminController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private PasswordEncoder passwordEncoder;

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
        model.addAttribute("listaUsuarios", usuarioService.listarUsuarios());
        model.addAttribute("nuevoUsuario", new UsuarioDTO());
        
        model.addAttribute("totalAdmins", usuarioService.buscarPorRol(RolUsuario.ADMINISTRADOR).size());
        model.addAttribute("totalBibliotecarios", usuarioService.buscarPorRol(RolUsuario.BIBLIOTECARIO).size());
        model.addAttribute("totalProfesores", usuarioService.buscarPorRol(RolUsuario.PROFESOR).size());
        model.addAttribute("totalEstudiantes", usuarioService.buscarPorRol(RolUsuario.ESTUDIANTE).size());
        
        return "Administrador/GestionUsuario";
    }

    @PostMapping("/panel-admin/usuarios/guardar")
    public String guardarUsuario(@ModelAttribute("nuevoUsuario") UsuarioDTO dto, RedirectAttributes redirectAttributes) {
        try {
            Usuario usuario = new Usuario();
            if (dto.getId() != null) {
                usuario = usuarioService.obtenerPorId(dto.getId());
            }
            
            String nombreCompleto = dto.getNombre() != null ? dto.getNombre() : "";
            if (dto.getApellido() != null && !dto.getApellido().isEmpty()) {
                nombreCompleto += " " + dto.getApellido();
            }
            usuario.setNombre(nombreCompleto.trim());
            usuario.setCodigo(dto.getCodigo());
            usuario.setRol(dto.getRol());

            if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
                usuario.setPassword(passwordEncoder.encode(dto.getPassword()));
            }

            usuarioService.guardarUsuario(usuario);
            redirectAttributes.addFlashAttribute("mensajeExito", "Usuario guardado exitosamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", "Error al guardar el usuario.");
        }
        return "redirect:/panel-admin/usuarios";
    }

    @PostMapping("/panel-admin/usuarios/eliminar/{id}")
    public String eliminarUsuario(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            usuarioService.eliminarUsuario(id);
            redirectAttributes.addFlashAttribute("mensajeExito", "Usuario eliminado exitosamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", "Error al eliminar el usuario. Puede que tenga préstamos asociados.");
        }
        return "redirect:/panel-admin/usuarios";
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
