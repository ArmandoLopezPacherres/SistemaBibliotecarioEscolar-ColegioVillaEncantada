package sistema_biblioteca.demo.service;

import sistema_biblioteca.demo.model.Prestamo;
import java.util.List;

public interface PrestamoService {

    List<Prestamo> listarPrestamos();

    List<Prestamo> listarPorUsuario(Long usuarioId);

    Prestamo guardarPrestamo(Prestamo prestamo);

    Prestamo obtenerPorId(Long id);

    void eliminarPrestamo(Long id);

    void procesarEscaner(String codigoUsuario, List<Long> idsDevolver, List<Long> idsPrestar, String fechaDevolucionStr, int limiteEstudiante, int limiteProfesor);
}