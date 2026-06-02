package sistema_biblioteca.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.security.crypto.password.PasswordEncoder;
import sistema_biblioteca.demo.repository.UsuarioRepository;
import sistema_biblioteca.demo.repository.LibroRepository;
import sistema_biblioteca.demo.repository.PrestamoRepository;
import sistema_biblioteca.demo.model.Usuario;
import sistema_biblioteca.demo.model.Reserva;
import sistema_biblioteca.demo.model.Prestamo;
import sistema_biblioteca.demo.model.enums.EstadoPrestamo;
import sistema_biblioteca.demo.model.enums.RolUsuario;
import sistema_biblioteca.demo.dto.PerfilDTO;
import sistema_biblioteca.demo.dto.PasswordChangeDTO;
import sistema_biblioteca.demo.service.ReservaService;
import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/estudiante")
public class EstudianteController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private LibroRepository libroRepository;

    @Autowired
    private PrestamoRepository prestamoRepository;

    @Autowired
    private ReservaService reservaService;

    private void cargarUsuarioEnModelo(Model model, Principal principal) {
        if (principal != null) {
            usuarioRepository.findByCodigo(principal.getName()).ifPresent(usuario -> {
                model.addAttribute("usuario", usuario);
            });
        }
    }

    @GetMapping("/buscar-libro")
    public String buscarLibro(Model model, Principal principal) {
        cargarUsuarioEnModelo(model, principal);
        model.addAttribute("libros",
                libroRepository.findAll().stream().filter(sistema_biblioteca.demo.model.Libro::isActivo).toList());
        return "Estudiante/buscarLibro";
    }

    @GetMapping("/mi-actividad")
    public String miActividad(Model model, Principal principal) {
        if (principal != null) {
            usuarioRepository.findByCodigo(principal.getName()).ifPresent(usuario -> {
                model.addAttribute("usuario", usuario);

                List<EstadoPrestamo> estadosPrestamoActivo = java.util.Arrays.asList(EstadoPrestamo.ACTIVO,
                        EstadoPrestamo.RETRASADO);
                List<Prestamo> prestamos = prestamoRepository
                        .findByUsuarioIdAndEstadoInAndEntregadoTrue(usuario.getId(), estadosPrestamoActivo)
                        .stream()
                        .sorted((p1, p2) -> {
                            LocalDate d1 = p1.getFechaDevolucionEsperada() != null ? p1.getFechaDevolucionEsperada()
                                    : LocalDate.MIN;
                            LocalDate d2 = p2.getFechaDevolucionEsperada() != null ? p2.getFechaDevolucionEsperada()
                                    : LocalDate.MIN;
                            return d1.compareTo(d2);
                        })
                        .toList();
                model.addAttribute("prestamos", prestamos);

                List<LocalDate> fechasEntrega = prestamos.stream()
                        .map(Prestamo::getFechaDevolucionEsperada)
                        .filter(java.util.Objects::nonNull)
                        .distinct()
                        .sorted()
                        .toList();
                model.addAttribute("fechasEntrega", fechasEntrega);

                List<Reserva> reservas = reservaService.obtenerReservasActivas(usuario);
                model.addAttribute("reservas", reservas);

                List<LocalDate> fechasSolicitud = reservas.stream()
                        .map(Reserva::getFechaReserva)
                        .filter(java.util.Objects::nonNull)
                        .distinct()
                        .sorted()
                        .toList();
                model.addAttribute("fechasSolicitud", fechasSolicitud);
            });
        }
        return "Estudiante/miActividad";
    }

    @GetMapping("/historial")
    public String historial(Model model, Principal principal) {
        if (principal != null) {
            usuarioRepository.findByCodigo(principal.getName()).ifPresent(usuario -> {
                model.addAttribute("usuario", usuario);

                List<Prestamo> historial = prestamoRepository.findByUsuarioId(usuario.getId()).stream()
                        .filter(p -> p.getEstado() == EstadoPrestamo.DEVUELTO)
                        .sorted(java.util.Comparator.comparing(Prestamo::getFechaDevolucionReal).reversed())
                        .toList();
                model.addAttribute("historial", historial);

                List<String> categorias = historial.stream()
                        .map(p -> p.getLibro().getCategoria() != null ? p.getLibro().getCategoria().getNombre()
                                : "Sin categoría")
                        .distinct()
                        .sorted()
                        .toList();
                model.addAttribute("categorias", categorias);

                List<LocalDate> fechasEntrega = historial.stream()
                        .map(Prestamo::getFechaPrestamo)
                        .filter(java.util.Objects::nonNull)
                        .distinct()
                        .sorted()
                        .toList();
                model.addAttribute("fechasEntrega", fechasEntrega);

                List<LocalDate> fechasDevolucion = historial.stream()
                        .map(Prestamo::getFechaDevolucionReal)
                        .filter(java.util.Objects::nonNull)
                        .distinct()
                        .sorted()
                        .toList();
                model.addAttribute("fechasDevolucion", fechasDevolucion);
            });
        }
        return "Estudiante/historial";
    }

    @GetMapping("/notificaciones")
    public String notificaciones(Model model, Principal principal) {
        cargarUsuarioEnModelo(model, principal);
        if (principal != null) {
            usuarioRepository.findByCodigo(principal.getName()).ifPresent(usuario -> {
                model.addAttribute("notificaciones", reservaService.obtenerNotificaciones(usuario));
            });
        }
        return "Estudiante/notificaciones";
    }

    @GetMapping("/perfil")
    public String perfil(Model model, Principal principal) {
        cargarUsuarioEnModelo(model, principal);
        if (principal != null) {
            usuarioRepository.findByCodigo(principal.getName()).ifPresent(usuario -> {
                String rolFormatted = usuario.getRol() == RolUsuario.ESTUDIANTE ? "ESTUDIANTE"
                        : usuario.getRol().name();
                String correoMock = usuario.getNombre().toLowerCase().replace(" ", "") + "@gmail.com";
                PerfilDTO perfilDto = new PerfilDTO(
                        usuario.getNombre(),
                        rolFormatted,
                        "15/05/2026", // Fecha simulada o podría venir de la BD si el modelo la tiene
                        correoMock,
                        usuario.getCodigo());
                model.addAttribute("perfil", perfilDto);
            });
        }
        return "Estudiante/perfil";
    }

    @PostMapping("/perfil/cambiar-password")
    @ResponseBody
    public ResponseEntity<String> cambiarPassword(
            PasswordChangeDTO dto,
            Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body("Usuario no autenticado.");
        }
        if (dto == null ||
                dto.getCurrentPassword() == null || dto.getCurrentPassword().trim().isEmpty() ||
                dto.getNewPassword() == null || dto.getNewPassword().trim().isEmpty() ||
                dto.getConfirmPassword() == null || dto.getConfirmPassword().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Todos los campos de contraseña son obligatorios.");
        }
        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            return ResponseEntity.badRequest().body("La nueva contraseña y su confirmación no coinciden.");
        }

        Optional<Usuario> optUsuario = usuarioRepository.findByCodigo(principal.getName());
        if (optUsuario.isEmpty()) {
            return ResponseEntity.badRequest().body("El usuario no existe.");
        }

        Usuario usuario = optUsuario.get();
        if (!passwordEncoder.matches(dto.getCurrentPassword(), usuario.getPassword())) {
            return ResponseEntity.badRequest().body("La contraseña actual es incorrecta.");
        }

        usuario.setPassword(passwordEncoder.encode(dto.getNewPassword().trim()));
        usuarioRepository.save(usuario);

        return ResponseEntity.ok("Contraseña actualizada con éxito.");
    }

    @PostMapping("/solicitar")
    @ResponseBody
    public ResponseEntity<String> solicitarLibro(
            @RequestParam("libroId") Long libroId,
            @RequestParam("fechaDevolucion") String fechaDevolucionStr,
            Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body("No autorizado");
        }
        Optional<Usuario> optUser = usuarioRepository.findByCodigo(principal.getName());
        if (optUser.isEmpty()) {
            return ResponseEntity.badRequest().body("Usuario no encontrado");
        }
        Usuario usuario = optUser.get();

        LocalDate fechaLimite;
        try {
            fechaLimite = LocalDate.parse(fechaDevolucionStr);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Fecha inválida");
        }

        try {
            reservaService.solicitarReserva(usuario, libroId, fechaLimite);
            return ResponseEntity.ok("Solicitud registrada con éxito. Podrá verla en su Historial.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/cancelar-solicitud")
    @ResponseBody
    public ResponseEntity<String> cancelarSolicitud(@RequestParam("id") Long id, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body("No autorizado");
        }
        Optional<Usuario> optUser = usuarioRepository.findByCodigo(principal.getName());
        if (optUser.isEmpty()) {
            return ResponseEntity.badRequest().body("Usuario no encontrado");
        }

        try {
            reservaService.cancelarReserva(id, optUser.get());
            return ResponseEntity.ok("Solicitud cancelada con éxito");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
