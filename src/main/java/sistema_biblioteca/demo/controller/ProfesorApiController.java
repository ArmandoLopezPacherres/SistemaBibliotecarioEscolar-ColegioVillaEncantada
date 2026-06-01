package sistema_biblioteca.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sistema_biblioteca.demo.model.Libro;
import sistema_biblioteca.demo.model.Prestamo;
import sistema_biblioteca.demo.model.Reserva;
import sistema_biblioteca.demo.model.Usuario;
import sistema_biblioteca.demo.model.enums.EstadoReserva;
import sistema_biblioteca.demo.model.enums.EstadoPrestamo;
import sistema_biblioteca.demo.repository.LibroRepository;
import sistema_biblioteca.demo.repository.PrestamoRepository;
import sistema_biblioteca.demo.repository.ReservaRepository;
import sistema_biblioteca.demo.repository.UsuarioRepository;

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
    private ReservaRepository reservaRepository;

    @Autowired
    private PrestamoRepository prestamoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/libros")
    public ResponseEntity<List<Libro>> buscarLibros(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long categoriaId) {

        List<Libro> libros;
        
        // 1. Filtrar por texto si existe
        if (q != null && !q.trim().isEmpty()) {
            libros = libroRepository.findByTituloContainingIgnoreCaseOrAutorNombreCompletoContainingIgnoreCase(q.trim(), q.trim());
        } else {
            libros = libroRepository.findByActivoTrue();
        }

        // 2. Filtrar por categoría en memoria si se proporcionó una
        if (categoriaId != null) {
            libros = libros.stream()
                    .filter(l -> l.getCategoria() != null && l.getCategoria().getId().equals(categoriaId))
                    .collect(Collectors.toList());
        }

        // 3. Filtrar solo los libros disponibles (que no hayan sido desactivados para préstamo)
        libros = libros.stream()
                    .filter(Libro::isDisponible)
                    .collect(Collectors.toList());

        return ResponseEntity.ok(libros);
    }

    @PostMapping("/reservas/solicitar")
    public ResponseEntity<?> solicitarLibro(@RequestParam Long libroId, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body("No autorizado");
        }

        Usuario usuario = usuarioRepository.findByCodigo(principal.getName()).orElse(null);
        if (usuario == null) {
            return ResponseEntity.status(404).body("Usuario no encontrado");
        }

        Libro libro = libroRepository.findById(libroId).orElse(null);
        if (libro == null || !libro.isDisponible() || libro.getStock() <= 0) {
            return ResponseEntity.badRequest().body("El libro no está disponible o no hay stock suficiente.");
        }

        // Verificar límite de 20 libros activos (solo los que tiene físicamente en su poder)
        long librosActivos = prestamoRepository.findByUsuarioId(usuario.getId())
                .stream()
                .filter(p -> p.isEntregado() && p.getEstado() != EstadoPrestamo.DEVUELTO)
                .mapToLong(p -> p.getCantidad())
                .sum();
        if (librosActivos >= 20) {
            return ResponseEntity.badRequest().body("Ya tienes 20 libros en tu poder. No puedes solicitar más hasta devolver alguno.");
        }

        // Verificar que no tenga ya una solicitud PENDIENTE del mismo libro
        boolean yaSolicito = reservaRepository.findByUsuarioId(usuario.getId())
                .stream()
                .anyMatch(r -> r.getLibro().getId().equals(libroId) && r.getEstado() == EstadoReserva.PENDIENTE);
        if (yaSolicito) {
            return ResponseEntity.badRequest().body("Ya tienes una solicitud pendiente para este libro.");
        }

        // Creamos la solicitud de reserva
        Reserva reserva = new Reserva();
        reserva.setUsuario(usuario);
        reserva.setLibro(libro);
        reserva.setFechaReserva(LocalDate.now());
        reserva.setFechaLimiteRecojo(LocalDate.now().plusDays(5));
        reserva.setEstado(EstadoReserva.PENDIENTE);
        reserva.setCantidad(1);
        reserva.setNotificado(false);

        reservaRepository.save(reserva);

        return ResponseEntity.ok("Solicitud de reserva enviada exitosamente");
    }

    @GetMapping("/actividad/prestamos")
    public ResponseEntity<?> obtenerPrestamosActivos(Principal principal) {
        if (principal == null) return ResponseEntity.status(401).body("No autorizado");
        
        Usuario usuario = usuarioRepository.findByCodigo(principal.getName()).orElse(null);
        if (usuario == null) return ResponseEntity.status(404).body("Usuario no encontrado");

        // Solo los que el profesor tiene físicamente (entregado=true) y aún no devueltos
        List<Prestamo> activos = prestamoRepository.findByUsuarioId(usuario.getId())
                .stream()
                .filter(p -> p.isEntregado() && p.getEstado() != EstadoPrestamo.DEVUELTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(activos);
    }

    @GetMapping("/actividad/reservas")
    public ResponseEntity<?> obtenerReservas(Principal principal) {
        if (principal == null) return ResponseEntity.status(401).body("No autorizado");
        
        Usuario usuario = usuarioRepository.findByCodigo(principal.getName()).orElse(null);
        if (usuario == null) return ResponseEntity.status(404).body("Usuario no encontrado");

        // IDs de préstamos que ya fueron entregados físicamente al profesor
        List<Long> librosYaRecogidosIds = prestamoRepository.findByUsuarioId(usuario.getId())
                .stream()
                .filter(Prestamo::isEntregado)
                .map(p -> p.getLibro().getId())
                .collect(Collectors.toList());

        // Solo mostrar reservas que NO tienen un préstamo entregado (pendientes de aprobación o de recojo)
        List<Reserva> reservas = reservaRepository.findByUsuarioId(usuario.getId())
                .stream()
                .filter(r -> {
                    // Siempre mostrar canceladas y vencidas (informativo)
                    if (r.getEstado() == EstadoReserva.CANCELADA || r.getEstado() == EstadoReserva.VENCIDA) {
                        return true;
                    }
                    // Para PENDIENTE y RECOGIDA: solo si el libro aún no fue recogido físicamente
                    return !librosYaRecogidosIds.contains(r.getLibro().getId());
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(reservas);
    }

    @PostMapping("/reservas/{id}/cancelar")
    public ResponseEntity<?> cancelarReserva(@PathVariable Long id, Principal principal) {
        if (principal == null) return ResponseEntity.status(401).body("No autorizado");
        
        Usuario usuario = usuarioRepository.findByCodigo(principal.getName()).orElse(null);
        if (usuario == null) return ResponseEntity.status(404).body("Usuario no encontrado");

        Reserva reserva = reservaRepository.findById(id).orElse(null);
        if (reserva == null) return ResponseEntity.status(404).body("Reserva no encontrada");

        if (!reserva.getUsuario().getId().equals(usuario.getId())) {
            return ResponseEntity.status(403).body("No tienes permiso para cancelar esta reserva");
        }

        if (reserva.getEstado() != EstadoReserva.PENDIENTE) {
            return ResponseEntity.badRequest().body("Solo se pueden cancelar reservas en estado PENDIENTE");
        }

        reservaRepository.delete(reserva); // Eliminamos la reserva o la pasamos a CANCELADO, aquí la eliminamos para no dejar basura o según la lógica del negocio.
        return ResponseEntity.ok("Reserva cancelada exitosamente");
    }

    @GetMapping("/historial/prestamos")
    public ResponseEntity<?> obtenerHistorialPrestamos(Principal principal) {
        if (principal == null) return ResponseEntity.status(401).body("No autorizado");
        
        Usuario usuario = usuarioRepository.findByCodigo(principal.getName()).orElse(null);
        if (usuario == null) return ResponseEntity.status(404).body("Usuario no encontrado");

        // Retornamos todos los préstamos que el usuario ya devolvió (entregado == true)
        List<Prestamo> historial = prestamoRepository.findByUsuarioId(usuario.getId())
                .stream()
                .filter(p -> p.isEntregado() && p.getEstado() == EstadoPrestamo.DEVUELTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(historial);
    }

    @PostMapping("/perfil/actualizar")
    public ResponseEntity<?> actualizarPerfil(@RequestBody Map<String, String> payload, Principal principal) {
        if (principal == null) return ResponseEntity.status(401).body("No autorizado");
        
        Usuario usuario = usuarioRepository.findByCodigo(principal.getName()).orElse(null);
        if (usuario == null) return ResponseEntity.status(404).body("Usuario no encontrado");

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
