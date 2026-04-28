# Sistema Bibliotecario Escolar - Colegio Villa Encantada

Sistema de gestión bibliotecaria completo enfocado al entorno escolar del Colegio Villa Encantada, diseñado para administrar préstamos, reservas y catálogo de libros de manera eficiente.

## Tecnologías

- **Java 21** - Lenguaje de programación principal
- **Spring Boot 3.x** - Framework principal para el backend
- **Spring Security** - Gestión de autenticación y autorización
- **Spring Data JPA** - Persistencia de datos
- **Thymeleaf** - Motor de plantillas para el frontend
- **MySQL** - Base de datos relacional
- **Maven** - Gestión de dependencias y construcción
- **HTML/CSS/JavaScript** - Tecnologías web frontend

## Ramas

- `main` - Rama principal con la versión estable
- `develop` - Rama de desarrollo con las últimas características
- `feature/ui-publica` - Rama de desarrollo de la UI pública
- `feature/ui-privada` - Rama de desarrollo de la UI privada
- `feature/modelos-dominio` - Rama de desarrollo de modelos de la BD
- `feature/config-bd` - Rama de desarrollo para configuración de BD
- `feature/seguridad-rutas` - Rama de desarrollo de autenticación y rutas
- `feature/documentacion` - Rama de desarrollo de documentación

## Ejecución

1. **Clonar el repositorio**
   ```bash
   git clone https://github.com/HectorRamonDeV/SistemaBibliotecarioEscolar-ColegioVillaEncantada.git
   cd SistemaBibliotecarioEscolar-ColegioVillaEncantada
   ```

2. **Configurar la base de datos**
   
   **Opción A: Configuración manual**
   - Crear la base de datos MySQL ejecutando el script en `database/script.sql`
   - Configurar las credenciales de la base de datos en `src/main/resources/application.properties`
   
   **Opción B: Configuración automática**
   - Simplemente ejecutar el proyecto (la base de datos y sus tablas se crearán automáticamente gracias a Hibernate).

3. **Ejecutar la aplicación**
   ```bash
   ./mvnw spring-boot:run
   ```
   O ejecutar la clase principal `DemoApplication.java` desde tu IDE.

4. **Acceder a la aplicación**
   - URL: http://localhost:8080
   - Credenciales por defecto:

     **Administrador**
     - Código: A12345678
     - Nombre: Administrador 
     - Rol: ADMINISTRADOR
     - Contraseña: 1234 

     **Bibliotecario**
     - Código: B87654321
     - Nombre: Bibliotecario 
     - Rol: BIBLIOTECARIO
     - Contraseña: 1234 

     **Profesor**
     - Código: P11223344
     - Nombre: Profesor
     - Rol: PROFESOR
     - Contraseña: 1234 

     **Estudiante**
     - Código: E55667788
     - Nombre: Estudiante
     - Rol: ESTUDIANTE
     - Contraseña: 1234 

## Evidencias

Ubicaciones de evidencias:

- Mockups: `/docs/Mockups`
- Imágenes del proyecto: `/Evidencias`


### Capturas
- Interfaz principal del sistema
- Panel de administración
- Gestión de préstamos y reservas
- Catálogo de libros

## Funcionalidades

### Gestión de Usuarios 
- **Autenticación y autorización** con Spring Security
- **Roles de usuario**: Administrador, Bibliotecario, Estudiante, Profesor
- **Redirección según el rol** del usuario

### Gestión de Libros
- **Catálogo completo** con información detallada
- **Gestión de autores, categorías y editoriales**
- **Control de stock** y disponibilidad

### Gestión de Préstamos
- **Sistema de préstamos** con fechas de devolución
- **Control de estados**: Activo, Devuelto, Retrasado 
- **Historial de préstamos** por usuario

### Gestión de Reservas
- **Sistema de reservas** para libros no disponibles
- **Control de estados**: Cancelada, Pendiente, Recogida, Vencida
- **Fechas límite** para recojo de reservas

### Interfaz Web
- **Diseño responsivo** con Thymeleaf
- **Navegación intuitiva** entre secciones
- **Páginas específicas** para cada rol de usuario




