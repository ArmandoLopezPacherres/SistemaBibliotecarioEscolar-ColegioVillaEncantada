package sistema_biblioteca.demo.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sistema_biblioteca.demo.model.Prestamo;
import sistema_biblioteca.demo.repository.PrestamoRepository;
import sistema_biblioteca.demo.service.PrestamoService;

import java.util.List;

@Service
public class PrestamoServiceImpl implements PrestamoService {

    @Autowired
    private PrestamoRepository prestamoRepository;

    @Override
    public List<Prestamo> listarPrestamos() {
        return prestamoRepository.findAll();
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
}