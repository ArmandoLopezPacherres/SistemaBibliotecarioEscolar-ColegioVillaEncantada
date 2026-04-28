package sistema_biblioteca.demo.service;

import sistema_biblioteca.demo.model.Libro;
import java.util.List;

public interface LibroService {

    List<Libro> listarLibros();

    Libro guardarLibro(Libro libro);

    Libro obtenerPorId(Long id);

    void eliminarLibro(Long id);
}