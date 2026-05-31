package sistema_biblioteca.demo.dto;

import java.util.List;

public class DashboardChartsDTO {

    // Gráfico 1: Préstamos por mes
    private List<String> mesesLabels;
    private List<Long> prestamosPorMes;

    // Gráfico 2: Libros más prestados
    private List<String> librosLabels;
    private List<Long> librosCantidad;

    // Gráfico 3: Usuarios por rol
    private List<String> rolesLabels;
    private List<Long> rolesCantidad;

    // Gráfico 4: Resumen de morosidad
    private long prestamosActivos;
    private long prestamosDevueltos;
    private long prestamosRetrasados;

    public DashboardChartsDTO() {}

    public List<String> getMesesLabels() { return mesesLabels; }
    public void setMesesLabels(List<String> mesesLabels) { this.mesesLabels = mesesLabels; }

    public List<Long> getPrestamosPorMes() { return prestamosPorMes; }
    public void setPrestamosPorMes(List<Long> prestamosPorMes) { this.prestamosPorMes = prestamosPorMes; }

    public List<String> getLibrosLabels() { return librosLabels; }
    public void setLibrosLabels(List<String> librosLabels) { this.librosLabels = librosLabels; }

    public List<Long> getLibrosCantidad() { return librosCantidad; }
    public void setLibrosCantidad(List<Long> librosCantidad) { this.librosCantidad = librosCantidad; }

    public List<String> getRolesLabels() { return rolesLabels; }
    public void setRolesLabels(List<String> rolesLabels) { this.rolesLabels = rolesLabels; }

    public List<Long> getRolesCantidad() { return rolesCantidad; }
    public void setRolesCantidad(List<Long> rolesCantidad) { this.rolesCantidad = rolesCantidad; }

    public long getPrestamosActivos() { return prestamosActivos; }
    public void setPrestamosActivos(long prestamosActivos) { this.prestamosActivos = prestamosActivos; }

    public long getPrestamosDevueltos() { return prestamosDevueltos; }
    public void setPrestamosDevueltos(long prestamosDevueltos) { this.prestamosDevueltos = prestamosDevueltos; }

    public long getPrestamosRetrasados() { return prestamosRetrasados; }
    public void setPrestamosRetrasados(long prestamosRetrasados) { this.prestamosRetrasados = prestamosRetrasados; }
}
