package sistema_biblioteca.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sistema_biblioteca.demo.model.Libro;
import sistema_biblioteca.demo.model.Reserva;
import sistema_biblioteca.demo.model.Usuario;
import sistema_biblioteca.demo.model.enums.EstadoReserva;
import sistema_biblioteca.demo.repository.LibroRepository;
import sistema_biblioteca.demo.repository.ReservaRepository;
import sistema_biblioteca.demo.repository.UsuarioRepository;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/profesor")
public class ProfesorApiController {

    @Autowired
    private LibroRepository libroRepository;

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

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

        // Creamos la solicitud de reserva
        Reserva reserva = new Reserva();
        reserva.setUsuario(usuario);
        reserva.setLibro(libro);
        reserva.setFechaReserva(LocalDate.now());
        // Por defecto damos 5 días para recoger el libro una vez aprobado, aunque al ser PENDIENTE, 
        // la fecha límite aplica más desde que se acepta. Lo guardamos como referencia.
        reserva.setFechaLimiteRecojo(LocalDate.now().plusDays(5));
        reserva.setEstado(EstadoReserva.PENDIENTE);
        reserva.setCantidad(1);
        reserva.setNotificado(false);

        reservaRepository.save(reserva);

        return ResponseEntity.ok("Solicitud de reserva enviada exitosamente");
    }
}
