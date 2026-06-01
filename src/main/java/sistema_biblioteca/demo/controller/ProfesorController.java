package sistema_biblioteca.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import sistema_biblioteca.demo.repository.UsuarioRepository;
import java.security.Principal;

import sistema_biblioteca.demo.repository.CategoriaRepository;
import sistema_biblioteca.demo.repository.ReservaRepository;
import sistema_biblioteca.demo.model.Reserva;
import sistema_biblioteca.demo.model.enums.EstadoReserva;
import java.util.List;

@Controller
@RequestMapping("/panel-profesor")
public class ProfesorController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private ReservaRepository reservaRepository;

    private void cargarUsuarioEnModelo(Model model, Principal principal) {
        if (principal != null) {
            usuarioRepository.findByCodigo(principal.getName()).ifPresent(usuario -> {
                model.addAttribute("usuario", usuario);
            });
        }
    }

    @GetMapping
    public String panelProfesor() {
        return "redirect:/panel-profesor/mi-actividad";
    }

    @GetMapping("/mi-actividad")
    public String miActividad(Model model, Principal principal) {
        cargarUsuarioEnModelo(model, principal);
        return "Profesor/miActividad";
    }

    @GetMapping("/buscar-libro")
    public String buscarLibro(Model model, Principal principal) {
        cargarUsuarioEnModelo(model, principal);
        model.addAttribute("categorias", categoriaRepository.findByActivoTrue());
        return "Profesor/buscarLibro";
    }

    @GetMapping("/historial")
    public String historial(Model model, Principal principal) {
        cargarUsuarioEnModelo(model, principal);
        return "Profesor/historial";
    }

    @GetMapping("/notificaciones")
    public String notificaciones(Model model, Principal principal) {
        cargarUsuarioEnModelo(model, principal);
        if (principal != null) {
            usuarioRepository.findByCodigo(principal.getName()).ifPresent(usuario -> {
                List<Reserva> notificacionesReservas = reservaRepository.findByUsuarioId(usuario.getId())
                        .stream()
                        .filter(r -> r.getEstado() == EstadoReserva.RECOGIDA || r.getEstado() == EstadoReserva.COMPLETADA || r.getEstado() == EstadoReserva.CANCELADA)
                        .sorted((r1, r2) -> {
                            java.time.LocalDate d1 = r1.getFechaReserva() != null ? r1.getFechaReserva() : java.time.LocalDate.MIN;
                            java.time.LocalDate d2 = r2.getFechaReserva() != null ? r2.getFechaReserva() : java.time.LocalDate.MIN;
                            if (d1.equals(d2)) {
                                return r2.getId().compareTo(r1.getId());
                            }
                            return d2.compareTo(d1);
                        })
                        .toList();
                model.addAttribute("notificacionesReservas", notificacionesReservas);
            });
        }
        return "Profesor/notificaciones";
    }

    @GetMapping("/perfil")
    public String perfil(Model model, Principal principal) {
        cargarUsuarioEnModelo(model, principal);
        return "Profesor/perfil";
    }
}
