CREATE DATABASE IF NOT EXISTS biblioteca_cve;
USE biblioteca_cve;

CREATE TABLE autores (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nacionalidad VARCHAR(255),
    nombre_completo VARCHAR(255) NOT NULL
);

CREATE TABLE categorias (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL UNIQUE,
    descripcion VARCHAR(255)
);

CREATE TABLE editoriales (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(255)
);

CREATE TABLE libros (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    titulo VARCHAR(255) NOT NULL,
    isbn VARCHAR(255) NOT NULL UNIQUE,
    anio_publicacion INT,
    stock INT NOT NULL,
    autor_id BIGINT,
    categoria_id BIGINT,
    editorial_id BIGINT,
    FOREIGN KEY (autor_id) REFERENCES autores(id),
    FOREIGN KEY (categoria_id) REFERENCES categorias(id),
    FOREIGN KEY (editorial_id) REFERENCES editoriales(id)
);

CREATE TABLE usuarios (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    codigo VARCHAR(255),
    nombre VARCHAR(255),
    password VARCHAR(255),
    rol ENUM('ADMINISTRADOR', 'BIBLIOTECARIO', 'ESTUDIANTE', 'PROFESOR') NOT NULL
);

CREATE TABLE prestamos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    estado ENUM('ACTIVO', 'DEVUELTO', 'RETRASADO') NOT NULL,
    fecha_prestamo DATE NOT NULL,
    fecha_devolucion_esperada DATE NOT NULL,
    fecha_devolucion_real DATE,
    observaciones VARCHAR(500),
    libro_id BIGINT NOT NULL,
    usuario_id BIGINT NOT NULL,
    FOREIGN KEY (libro_id) REFERENCES libros(id),
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
);

CREATE TABLE reservas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    estado ENUM('CANCELADA', 'PENDIENTE', 'RECOGIDA', 'VENCIDA') NOT NULL,
    fecha_reserva DATE NOT NULL,
    fecha_limite_recojo DATE NOT NULL,
    libro_id BIGINT NOT NULL,
    usuario_id BIGINT NOT NULL,
    FOREIGN KEY (libro_id) REFERENCES libros(id),
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
);