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
import sistema_biblioteca.demo.model.Libro;
import sistema_biblioteca.demo.model.Autor;
import sistema_biblioteca.demo.model.Categoria;
import sistema_biblioteca.demo.model.Editorial;
import sistema_biblioteca.demo.repository.UsuarioRepository;
import sistema_biblioteca.demo.repository.AutorRepository;
import sistema_biblioteca.demo.repository.CategoriaRepository;
import sistema_biblioteca.demo.repository.EditorialRepository;
import sistema_biblioteca.demo.service.UsuarioService;
import sistema_biblioteca.demo.service.LibroService;
import sistema_biblioteca.demo.service.PrestamoService;
import sistema_biblioteca.demo.model.enums.EstadoPrestamo;
import java.util.stream.Collectors;
import java.security.Principal;

@Controller
public class AdminController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private LibroService libroService;

    @Autowired
    private AutorRepository autorRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private EditorialRepository editorialRepository;

    @Autowired
    private PrestamoService prestamoService;

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
        
        long totalLibros = libroService.listarLibros().size();
        model.addAttribute("totalLibros", totalLibros);
        
        long totalUsuarios = usuarioService.listarUsuarios().stream().filter(Usuario::isActivo).count();
        model.addAttribute("totalUsuarios", totalUsuarios);
        
        var todosPrestamos = prestamoService.listarPrestamos();
        long prestamosActivos = todosPrestamos.stream().filter(p -> p.getEstado() == EstadoPrestamo.ACTIVO).count();
        long usuariosMorosos = todosPrestamos.stream().filter(p -> p.getEstado() == EstadoPrestamo.RETRASADO).map(p -> p.getUsuario().getId()).distinct().count();
        
        model.addAttribute("prestamosActivos", prestamosActivos);
        model.addAttribute("usuariosMorosos", usuariosMorosos);
        
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
        model.addAttribute("listaLibros", libroService.listarLibros());
        model.addAttribute("listaAutores", autorRepository.findAll());
        model.addAttribute("listaCategorias", categoriaRepository.findAll());
        model.addAttribute("listaEditoriales", editorialRepository.findAll());
        return "Administrador/GestionCatalogo";
    }

    @PostMapping("/panel-admin/catalogo/libros/guardar")
    public String guardarLibro(Libro libro, RedirectAttributes redirectAttributes) {
        try {
            libroService.guardarLibro(libro);
            redirectAttributes.addFlashAttribute("mensajeExito", "Libro guardado exitosamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", "Error al guardar el libro.");
        }
        return "redirect:/panel-admin/catalogo";
    }

    @PostMapping("/panel-admin/catalogo/libros/eliminar/{id}")
    public String eliminarLibro(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            libroService.eliminarLibro(id);
            redirectAttributes.addFlashAttribute("mensajeExito", "Libro eliminado.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", "Error al eliminar el libro.");
        }
        return "redirect:/panel-admin/catalogo";
    }

    @PostMapping("/panel-admin/catalogo/autores/guardar")
    public String guardarAutor(Autor autor, RedirectAttributes redirectAttributes) {
        try {
            autorRepository.save(autor);
            redirectAttributes.addFlashAttribute("mensajeExito", "Autor guardado exitosamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", "Error al guardar el autor.");
        }
        return "redirect:/panel-admin/catalogo";
    }

    @PostMapping("/panel-admin/catalogo/autores/eliminar/{id}")
    public String eliminarAutor(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            autorRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("mensajeExito", "Autor eliminado.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", "Error al eliminar el autor.");
        }
        return "redirect:/panel-admin/catalogo";
    }

    @PostMapping("/panel-admin/catalogo/editoriales/guardar")
    public String guardarEditorial(Editorial editorial, RedirectAttributes redirectAttributes) {
        try {
            editorialRepository.save(editorial);
            redirectAttributes.addFlashAttribute("mensajeExito", "Editorial guardada exitosamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", "Error al guardar la editorial.");
        }
        return "redirect:/panel-admin/catalogo";
    }

    @PostMapping("/panel-admin/catalogo/editoriales/eliminar/{id}")
    public String eliminarEditorial(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            editorialRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("mensajeExito", "Editorial eliminada.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", "Error al eliminar la editorial.");
        }
        return "redirect:/panel-admin/catalogo";
    }

    @PostMapping("/panel-admin/catalogo/categorias/guardar")
    public String guardarCategoria(Categoria categoria, RedirectAttributes redirectAttributes) {
        try {
            categoriaRepository.save(categoria);
            redirectAttributes.addFlashAttribute("mensajeExito", "Categoría guardada exitosamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", "Error al guardar la categoría.");
        }
        return "redirect:/panel-admin/catalogo";
    }

    @PostMapping("/panel-admin/catalogo/categorias/eliminar/{id}")
    public String eliminarCategoria(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            categoriaRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("mensajeExito", "Categoría eliminada.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", "Error al eliminar la categoría.");
        }
        return "redirect:/panel-admin/catalogo";
    }

    @GetMapping("/panel-admin/reportes")
    public String reportes(Model model, Principal principal) {
        cargarUsuarioEnModelo(model, principal);
        
        var prestamos = prestamoService.listarPrestamos();
        model.addAttribute("listaPrestamos", prestamos);
        
        model.addAttribute("listaUsuarios", usuarioService.listarUsuarios());
        
        var morosos = prestamos.stream()
            .filter(p -> p.getEstado() == EstadoPrestamo.RETRASADO)
            .collect(Collectors.toList());
        model.addAttribute("listaMorosos", morosos);
        
        model.addAttribute("listaCatalogo", libroService.listarLibros());
        
        return "Administrador/Reportes";
    }

    @GetMapping("/panel-admin/perfil")
    public String perfil(Model model, Principal principal) {
        cargarUsuarioEnModelo(model, principal);
        return "Administrador/Perfil";
    }

    @PostMapping("/panel-admin/perfil/actualizar")
    public String actualizarPerfil(@ModelAttribute UsuarioDTO dto, RedirectAttributes redirectAttributes) {
        try {
            Usuario usuario = usuarioService.obtenerPorId(dto.getId());
            if (usuario != null) {
                usuario.setNombre(dto.getNombre());
                // El código y rol no se cambian desde el perfil
                
                if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
                    usuario.setPassword(passwordEncoder.encode(dto.getPassword()));
                }
                
                usuarioService.guardarUsuario(usuario);
                redirectAttributes.addFlashAttribute("mensajeExito", "Perfil actualizado exitosamente.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", "Error al actualizar el perfil.");
        }
        return "redirect:/panel-admin/perfil";
    }
}
