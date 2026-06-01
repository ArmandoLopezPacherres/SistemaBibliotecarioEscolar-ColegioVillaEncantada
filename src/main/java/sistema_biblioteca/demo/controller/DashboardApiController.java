package sistema_biblioteca.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import sistema_biblioteca.demo.dto.DashboardChartsDTO;
import sistema_biblioteca.demo.model.Prestamo;
import sistema_biblioteca.demo.model.enums.EstadoPrestamo;
import sistema_biblioteca.demo.model.enums.RolUsuario;
import sistema_biblioteca.demo.service.PrestamoService;
import sistema_biblioteca.demo.service.UsuarioService;

import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/charts")
public class DashboardApiController {

    @Autowired
    private PrestamoService prestamoService;

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping
    public DashboardChartsDTO getDashboardCharts(@RequestParam(defaultValue = "12") int meses) {
        List<Prestamo> todosPrestamos = prestamoService.listarPrestamos();

        LocalDate hoy = LocalDate.now();
        LocalDate inicio = hoy.minusMonths(meses - 1).withDayOfMonth(1);

        List<Prestamo> prestamosEnRango = todosPrestamos.stream()
            .filter(p -> p.getFechaPrestamo() != null && !p.getFechaPrestamo().isBefore(inicio))
            .collect(Collectors.toList());

        DashboardChartsDTO dto = new DashboardChartsDTO();

        List<String> mesesLabels = new ArrayList<>();
        List<Long> prestamosPorMes = new ArrayList<>();

        for (int i = meses - 1; i >= 0; i--) {
            LocalDate mes = hoy.minusMonths(i);
            String label = mes.getMonth()
                .getDisplayName(TextStyle.SHORT, Locale.forLanguageTag("es-ES"));
            label = label.substring(0, 1).toUpperCase() + label.substring(1);
            if (meses != 12) {
                label = label + " " + mes.getYear();
            }
            mesesLabels.add(label);

            final int yr = mes.getYear();
            final Month month = mes.getMonth();
            long count = prestamosEnRango.stream()
                .filter(p -> p.getFechaPrestamo().getYear() == yr
                          && p.getFechaPrestamo().getMonth() == month)
                .count();
            prestamosPorMes.add(count);
        }

        dto.setMesesLabels(mesesLabels);
        dto.setPrestamosPorMes(prestamosPorMes);

        Map<String, Long> librosCounts = prestamosEnRango.stream()
            .filter(p -> p.getLibro() != null)
            .collect(Collectors.groupingBy(
                p -> p.getLibro().getTitulo(),
                Collectors.counting()
            ));

        List<Map.Entry<String, Long>> topLibros = librosCounts.entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .limit(5)
            .collect(Collectors.toList());

        dto.setLibrosLabels(topLibros.stream().map(Map.Entry::getKey).collect(Collectors.toList()));
        dto.setLibrosCantidad(topLibros.stream().map(Map.Entry::getValue).collect(Collectors.toList()));

        List<String> rolesLabels = Arrays.asList("Administrador", "Bibliotecario", "Profesor", "Estudiante");
        List<Long> rolesCantidad = Arrays.asList(
            usuarioService.buscarPorRol(RolUsuario.ADMINISTRADOR).stream().filter(u -> u.isActivo()).count(),
            usuarioService.buscarPorRol(RolUsuario.BIBLIOTECARIO).stream().filter(u -> u.isActivo()).count(),
            usuarioService.buscarPorRol(RolUsuario.PROFESOR).stream().filter(u -> u.isActivo()).count(),
            usuarioService.buscarPorRol(RolUsuario.ESTUDIANTE).stream().filter(u -> u.isActivo()).count()
        );
        dto.setRolesLabels(rolesLabels);
        dto.setRolesCantidad(rolesCantidad);

        dto.setPrestamosActivos(
            todosPrestamos.stream().filter(p -> p.getEstado() == EstadoPrestamo.ACTIVO).count());
        dto.setPrestamosDevueltos(
            todosPrestamos.stream().filter(p -> p.getEstado() == EstadoPrestamo.DEVUELTO).count());
        dto.setPrestamosRetrasados(
            todosPrestamos.stream().filter(p -> p.getEstado() == EstadoPrestamo.RETRASADO).count());

        return dto;
    }
}
