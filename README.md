<img width="1600" height="764" alt="imagen" src="https://github.com/user-attachments/assets/1e78494a-8b2f-4b0f-8ec3-fe18a582a61d" />

# Sistema Bibliotecario Escolar - Colegio Villa Encantada

Sistema de gestión bibliotecaria completo enfocado al entorno escolar del Colegio Villa Encantada, diseñado para administrar préstamos, reservas y catálogo de libros de manera eficiente.

##  Tecnologías y Herramientas

- **Java 21** - Lenguaje de programación principal
- **Spring Boot 3.x** - Framework principal para el backend
- **Spring Security** - Gestión de autenticación, encriptación de contraseñas y autorización basada en roles
- **Spring Data JPA / Hibernate** - ORM y persistencia de datos relacional
- **Thymeleaf** - Motor de plantillas para el renderizado dinámico en el frontend
- **MySQL** - Base de datos relacional
- **Maven** - Gestión de dependencias y construcción del proyecto
- **HTML5, CSS3, JavaScript** - Diseño e interacción de la interfaz web (UI Responsiva)

## 📁 Arquitectura y Estructura del Proyecto

El proyecto sigue un patrón de arquitectura Modelo-Vista-Controlador (MVC) bien definido y organizado en las siguientes capas principales:

- **`config/`**: Configuraciones globales de la aplicación (Seguridad web en `SecurityConfig` e inicialización de datos por defecto con `DataInitializer`).
- **`controller/`**: Controladores MVC (`WebController`, `AdminController`, `EstudianteController`, etc.) y APIs REST (`DashboardApiController`, `ProfesorApiController`) que sirven datos en formato JSON para el frontend.
- **`dto/`**: Objetos de Transferencia de Datos (`UsuarioDTO`, `PerfilDTO`, `DashboardChartsDTO`, etc.) empleados para optimizar el paso de información entre el backend y las vistas sin exponer entidades completas.
- **`model/`**: Entidades de dominio mapeadas a la base de datos (`Usuario`, `Libro`, `Autor`, `Categoria`, `Editorial`, `Prestamo`, `Reserva`) y Enumeraciones para estados fijos (`RolUsuario`, `EstadoPrestamo`, `EstadoReserva`).
- **`repository/`**: Interfaces de Spring Data JPA que abstraen el acceso a la base de datos.
- **`security/`**: Implementación personalizada de `UserDetailsService` para cargar los usuarios y validar credenciales contra la BD.
- **`service/`**: Lógica de negocio compleja declarada a través de interfaces y resuelta en sus respectivas implementaciones (`impl/`).
- **`resources/static/` & `resources/templates/`**: Recursos estáticos (estilos CSS e imágenes modulares) y plantillas Thymeleaf. Están rigurosamente ordenadas por rol (Administrador, Bibliotecario, Estudiante, Profesor).

## Roles de Usuario y Funcionalidades

El sistema está diseñado con control de acceso basado en roles (RBAC) asegurando que cada perfil tenga vistas y permisos específicos:

### 1. Administrador
- Acceso a un Dashboard estadístico (`panel-admin`) con métricas globales del sistema.
- Gestión de usuarios y asignación de roles.
- Gestión completa del catálogo bibliotecario (libros, autores, categorías, editoriales).
- Generación y visualización de reportes.

### 2. Bibliotecario
- Control estricto de inventario y catálogo de libros.
- Gestión de préstamos (entregas) y devoluciones.
- Administración de multas y lectores.
- Revisión de solicitudes y envío de notificaciones.

### 3. Profesor / Estudiante
- Búsqueda avanzada de libros en el catálogo.
- Consulta del historial personal de préstamos.
- Seguimiento en "Mi Actividad" y revisión de notificaciones.
- Gestión y actualización del perfil de usuario.

##  Ejecución y Configuración

1. **Clonar el repositorio**
   ```bash
   git clone https://github.com/HectorRamonDeV/SistemaBibliotecarioEscolar-ColegioVillaEncantada.git
   cd SistemaBibliotecarioEscolar-ColegioVillaEncantada
   ```

2. **Configurar la base de datos (MySQL)**
   - Configurar las credenciales de la base de datos en `src/main/resources/application.properties`.
   - **Nota:** La estructura de tablas y relaciones se creará automáticamente gracias a la configuración de Hibernate (`ddl-auto`) al iniciar la aplicación. Adicionalmente, el `DataInitializer` se encarga de poblar el sistema con usuarios administradores y datos de prueba.

3. **Ejecutar la aplicación**
   ```bash
   ./mvnw spring-boot:run
   ```
   *(También puedes abrir y ejecutar la clase principal `DemoApplication.java` directamente desde tu IDE como IntelliJ, Eclipse o VS Code).*

4. **Acceso Inicial**
   - URL de despliegue local: [http://localhost:8080](http://localhost:8080)
   - **Credenciales por defecto** (Generadas para uso inmediato en pruebas):
     - **Admin**: Código: `A12345678` | Contraseña: `1234`
     - **Bibliotecario**: Código: `B87654321` | Contraseña: `1234`
     - **Profesor**: Código: `P11223344` | Contraseña: `1234`
     - **Estudiante**: Código: `E55667788` | Contraseña: `1234`

## Configuración del Asistente IA (Gemini)

El módulo de Inteligencia Artificial requiere una API Key de Google Gemini para funcionar. Por seguridad, esta llave **nunca debe subirse al repositorio público**.

Para configurarlo en tu entorno de desarrollo local:
1. Crea un archivo llamado `application-local.properties` en la carpeta `src/main/resources/`.
2. Añade tu API Key dentro del archivo con este formato:
   ```properties
   GEMINI_API_KEY=tu_llave_real_aqui
   ```
*(Nota: Este archivo ya está excluido en el `.gitignore` para proteger tu información).*

## Ramas del Repositorio

- `main` - Rama principal con la versión estable
- `develop` - Rama de desarrollo integrador de características
- `feature/*` - Ramas dedicadas a funcionalidades específicas 

##  Evidencias y Documentación

- **Mockups y Diseño de Interfaces:** Se encuentran en el directorio `/docs/Mockups`
- **Imágenes y Capturas del Sistema:** Ubicadas en `docs/Evidencias`
