package sistema_biblioteca.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sistema_biblioteca.demo.model.Libro;
import sistema_biblioteca.demo.model.Prestamo;
import sistema_biblioteca.demo.model.Reserva;
import sistema_biblioteca.demo.model.Usuario;
import sistema_biblioteca.demo.model.enums.EstadoPrestamo;
import sistema_biblioteca.demo.repository.LibroRepository;
import sistema_biblioteca.demo.repository.PrestamoRepository;
import sistema_biblioteca.demo.repository.UsuarioRepository;
import sistema_biblioteca.demo.service.ReservaService;

import org.springframework.security.crypto.password.PasswordEncoder;
import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/profesor")
public class ProfesorApiController {

    @Autowired
    private LibroRepository libroRepository;

    @Autowired
    private PrestamoRepository prestamoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ReservaService reservaService;

    @GetMapping("/libros")
    public ResponseEntity<List<Libro>> buscarLibros(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long categoriaId) {

        List<Libro> libros;

        // 1. Filtrar por texto si existe
        if (q != null && !q.trim().isEmpty()) {
            libros = libroRepository.findByTituloContainingIgnoreCaseOrAutorNombreCompletoContainingIgnoreCase(q.trim(),
                    q.trim());
        } else {
            libros = libroRepository.findByActivoTrue();
        }

        // 2. Filtrar por categoría en memoria si se proporcionó una
        if (categoriaId != null) {
            libros = libros.stream()
                    .filter(l -> l.getCategoria() != null && l.getCategoria().getId().equals(categoriaId))
                    .collect(Collectors.toList());
        }

        // 3. Filtrar solo los libros disponibles (que no hayan sido desactivados para
        // préstamo)
        libros = libros.stream()
                .filter(Libro::isDisponible)
                .collect(Collectors.toList());

        return ResponseEntity.ok(libros);
    }

    @PostMapping("/reservas/solicitar")
    public ResponseEntity<?> solicitarLibro(
            @RequestParam Long libroId,
            @RequestParam(required = false) String fechaDevolucion,
            Principal principal) {
        if (principal == null)
            return ResponseEntity.status(401).body("No autorizado");

        Usuario usuario = usuarioRepository.findByCodigo(principal.getName()).orElse(null);
        if (usuario == null)
            return ResponseEntity.status(404).body("Usuario no encontrado");

        LocalDate fechaLimite;
        try {
            if (fechaDevolucion != null && !fechaDevolucion.isEmpty()) {
                fechaLimite = LocalDate.parse(fechaDevolucion);
            } else {
                fechaLimite = LocalDate.now().plusDays(5);
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Fecha inválida");
        }

        try {
            reservaService.solicitarReserva(usuario, libroId, fechaLimite);
            return ResponseEntity.ok("Solicitud de reserva enviada exitosamente");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/actividad/prestamos")
    public ResponseEntity<?> obtenerPrestamosActivos(Principal principal) {
        if (principal == null)
            return ResponseEntity.status(401).body("No autorizado");

        Usuario usuario = usuarioRepository.findByCodigo(principal.getName()).orElse(null);
        if (usuario == null)
            return ResponseEntity.status(404).body("Usuario no encontrado");

        List<EstadoPrestamo> estadosPrestamoActivo = java.util.Arrays.asList(EstadoPrestamo.ACTIVO,
                EstadoPrestamo.RETRASADO);
        List<Prestamo> activos = prestamoRepository.findByUsuarioIdAndEstadoInAndEntregadoTrue(usuario.getId(),
                estadosPrestamoActivo);

        return ResponseEntity.ok(activos);
    }

    @GetMapping("/actividad/reservas")
    public ResponseEntity<?> obtenerReservas(Principal principal) {
        if (principal == null)
            return ResponseEntity.status(401).body("No autorizado");

        Usuario usuario = usuarioRepository.findByCodigo(principal.getName()).orElse(null);
        if (usuario == null)
            return ResponseEntity.status(404).body("Usuario no encontrado");

        List<Reserva> reservas = reservaService.obtenerReservasActivas(usuario);
        return ResponseEntity.ok(reservas);
    }

    @PostMapping("/reservas/{id}/cancelar")
    public ResponseEntity<?> cancelarReserva(@PathVariable Long id, Principal principal) {
        if (principal == null)
            return ResponseEntity.status(401).body("No autorizado");

        Usuario usuario = usuarioRepository.findByCodigo(principal.getName()).orElse(null);
        if (usuario == null)
            return ResponseEntity.status(404).body("Usuario no encontrado");

        try {
            reservaService.cancelarReserva(id, usuario);
            return ResponseEntity.ok("Reserva cancelada exitosamente");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/historial/prestamos")
    public ResponseEntity<?> obtenerHistorialPrestamos(Principal principal) {
        if (principal == null)
            return ResponseEntity.status(401).body("No autorizado");

        Usuario usuario = usuarioRepository.findByCodigo(principal.getName()).orElse(null);
        if (usuario == null)
            return ResponseEntity.status(404).body("Usuario no encontrado");

        // Retornamos todos los préstamos que el usuario ya devolvió (entregado == true)
        List<Prestamo> historial = prestamoRepository.findByUsuarioId(usuario.getId())
                .stream()
                .filter(p -> p.isEntregado() && p.getEstado() == EstadoPrestamo.DEVUELTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(historial);
    }

    @PostMapping("/perfil/actualizar")
    public ResponseEntity<?> actualizarPerfil(@RequestBody Map<String, String> payload, Principal principal) {
        if (principal == null)
            return ResponseEntity.status(401).body("No autorizado");

        Usuario usuario = usuarioRepository.findByCodigo(principal.getName()).orElse(null);
        if (usuario == null)
            return ResponseEntity.status(404).body("Usuario no encontrado");

        String passwordActual = payload.get("passwordActual");
        String nuevaPassword = payload.get("nuevaPassword");
        String confirmarPassword = payload.get("confirmarPassword");

        if (passwordActual != null && !passwordActual.trim().isEmpty()) {
            if (!passwordEncoder.matches(passwordActual, usuario.getPassword())) {
                return ResponseEntity.status(400).body(Map.of("message", "La contraseña actual es incorrecta"));
            }
            if (nuevaPassword == null || nuevaPassword.trim().isEmpty()) {
                return ResponseEntity.status(400).body(Map.of("message", "La nueva contraseña no puede estar vacía"));
            }
            if (!nuevaPassword.equals(confirmarPassword)) {
                return ResponseEntity.status(400).body(Map.of("message", "Las nuevas contraseñas no coinciden"));
            }
            usuario.setPassword(passwordEncoder.encode(nuevaPassword.trim()));
        }

        usuarioRepository.save(usuario);

        return ResponseEntity.ok(Map.of("message", "Perfil actualizado correctamente"));
    }
}
