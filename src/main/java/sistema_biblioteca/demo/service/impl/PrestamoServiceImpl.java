package sistema_biblioteca.demo.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sistema_biblioteca.demo.model.Prestamo;
import sistema_biblioteca.demo.repository.PrestamoRepository;
import sistema_biblioteca.demo.service.PrestamoService;

import java.util.List;

@Service
@org.springframework.transaction.annotation.Transactional
public class PrestamoServiceImpl implements PrestamoService {

    @Autowired
    private PrestamoRepository prestamoRepository;

    @Override
    public List<Prestamo> listarPrestamos() {
        return prestamoRepository.findAll();
    }

    @Override
    public List<Prestamo> listarPorUsuario(Long usuarioId) {
        return prestamoRepository.findByUsuarioId(usuarioId);
    }

    @Override
    public Prestamo guardarPrestamo(Prestamo prestamo) {
        return prestamoRepository.save(prestamo);
    }

    @Override
    public Prestamo obtenerPorId(Long id) {
        return prestamoRepository.findById(id).orElse(null);
    }

    @Override
    public void eliminarPrestamo(Long id) {
        prestamoRepository.deleteById(id);
    }

    @Autowired
    private sistema_biblioteca.demo.repository.UsuarioRepository usuarioRepository;

    @Autowired
    private sistema_biblioteca.demo.repository.LibroRepository libroRepository;

    @Override
    public void procesarEscaner(String codigoUsuario, List<Long> idsDevolver, List<Long> idsPrestar, String fechaDevolucionStr, int limiteEstudiante, int limiteProfesor) {
        java.util.Optional<sistema_biblioteca.demo.model.Usuario> optUsuario = usuarioRepository.findByCodigo(codigoUsuario);
        if (optUsuario.isEmpty()) {
            throw new IllegalArgumentException("Usuario no encontrado.");
        }
        sistema_biblioteca.demo.model.Usuario usuario = optUsuario.get();

        int puntosGanados = 0;
        long cantidadADevolver = 0;

        if (idsDevolver != null && !idsDevolver.isEmpty()) {
            for (Long idPrestamo : idsDevolver) {
                java.util.Optional<Prestamo> optPrestamo = prestamoRepository.findById(idPrestamo);
                if (optPrestamo.isPresent()) {
                    Prestamo prestamo = optPrestamo.get();
                    if (prestamo.getEstado() != sistema_biblioteca.demo.model.enums.EstadoPrestamo.DEVUELTO) {
                        cantidadADevolver += prestamo.getCantidad() > 0 ? prestamo.getCantidad() : 1;
                        prestamo.setEstado(sistema_biblioteca.demo.model.enums.EstadoPrestamo.DEVUELTO);
                        prestamo.setFechaDevolucionReal(java.time.LocalDate.now());
                        prestamoRepository.save(prestamo);
                        
                        sistema_biblioteca.demo.model.Libro libro = prestamo.getLibro();
                        if (libro != null) {
                            libro.setStock(libro.getStock() + (prestamo.getCantidad() > 0 ? prestamo.getCantidad() : 1));
                            libro.setDisponible(true);
                            libroRepository.save(libro);
                        }

                        if (prestamo.getFechaPrestamo() != null && prestamo.getFechaDevolucionReal().isAfter(prestamo.getFechaPrestamo())) {
                            puntosGanados += 10;
                        }
                    }
                }
            }
        }

        java.util.Map<Long, Long> cantidades = idsPrestar == null ? java.util.Collections.emptyMap() : 
            idsPrestar.stream().collect(java.util.stream.Collectors.groupingBy(id -> id, java.util.stream.Collectors.counting()));

        int limite = usuario.getRol() == sistema_biblioteca.demo.model.enums.RolUsuario.ESTUDIANTE ? limiteEstudiante : limiteProfesor;
        
        long totalPrestamosActivos = prestamoRepository.findByUsuarioId(usuario.getId()).stream()
                .filter(p -> p.getEstado() == sistema_biblioteca.demo.model.enums.EstadoPrestamo.ACTIVO || p.getEstado() == sistema_biblioteca.demo.model.enums.EstadoPrestamo.RETRASADO)
                .mapToLong(p -> p.getCantidad() > 0 ? p.getCantidad() : 1)
                .sum();
                
        long totalNuevos = cantidades.values().stream().mapToLong(Long::longValue).sum();

        if ((totalPrestamosActivos - cantidadADevolver + totalNuevos) > limite) {
            throw new IllegalArgumentException("Límite de préstamos excedido. El límite máximo es " + limite + " libros.");
        }

        if (usuario.getRol() == sistema_biblioteca.demo.model.enums.RolUsuario.ESTUDIANTE) {
            for (Long count : cantidades.values()) {
                if (count > 1) {
                    throw new IllegalArgumentException("Los estudiantes solo pueden llevar 1 copia de cada libro.");
                }
            }
        }

        if (!cantidades.isEmpty()) {
            java.time.LocalDate fechaDevolucion = java.time.LocalDate.now().plusDays(7);
            if (fechaDevolucionStr != null && !fechaDevolucionStr.trim().isEmpty()) {
                try {
                    fechaDevolucion = java.time.LocalDate.parse(fechaDevolucionStr);
                } catch (java.time.format.DateTimeParseException e) {
                    System.err.println("[ERROR Seguridad - Alerta 8] Intento de inyección o fallo de formato en fecha: " + e.getMessage());
                    throw new IllegalArgumentException("Formato de fecha inválido. Use el formato YYYY-MM-DD.");
                }
            }

            for (java.util.Map.Entry<Long, Long> entry : cantidades.entrySet()) {
                Long idLibro = entry.getKey();
                int cantidadPedida = entry.getValue().intValue();

                java.util.Optional<sistema_biblioteca.demo.model.Libro> optLibro = libroRepository.findById(idLibro);
                if (optLibro.isPresent()) {
                    sistema_biblioteca.demo.model.Libro libro = optLibro.get();
                    int filasAfectadas = libroRepository.descontarStock(idLibro, cantidadPedida);
                    if (filasAfectadas > 0) {
                        libro = libroRepository.findById(idLibro).get();
                        if (libro.getStock() == 0) {
                            libro.setDisponible(false);
                            libroRepository.save(libro);
                        }

                        Prestamo prestamo = new Prestamo();
                        prestamo.setUsuario(usuario);
                        prestamo.setLibro(libro);
                        prestamo.setFechaPrestamo(java.time.LocalDate.now());
                        prestamo.setFechaDevolucionEsperada(fechaDevolucion);
                        prestamo.setEstado(sistema_biblioteca.demo.model.enums.EstadoPrestamo.ACTIVO);
                        prestamo.setEntregado(true);
                        prestamo.setCantidad(cantidadPedida);
                        prestamoRepository.save(prestamo);
                    } else {
                        throw new IllegalArgumentException("Stock insuficiente para el libro: " + libro.getTitulo());
                    }
                }
            }
        }

        if (puntosGanados > 0) {
            Integer puntosAnteriores = usuario.getPuntosLectura() != null ? usuario.getPuntosLectura() : 0;
            usuario.setPuntosLectura(puntosAnteriores + puntosGanados);
            usuarioRepository.save(usuario);
        }
    }
}