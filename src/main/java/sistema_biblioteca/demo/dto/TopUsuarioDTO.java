package sistema_biblioteca.demo.dto;

public class TopUsuarioDTO {
    private int posicion;
    private String nombre;
    private String rol;
    private long totalPrestamos;
    private long totalDevoluciones;
    private String tasaDevolucion;

    public TopUsuarioDTO() {}

    public int getPosicion() { return posicion; }
    public void setPosicion(int posicion) { this.posicion = posicion; }
    
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    
    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }
    
    public long getTotalPrestamos() { return totalPrestamos; }
    public void setTotalPrestamos(long totalPrestamos) { this.totalPrestamos = totalPrestamos; }
    
    public long getTotalDevoluciones() { return totalDevoluciones; }
    public void setTotalDevoluciones(long totalDevoluciones) { this.totalDevoluciones = totalDevoluciones; }
    
    public String getTasaDevolucion() { return tasaDevolucion; }
    public void setTasaDevolucion(String tasaDevolucion) { this.tasaDevolucion = tasaDevolucion; }
}
