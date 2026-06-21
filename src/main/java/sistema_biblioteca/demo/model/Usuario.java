package sistema_biblioteca.demo.model;

import jakarta.persistence.*;
import sistema_biblioteca.demo.model.enums.RolUsuario;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String codigo;
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RolUsuario rol;

    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private boolean activo = true;

    public Usuario() {
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public RolUsuario getRol() {
        return rol;
    }

    public void setRol(RolUsuario rol) {
        this.rol = rol;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    @Transient
    private String estadoLector;

    @Column(nullable = false, columnDefinition = "INT DEFAULT 0")
    private Integer puntosLectura = 0;

    public String getEstadoLector() {
        return estadoLector;
    }

    public void setEstadoLector(String estadoLector) {
        this.estadoLector = estadoLector;
    }

    public Integer getPuntosLectura() {
        return puntosLectura;
    }

    public void setPuntosLectura(Integer puntosLectura) {
        this.puntosLectura = puntosLectura;
    }
}