package sistema_biblioteca.demo.service;

import sistema_biblioteca.demo.model.Reserva;
import sistema_biblioteca.demo.model.Usuario;
import java.time.LocalDate;
import java.util.List;

public interface ReservaService {
    void solicitarReserva(Usuario usuario, Long libroId, LocalDate fechaLimite);
    void cancelarReserva(Long reservaId, Usuario usuario);
    List<Reserva> obtenerReservasActivas(Usuario usuario);
    List<Reserva> obtenerNotificaciones(Usuario usuario);
}
