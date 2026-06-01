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
import sistema_biblioteca.demo.repository.ReservaRepository;
import sistema_biblioteca.demo.repository.PrestamoRepository;
import sistema_biblioteca.demo.model.Usuario;
import sistema_biblioteca.demo.model.Reserva;
import sistema_biblioteca.demo.model.Prestamo;
import sistema_biblioteca.demo.model.enums.EstadoReserva;
import sistema_biblioteca.demo.model.enums.EstadoPrestamo;
import sistema_biblioteca.demo.model.enums.RolUsuario;
import sistema_biblioteca.demo.dto.PerfilDTO;
import sistema_biblioteca.demo.dto.PasswordChangeDTO;
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
    private ReservaRepository reservaRepository;

    @Autowired
    private PrestamoRepository prestamoRepository;

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
        model.addAttribute("libros", libroRepository.findAll().stream().filter(sistema_biblioteca.demo.model.Libro::isActivo).toList());
        return "Estudiante/buscarLibro";
    }

    @GetMapping("/mi-actividad")
    public String miActividad(Model model, Principal principal) {
        if (principal != null) {
            usuarioRepository.findByCodigo(principal.getName()).ifPresent(usuario -> {
                model.addAttribute("usuario", usuario);
                
                List<Prestamo> prestamos = prestamoRepository.findAll().stream()
                        .filter(p -> p.getUsuario() != null && p.getUsuario().getCodigo().equals(principal.getName()))
                        .filter(p -> (p.getEstado() == EstadoPrestamo.ACTIVO || p.getEstado() == EstadoPrestamo.RETRASADO) && p.isEntregado())
                        .sorted((p1, p2) -> {
                            LocalDate d1 = p1.getFechaDevolucionEsperada() != null ? p1.getFechaDevolucionEsperada() : LocalDate.MIN;
                            LocalDate d2 = p2.getFechaDevolucionEsperada() != null ? p2.getFechaDevolucionEsperada() : LocalDate.MIN;
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
                
                List<Prestamo> todosPrestamosUsuario = prestamoRepository.findAll().stream()
                        .filter(p -> p.getUsuario() != null && p.getUsuario().getId().equals(usuario.getId()))
                        .toList();

                List<Reserva> reservas = reservaRepository.findAll().stream()
                        .filter(r -> r.getUsuario() != null && r.getUsuario().getId().equals(usuario.getId()))
                        .filter(r -> {
                            if (r.getEstado() == EstadoReserva.PENDIENTE) return true;
                            if (r.getEstado() == EstadoReserva.RECOGIDA) {
                                boolean yaEntregado = todosPrestamosUsuario.stream()
                                        .anyMatch(p -> p.getLibro() != null && 
                                                       p.getLibro().getId().equals(r.getLibro().getId()) && 
                                                       p.isEntregado() && 
                                                       (p.getFechaPrestamo().isEqual(r.getFechaReserva()) || p.getFechaPrestamo().isAfter(r.getFechaReserva())));
                                return !yaEntregado;
                            }
                            return false;
                        })
                        .sorted((r1, r2) -> {
                            LocalDate d1 = r1.getFechaReserva() != null ? r1.getFechaReserva() : LocalDate.MIN;
                            LocalDate d2 = r2.getFechaReserva() != null ? r2.getFechaReserva() : LocalDate.MIN;
                            return d2.compareTo(d1);
                        })
                        .toList();
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
                        .map(p -> p.getLibro().getCategoria() != null ? p.getLibro().getCategoria().getNombre() : "Sin categoría")
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
            List<Reserva> reservas = reservaRepository.findAll().stream()
                    .filter(r -> r.getUsuario() != null && r.getUsuario().getCodigo().equals(principal.getName()))
                    .filter(r -> r.getEstado() == EstadoReserva.RECOGIDA || r.getEstado() == EstadoReserva.COMPLETADA || r.getEstado() == EstadoReserva.CANCELADA)
                    .sorted((r1, r2) -> {
                        LocalDate d1 = r1.getFechaReserva() != null ? r1.getFechaReserva() : LocalDate.MIN;
                        LocalDate d2 = r2.getFechaReserva() != null ? r2.getFechaReserva() : LocalDate.MIN;
                        if (d1.equals(d2)) {
                            return r2.getId().compareTo(r1.getId());
                        }
                        return d2.compareTo(d1);
                    })
                    .toList();
            model.addAttribute("notificaciones", reservas);
        }
        return "Estudiante/notificaciones";
    }

    @GetMapping("/perfil")
    public String perfil(Model model, Principal principal) {
        cargarUsuarioEnModelo(model, principal);
        if (principal != null) {
            usuarioRepository.findByCodigo(principal.getName()).ifPresent(usuario -> {
                String rolFormatted = usuario.getRol() == RolUsuario.ESTUDIANTE ? "ESTUDIANTE" : usuario.getRol().name();
                String correoMock = usuario.getNombre().toLowerCase().replace(" ", "") + "@gmail.com";
                PerfilDTO perfilDto = new PerfilDTO(
                    usuario.getNombre(),
                    rolFormatted,
                    "15/05/2026", // Fecha simulada o podría venir de la BD si el modelo la tiene
                    correoMock,
                    usuario.getCodigo()
                );
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

        Optional<sistema_biblioteca.demo.model.Libro> optLibro = libroRepository.findById(libroId);
        if (optLibro.isEmpty()) {
            return ResponseEntity.badRequest().body("Libro no encontrado");
        }
        sistema_biblioteca.demo.model.Libro libro = optLibro.get();

        if (!libro.isDisponible() || libro.getStock() <= 0) {
            return ResponseEntity.badRequest().body("El libro no está disponible");
        }

        LocalDate fechaLimite;
        try {
            fechaLimite = LocalDate.parse(fechaDevolucionStr);
        } catch(Exception e) {
            return ResponseEntity.badRequest().body("Fecha inválida");
        }
        if (fechaLimite.isBefore(LocalDate.now())) {
            return ResponseEntity.badRequest().body("La fecha no puede ser en el pasado");
        }

        List<sistema_biblioteca.demo.model.Prestamo> todosPrestamosUsuario = prestamoRepository.findAll().stream()
                .filter(p -> p.getUsuario() != null && p.getUsuario().getId().equals(usuario.getId()))
                .toList();

        long activeReservas = reservaRepository.findAll().stream()
                .filter(r -> r.getUsuario() != null && r.getUsuario().getId().equals(usuario.getId()))
                .filter(r -> {
                    if (r.getEstado() == EstadoReserva.PENDIENTE) return true;
                    if (r.getEstado() == EstadoReserva.RECOGIDA) {
                        boolean yaEntregado = todosPrestamosUsuario.stream()
                                .anyMatch(p -> p.getLibro() != null && 
                                               p.getLibro().getId().equals(r.getLibro().getId()) && 
                                               p.isEntregado() && 
                                               (p.getFechaPrestamo().isEqual(r.getFechaReserva()) || p.getFechaPrestamo().isAfter(r.getFechaReserva())));
                        return !yaEntregado;
                    }
                    return false;
                })
                .count();
        long activePrestamos = prestamoRepository.findAll().stream()
                .filter(p -> p.getUsuario() != null && p.getUsuario().getId().equals(usuario.getId()))
                .filter(p -> (p.getEstado() == EstadoPrestamo.ACTIVO || p.getEstado() == EstadoPrestamo.RETRASADO) && p.isEntregado())
                .count();
        if (activeReservas + activePrestamos >= 3) {
            return ResponseEntity.badRequest().body("Ya tienes el límite máximo de 3 libros (entre solicitudes activas y libros que tienes).");
        }

        Reserva reserva = new Reserva();
        reserva.setUsuario(usuario);
        reserva.setLibro(libro);
        reserva.setFechaReserva(LocalDate.now());
        reserva.setFechaLimiteRecojo(fechaLimite); // Se usa como fecha de devolución
        reserva.setEstado(EstadoReserva.PENDIENTE);
        reserva.setCantidad(1);
        reserva.setNotificado(false);

        reservaRepository.save(reserva);

        return ResponseEntity.ok("Solicitud registrada con éxito. Podrá verla en su Historial.");
    }

    @PostMapping("/cancelar-solicitud")
    @ResponseBody
    public ResponseEntity<String> cancelarSolicitud(@RequestParam("id") Long id, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body("No autorizado");
        }
        Optional<Reserva> optReserva = reservaRepository.findById(id);
        if (optReserva.isEmpty()) {
            return ResponseEntity.badRequest().body("La solicitud no existe");
        }
        Reserva reserva = optReserva.get();
        if (!reserva.getUsuario().getCodigo().equals(principal.getName())) {
            return ResponseEntity.badRequest().body("No puede cancelar una solicitud ajena");
        }
        if (reserva.getEstado() != EstadoReserva.PENDIENTE) {
            return ResponseEntity.badRequest().body("Solo se pueden cancelar solicitudes pendientes");
        }
        
        reservaRepository.delete(reserva);
        
        return ResponseEntity.ok("Solicitud cancelada con éxito");
    }
}
