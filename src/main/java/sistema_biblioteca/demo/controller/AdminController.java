package sistema_biblioteca.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import sistema_biblioteca.demo.dto.UsuarioDTO;
import sistema_biblioteca.demo.dto.TopUsuarioDTO;
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
import java.util.List;
import java.util.Map;

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
        
        // Calcular Top 5 usuarios
        java.util.Map<Usuario, java.util.List<sistema_biblioteca.demo.model.Prestamo>> prestamosPorUsuario = todosPrestamos.stream()
            .collect(Collectors.groupingBy(sistema_biblioteca.demo.model.Prestamo::getUsuario));
            
        java.util.List<TopUsuarioDTO> topUsuarios = prestamosPorUsuario.entrySet().stream()
            .sorted((e1, e2) -> Integer.compare(e2.getValue().size(), e1.getValue().size()))
            .limit(5)
            .map(entry -> {
                TopUsuarioDTO dto = new TopUsuarioDTO();
                dto.setNombre(entry.getKey().getNombre());
                dto.setRol(entry.getKey().getRol().name());
                
                long totalPrestados = entry.getValue().size();
                long totalDevueltos = entry.getValue().stream().filter(p -> p.getEstado() == EstadoPrestamo.DEVUELTO).count();
                
                dto.setTotalPrestamos(totalPrestados);
                dto.setTotalDevoluciones(totalDevueltos);
                
                long tasa = totalPrestados > 0 ? (totalDevueltos * 100 / totalPrestados) : 0;
                dto.setTasaDevolucion(tasa + "%");
                return dto;
            })
            .collect(Collectors.toList());
            
        // Asignar posición
        for(int i = 0; i < topUsuarios.size(); i++){
            topUsuarios.get(i).setPosicion(i + 1);
        }
        
        model.addAttribute("topUsuarios", topUsuarios);
        
        return "Administrador/Admin-dashboard";
    }

    @GetMapping("/panel-admin/usuarios")
    public String gestionUsuarios(Model model, Principal principal) {
        cargarUsuarioEnModelo(model, principal);
        model.addAttribute("listaUsuarios", usuarioService.listarUsuarios());
        model.addAttribute("nuevoUsuario", new UsuarioDTO());

        model.addAttribute("totalAdmins",
            usuarioService.buscarPorRol(RolUsuario.ADMINISTRADOR).stream().filter(Usuario::isActivo).count());
        model.addAttribute("totalBibliotecarios",
            usuarioService.buscarPorRol(RolUsuario.BIBLIOTECARIO).stream().filter(Usuario::isActivo).count());
        model.addAttribute("totalProfesores",
            usuarioService.buscarPorRol(RolUsuario.PROFESOR).stream().filter(Usuario::isActivo).count());
        model.addAttribute("totalEstudiantes",
            usuarioService.buscarPorRol(RolUsuario.ESTUDIANTE).stream().filter(Usuario::isActivo).count());

        return "Administrador/GestionUsuario";
    }

    @PostMapping("/panel-admin/usuarios/guardar")
    public String guardarUsuario(@ModelAttribute("nuevoUsuario") UsuarioDTO dto, RedirectAttributes redirectAttributes) {
        try {
            Usuario usuario = new Usuario();
            if (dto.getId() != null) {
                usuario = usuarioService.obtenerPorId(dto.getId());
            }

            Long idExcluir = dto.getId() != null ? dto.getId() : -1L;
            if (usuarioService.existePorCodigo(dto.getCodigo(), idExcluir)) {
                redirectAttributes.addFlashAttribute("mensajeError",
                    "Ya existe un usuario con el código \"" + dto.getCodigo() + "\". Elige otro código.");
                return "redirect:/panel-admin/usuarios";
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
    public String inactivarUsuario(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            usuarioService.eliminarUsuario(id);
            redirectAttributes.addFlashAttribute("mensajeExito", "Usuario inactivado exitosamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", "Error al inactivar el usuario.");
        }
        return "redirect:/panel-admin/usuarios";
    }

    @PostMapping("/panel-admin/usuarios/activar/{id}")
    public String activarUsuario(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Usuario usuario = usuarioService.obtenerPorId(id);
            if (usuario != null) {
                usuario.setActivo(true);
                usuarioService.guardarUsuario(usuario);
                redirectAttributes.addFlashAttribute("mensajeExito", "Usuario activado exitosamente.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", "Error al activar el usuario.");
        }
        return "redirect:/panel-admin/usuarios";
    }

    @GetMapping("/panel-admin/catalogo")
    public String gestionCatalogo(
            @RequestParam(required = false, defaultValue = "libros") String tab,
            Model model, Principal principal) {
        cargarUsuarioEnModelo(model, principal);
        List<Libro> libros = libroService.listarLibros();
        model.addAttribute("listaLibros", libros);
        model.addAttribute("listaAutores", autorRepository.findAll());
        model.addAttribute("listaCategorias", categoriaRepository.findAll());
        model.addAttribute("listaEditoriales", editorialRepository.findAll());

        Map<Long, Long> librosPerAutor = libros.stream()
            .filter(l -> l.getAutor() != null)
            .collect(Collectors.groupingBy(l -> l.getAutor().getId(), Collectors.counting()));
        Map<Long, Long> librosPerEditorial = libros.stream()
            .filter(l -> l.getEditorial() != null)
            .collect(Collectors.groupingBy(l -> l.getEditorial().getId(), Collectors.counting()));
        Map<Long, Long> librosPerCategoria = libros.stream()
            .filter(l -> l.getCategoria() != null)
            .collect(Collectors.groupingBy(l -> l.getCategoria().getId(), Collectors.counting()));

        model.addAttribute("librosPerAutor", librosPerAutor);
        model.addAttribute("librosPerEditorial", librosPerEditorial);
        model.addAttribute("librosPerCategoria", librosPerCategoria);
        model.addAttribute("tabActiva", tab);

        return "Administrador/GestionCatalogo";
    }

    @PostMapping("/panel-admin/catalogo/libros/guardar")
    public String guardarLibro(
            @RequestParam(required = false) Long id,
            @RequestParam String isbn,
            @RequestParam String titulo,
            @RequestParam(required = false) Integer anioPublicacion,
            @RequestParam int stock,
            @RequestParam(name = "autor.id", required = false) Long autorId,
            @RequestParam(name = "editorial.id", required = false) Long editorialId,
            @RequestParam(name = "categoria.id", required = false) Long categoriaId,
            RedirectAttributes redirectAttributes) {
        try {
            Libro libro = (id != null) ? libroService.obtenerPorId(id) : new Libro();
            libro.setIsbn(isbn);
            libro.setTitulo(titulo);
            libro.setAnioPublicacion(anioPublicacion);
            libro.setStock(stock);
            libro.setAutor(autorId != null ? autorRepository.findById(autorId).orElse(null) : null);
            libro.setEditorial(editorialId != null ? editorialRepository.findById(editorialId).orElse(null) : null);
            libro.setCategoria(categoriaId != null ? categoriaRepository.findById(categoriaId).orElse(null) : null);
            libroService.guardarLibro(libro);
            redirectAttributes.addFlashAttribute("mensajeExito", "Libro guardado exitosamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", "Error al guardar el libro: " + e.getMessage());
        }
        return "redirect:/panel-admin/catalogo?tab=libros";
    }

    @PostMapping("/panel-admin/catalogo/libros/eliminar/{id}")
    public String eliminarLibro(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            libroService.eliminarLibro(id);
            redirectAttributes.addFlashAttribute("mensajeExito", "Libro eliminado.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", "Error al eliminar el libro.");
        }
        return "redirect:/panel-admin/catalogo?tab=libros";
    }

    @PostMapping("/panel-admin/catalogo/autores/guardar")
    public String guardarAutor(
            @RequestParam(required = false) Long id,
            @RequestParam String nombreCompleto,
            @RequestParam(required = false) String nacionalidad,
            @RequestParam(required = false) String descripcion,
            RedirectAttributes redirectAttributes) {
        try {
            Autor autor = (id != null) ? autorRepository.findById(id).orElse(new Autor()) : new Autor();
            autor.setNombreCompleto(nombreCompleto);
            autor.setNacionalidad(nacionalidad);
            autor.setDescripcion(descripcion);
            autorRepository.save(autor);
            redirectAttributes.addFlashAttribute("mensajeExito", "Autor guardado exitosamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", "Error al guardar el autor.");
        }
        return "redirect:/panel-admin/catalogo?tab=autores";
    }

    @PostMapping("/panel-admin/catalogo/autores/eliminar/{id}")
    public String eliminarAutor(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            autorRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("mensajeExito", "Autor eliminado.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", "Error al eliminar el autor.");
        }
        return "redirect:/panel-admin/catalogo?tab=autores";
    }

    @PostMapping("/panel-admin/catalogo/editoriales/guardar")
    public String guardarEditorial(
            @RequestParam(required = false) Long id,
            @RequestParam String nombre,
            @RequestParam(required = false) String pais,
            @RequestParam(required = false) String correo,
            RedirectAttributes redirectAttributes) {
        try {
            Editorial editorial = (id != null) ? editorialRepository.findById(id).orElse(new Editorial()) : new Editorial();
            editorial.setNombre(nombre);
            editorial.setPais(pais);
            editorial.setCorreo(correo);
            editorialRepository.save(editorial);
            redirectAttributes.addFlashAttribute("mensajeExito", "Editorial guardada exitosamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", "Error al guardar la editorial.");
        }
        return "redirect:/panel-admin/catalogo?tab=editoriales";
    }

    @PostMapping("/panel-admin/catalogo/editoriales/eliminar/{id}")
    public String eliminarEditorial(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            editorialRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("mensajeExito", "Editorial eliminada.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", "Error al eliminar la editorial.");
        }
        return "redirect:/panel-admin/catalogo?tab=editoriales";
    }

    @PostMapping("/panel-admin/catalogo/categorias/guardar")
    public String guardarCategoria(
            @RequestParam(required = false) Long id,
            @RequestParam String nombre,
            @RequestParam(required = false) String descripcion,
            RedirectAttributes redirectAttributes) {
        try {
            Categoria categoria = (id != null) ? categoriaRepository.findById(id).orElse(new Categoria()) : new Categoria();
            categoria.setNombre(nombre);
            categoria.setDescripcion(descripcion);
            categoriaRepository.save(categoria);
            redirectAttributes.addFlashAttribute("mensajeExito", "Categoría guardada exitosamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", "Error al guardar la categoría.");
        }
        return "redirect:/panel-admin/catalogo?tab=categorias";
    }

    @PostMapping("/panel-admin/catalogo/categorias/eliminar/{id}")
    public String eliminarCategoria(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            categoriaRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("mensajeExito", "Categoría eliminada.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", "Error al eliminar la categoría.");
        }
        return "redirect:/panel-admin/catalogo?tab=categorias";
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
    public String actualizarPerfil(
            @RequestParam Long id,
            @RequestParam String nombre,
            @RequestParam(required = false) String passwordActual,
            @RequestParam(required = false) String password,
            @RequestParam(required = false) String confirmarPassword,
            RedirectAttributes redirectAttributes) {
        try {
            Usuario usuario = usuarioService.obtenerPorId(id);
            if (usuario == null) {
                redirectAttributes.addFlashAttribute("mensajeError", "Usuario no encontrado.");
                return "redirect:/panel-admin/perfil";
            }
            usuario.setNombre(nombre);
            boolean cambiarPassword = password != null && !password.isBlank();
            if (cambiarPassword) {
                if (passwordActual == null || passwordActual.isBlank()) {
                    redirectAttributes.addFlashAttribute("mensajeError", "Debes ingresar tu contraseña actual.");
                    return "redirect:/panel-admin/perfil";
                }
                if (!passwordEncoder.matches(passwordActual, usuario.getPassword())) {
                    redirectAttributes.addFlashAttribute("mensajeError", "La contraseña actual es incorrecta.");
                    return "redirect:/panel-admin/perfil";
                }
                if (!password.equals(confirmarPassword)) {
                    redirectAttributes.addFlashAttribute("mensajeError", "Las contraseñas nuevas no coinciden.");
                    return "redirect:/panel-admin/perfil";
                }
                if (password.length() < 6) {
                    redirectAttributes.addFlashAttribute("mensajeError", "La nueva contraseña debe tener al menos 6 caracteres.");
                    return "redirect:/panel-admin/perfil";
                }
                usuario.setPassword(passwordEncoder.encode(password));
            }
            usuarioService.guardarUsuario(usuario);
            redirectAttributes.addFlashAttribute("mensajeExito", "Perfil actualizado exitosamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", "Error al actualizar el perfil.");
        }
        return "redirect:/panel-admin/perfil";
    }
}
