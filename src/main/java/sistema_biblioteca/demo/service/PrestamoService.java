package sistema_biblioteca.demo.service;

import sistema_biblioteca.demo.model.Prestamo;
import java.util.List;

public interface PrestamoService {

    List<Prestamo> listarPrestamos();

    Prestamo guardarPrestamo(Prestamo prestamo);

    Prestamo obtenerPorId(Long id);

    void eliminarPrestamo(Long id);
}