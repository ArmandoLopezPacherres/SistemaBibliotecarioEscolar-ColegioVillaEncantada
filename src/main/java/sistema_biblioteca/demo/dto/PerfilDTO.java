package sistema_biblioteca.demo.dto;

public class PerfilDTO {
    private String nombre;
    private String rol;
    private String fechaCreacion;
    private String correo;
    private String codigo;

    public PerfilDTO() {}

    public PerfilDTO(String nombre, String rol, String fechaCreacion, String correo, String codigo) {
        this.nombre = nombre;
        this.rol = rol;
        this.fechaCreacion = fechaCreacion;
        this.correo = correo;
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public String getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(String fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }
}
