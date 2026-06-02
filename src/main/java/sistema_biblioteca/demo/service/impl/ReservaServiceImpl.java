package sistema_biblioteca.demo.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sistema_biblioteca.demo.model.Libro;
import sistema_biblioteca.demo.model.Reserva;
import sistema_biblioteca.demo.model.Usuario;
import sistema_biblioteca.demo.model.enums.EstadoPrestamo;
import sistema_biblioteca.demo.model.enums.EstadoReserva;
import sistema_biblioteca.demo.model.enums.RolUsuario;
import sistema_biblioteca.demo.repository.LibroRepository;
import sistema_biblioteca.demo.repository.PrestamoRepository;
import sistema_biblioteca.demo.repository.ReservaRepository;
import sistema_biblioteca.demo.service.ReservaService;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReservaServiceImpl implements ReservaService {

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private PrestamoRepository prestamoRepository;

    @Autowired
    private LibroRepository libroRepository;

    @Value("${biblioteca.limite.estudiante:3}")
    private int limiteEstudiante;

    @Value("${biblioteca.limite.profesor:20}")
    private int limiteProfesor;

    @Override
    @Transactional
    public void solicitarReserva(Usuario usuario, Long libroId, LocalDate fechaLimite) {
        Libro libro = libroRepository.findById(libroId)
                .orElseThrow(() -> new IllegalArgumentException("El libro no existe"));

        if (!libro.isDisponible() || libro.getStock() <= 0) {
            throw new IllegalArgumentException("El libro no está disponible o no hay stock suficiente.");
        }

        if (fechaLimite.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha de devolución no puede ser en el pasado.");
        }

        boolean yaSolicito = reservaRepository.existsByUsuarioIdAndLibroIdAndEstado(
                usuario.getId(), libroId, EstadoReserva.PENDIENTE);
        
        if (yaSolicito) {
            throw new IllegalArgumentException("Ya tienes una solicitud pendiente para este libro.");
        }

        List<EstadoReserva> estadosReservaActiva = Arrays.asList(EstadoReserva.PENDIENTE, EstadoReserva.RECOGIDA);
        long reservasActivas = reservaRepository.countByUsuarioIdAndEstadoIn(usuario.getId(), estadosReservaActiva);

        List<EstadoPrestamo> estadosPrestamoActivo = Arrays.asList(EstadoPrestamo.ACTIVO, EstadoPrestamo.RETRASADO);
        long prestamosActivos = prestamoRepository.countByUsuarioIdAndEstadoInAndEntregadoTrue(usuario.getId(), estadosPrestamoActivo);

        long totalLibrosActivos = reservasActivas + prestamosActivos;

        int limite = usuario.getRol() == RolUsuario.ESTUDIANTE ? limiteEstudiante : limiteProfesor;

        if (totalLibrosActivos >= limite) {
            throw new IllegalArgumentException("Ya tienes el límite máximo de " + limite + " libros (entre solicitudes activas y libros en tu poder).");
        }

        Reserva reserva = new Reserva();
        reserva.setUsuario(usuario);
        reserva.setLibro(libro);
        reserva.setFechaReserva(LocalDate.now());
        reserva.setFechaLimiteRecojo(fechaLimite);
        reserva.setEstado(EstadoReserva.PENDIENTE);
        reserva.setCantidad(1);
        reserva.setNotificado(false);

        reservaRepository.save(reserva);
    }

    @Override
    @Transactional
    public void cancelarReserva(Long reservaId, Usuario usuario) {
        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new IllegalArgumentException("La solicitud no existe"));

        if (!reserva.getUsuario().getId().equals(usuario.getId())) {
            throw new IllegalArgumentException("No puedes cancelar una solicitud ajena.");
        }

        if (reserva.getEstado() != EstadoReserva.PENDIENTE) {
            throw new IllegalArgumentException("Solo se pueden cancelar solicitudes pendientes.");
        }

        reservaRepository.delete(reserva);
    }

    @Override
    public List<Reserva> obtenerReservasActivas(Usuario usuario) {
        List<EstadoReserva> estados = Arrays.asList(EstadoReserva.PENDIENTE, EstadoReserva.RECOGIDA);
        return reservaRepository.findByUsuarioIdAndEstadoIn(usuario.getId(), estados)
                .stream()
                .sorted(Comparator.comparing((Reserva r) -> r.getFechaReserva() != null ? r.getFechaReserva() : LocalDate.MIN).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public List<Reserva> obtenerNotificaciones(Usuario usuario) {
        return reservaRepository.findByUsuarioId(usuario.getId()).stream()
                .filter(r -> r.getEstado() == EstadoReserva.RECOGIDA || 
                             r.getEstado() == EstadoReserva.COMPLETADA || 
                             r.getEstado() == EstadoReserva.CANCELADA)
                .sorted((r1, r2) -> {
                    LocalDate d1 = r1.getFechaReserva() != null ? r1.getFechaReserva() : LocalDate.MIN;
                    LocalDate d2 = r2.getFechaReserva() != null ? r2.getFechaReserva() : LocalDate.MIN;
                    if (d1.equals(d2)) {
                        return r2.getId().compareTo(r1.getId());
                    }
                    return d2.compareTo(d1);
                })
                .collect(Collectors.toList());
    }
}
