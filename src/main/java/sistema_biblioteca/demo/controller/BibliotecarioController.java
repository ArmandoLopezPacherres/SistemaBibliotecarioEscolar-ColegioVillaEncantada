package sistema_biblioteca.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.PostMapping;
import sistema_biblioteca.demo.model.Usuario;
import sistema_biblioteca.demo.model.Prestamo;
import sistema_biblioteca.demo.model.Libro;
import sistema_biblioteca.demo.model.Reserva;
import sistema_biblioteca.demo.model.enums.RolUsuario;
import sistema_biblioteca.demo.model.enums.EstadoPrestamo;
import sistema_biblioteca.demo.model.enums.EstadoReserva;
import sistema_biblioteca.demo.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import sistema_biblioteca.demo.repository.PrestamoRepository;
import sistema_biblioteca.demo.repository.LibroRepository;
import sistema_biblioteca.demo.repository.ReservaRepository;
import sistema_biblioteca.demo.dto.PerfilDTO;
import sistema_biblioteca.demo.dto.PasswordChangeDTO;
import java.security.Principal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/panel-bibliotecario")
public class BibliotecarioController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PrestamoRepository prestamoRepository;

    @Autowired
    private LibroRepository libroRepository;

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private void cargarUsuarioEnModelo(Model model, Principal principal) {
        if (principal != null) {
            usuarioRepository.findByCodigo(principal.getName()).ifPresent(usuario -> {
                model.addAttribute("usuario", usuario);
            });
        }
    }

    @GetMapping
    public String panelBibliotecario() {
        return "redirect:/panel-bibliotecario/lectores";
    }

    @GetMapping("/prestamos")
    public String prestamos(Model model, Principal principal) {
        cargarUsuarioEnModelo(model, principal);
        actualizarPrestamosVencidos();
        // Solo mostrar préstamos ACTIVOS y RETRASADOS (los DEVUELTOS van a la sección de devoluciones)
        List<Prestamo> prestamos = prestamoRepository.findAll().stream()
                .filter(p -> p.getEstado() != EstadoPrestamo.DEVUELTO)
                .sorted(Comparator.comparing(Prestamo::getUpdatedAt, Comparator.nullsLast(Comparator.reverseOrder()))
                                  .thenComparing(Prestamo::getId, Comparator.reverseOrder()))
                .toList();
        model.addAttribute("prestamos", prestamos);
        return "Bibliotecario/prestamos";
    }

    @GetMapping("/devoluciones")
    public String devoluciones(Model model, Principal principal) {
        cargarUsuarioEnModelo(model, principal);
        List<Prestamo> devoluciones = prestamoRepository.findAll().stream()
                .filter(p -> p.getEstado() == EstadoPrestamo.DEVUELTO)
                .sorted(Comparator.comparing(Prestamo::getUpdatedAt, Comparator.nullsLast(Comparator.reverseOrder()))
                                  .thenComparing(Prestamo::getId, Comparator.reverseOrder()))
                .toList();
        model.addAttribute("devoluciones", devoluciones);
        return "Bibliotecario/devoluciones";
    }

    @GetMapping("/inventario")
    public String inventario(Model model, Principal principal) {
        cargarUsuarioEnModelo(model, principal);
        model.addAttribute("libros", libroRepository.findAll());
        return "Bibliotecario/inventario";
    }

    @GetMapping("/inventario/buscar")
    @ResponseBody
    public ResponseEntity<List<Libro>> buscarLibrosAjax(@RequestParam("query") String query) {
        List<Libro> libros;
        if (query == null || query.trim().isEmpty()) {
            libros = libroRepository.findAll();
        } else {
            libros = libroRepository.findByTituloContainingIgnoreCaseOrAutorNombreCompletoContainingIgnoreCase(query.trim(), query.trim());
        }
        return ResponseEntity.ok(libros);
    }

    private String calcularEstadoLector(Usuario usuario) {
        if (usuario == null || usuario.getId() == null) {
            return "INACTIVO";
        }
        List<Prestamo> prestamos = prestamoRepository.findByUsuarioId(usuario.getId());
        if (prestamos == null || prestamos.isEmpty()) {
            return "INACTIVO";
        }

        LocalDate hoy = LocalDate.now();
        boolean tieneActivo = false;

        for (Prestamo p : prestamos) {
            if (p.getEstado() == EstadoPrestamo.RETRASADO) {
                return "MULTADO";
            }
            if (p.getEstado() == EstadoPrestamo.ACTIVO) {
                if (p.getFechaDevolucionEsperada() != null && hoy.isAfter(p.getFechaDevolucionEsperada())) {
                    return "MULTADO";
                }
                tieneActivo = true;
            }
        }

        if (tieneActivo) {
            return "ACTIVO";
        }
        return "INACTIVO";
    }

    @GetMapping("/lectores")
    public String lectores(
            @RequestParam(value = "search", required = false) String search,
            Model model,
            Principal principal) {
        cargarUsuarioEnModelo(model, principal);

        List<Usuario> matches;
        if (search != null && !search.trim().isEmpty()) {
            matches = usuarioRepository.findByCodigoContainingIgnoreCase(search.trim());
            model.addAttribute("searchValue", search);
        } else {
            matches = usuarioRepository.findAll();
        }

        List<Usuario> lectores = matches.stream()
                .filter(u -> u.getRol() == RolUsuario.ESTUDIANTE || u.getRol() == RolUsuario.PROFESOR)
                .toList();

        for (Usuario u : lectores) {
            u.setEstadoLector(calcularEstadoLector(u));
        }

        model.addAttribute("lectores", lectores);
        return "Bibliotecario/lectores";
    }

    @GetMapping("/lectores/buscar")
    @ResponseBody
    public ResponseEntity<List<Usuario>> buscarLectorAjax(@RequestParam(value = "codigo", required = false) String codigo) {
        List<Usuario> matches;
        if (codigo == null || codigo.trim().isEmpty()) {
            matches = usuarioRepository.findAll();
        } else {
            matches = usuarioRepository.findByCodigoContainingIgnoreCase(codigo.trim());
        }
        
        List<Usuario> lectores = matches.stream()
                .filter(u -> u.getRol() == RolUsuario.ESTUDIANTE || u.getRol() == RolUsuario.PROFESOR)
                .toList();
        
        for (Usuario u : lectores) {
            u.setEstadoLector(calcularEstadoLector(u));
        }
        
        return ResponseEntity.ok(lectores);
    }

    @GetMapping("/multas")
    public String multas(Model model, Principal principal) {
        cargarUsuarioEnModelo(model, principal);
        actualizarPrestamosVencidos();
        List<Prestamo> multados = prestamoRepository.findAll().stream()
                .filter(p -> p.getEstado() == EstadoPrestamo.RETRASADO)
                .toList();
        model.addAttribute("multas", multados);
        return "Bibliotecario/multas";
    }

    @GetMapping("/notificaciones")
    public String notificaciones(Model model, Principal principal) {
        cargarUsuarioEnModelo(model, principal);
        actualizarPrestamosVencidos();
        
        List<Reserva> solicitudes = reservaRepository.findByEstado(EstadoReserva.PENDIENTE);
        List<Prestamo> retrasos = prestamoRepository.findAll().stream()
                .filter(p -> p.getEstado() == EstadoPrestamo.RETRASADO)
                .toList();
        
        model.addAttribute("solicitudes", solicitudes);
        model.addAttribute("retrasos", retrasos);
        return "Bibliotecario/notificaciones";
    }

    @GetMapping("/perfil")
    public String perfil(Model model, Principal principal) {
        cargarUsuarioEnModelo(model, principal);
        if (principal != null) {
            usuarioRepository.findByCodigo(principal.getName()).ifPresent(usuario -> {
                String rolFormatted = usuario.getRol() == RolUsuario.BIBLIOTECARIO ? "BIBLIOTECARIO" : usuario.getRol().name();
                String correoMock = usuario.getNombre().toLowerCase().replace(" ", "") + "@gmail.com";
                PerfilDTO perfilDto = new PerfilDTO(
                    usuario.getNombre(),
                    rolFormatted,
                    "15/05/2026",
                    correoMock,
                    usuario.getCodigo()
                );
                model.addAttribute("perfil", perfilDto);
            });
        }
        return "Bibliotecario/perfil";
    }

    @PostMapping("/perfil/cambiar-password")
    @ResponseBody
    public ResponseEntity<String> cambiarPassword(
            PasswordChangeDTO dto,
            Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body("Usuario no autenticado.");
        }
        if (dto == null || 
            dto.getCurrentPassword() == null || dto.getCurrentPassword().trim().isEmpty() ||
            dto.getNewPassword() == null || dto.getNewPassword().trim().isEmpty() ||
            dto.getConfirmPassword() == null || dto.getConfirmPassword().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Todos los campos de contraseña son obligatorios.");
        }
        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            return ResponseEntity.badRequest().body("La nueva contraseña y su confirmación no coinciden.");
        }
        
        Optional<Usuario> optUsuario = usuarioRepository.findByCodigo(principal.getName());
        if (optUsuario.isEmpty()) {
            return ResponseEntity.badRequest().body("El usuario no existe.");
        }
        
        Usuario usuario = optUsuario.get();
        if (!passwordEncoder.matches(dto.getCurrentPassword(), usuario.getPassword())) {
            return ResponseEntity.badRequest().body("La contraseña actual es incorrecta.");
        }
        
        usuario.setPassword(passwordEncoder.encode(dto.getNewPassword().trim()));
        usuarioRepository.save(usuario);
        
        return ResponseEntity.ok("Contraseña actualizada con éxito.");
    }

    @GetMapping("/solicitudes")
    public String solicitudes(Model model, Principal principal) {
        cargarUsuarioEnModelo(model, principal);
        List<Reserva> pendientes = reservaRepository.findByEstado(EstadoReserva.PENDIENTE).stream()
                .sorted(Comparator.comparing(Reserva::getId).reversed())
                .toList();
        model.addAttribute("solicitudes", pendientes);
        return "Bibliotecario/solicitudes";
    }

    @GetMapping("/solicitudes/buscar")
    @ResponseBody
    public ResponseEntity<List<Reserva>> buscarSolicitudesAjax(
            @RequestParam(value = "query", required = false) String query,
            @RequestParam(value = "fecha", required = false) String fechaStr,
            @RequestParam(value = "rol", required = false) String rolStr) {
        
        List<Reserva> reservas;
        if (query != null && !query.trim().isEmpty()) {
            reservas = reservaRepository.buscarPendientesPorUsuarioOLibro(EstadoReserva.PENDIENTE, query.trim());
        } else {
            reservas = reservaRepository.findByEstado(EstadoReserva.PENDIENTE);
        }
        
        if (fechaStr != null && !fechaStr.trim().isEmpty()) {
            try {
                LocalDate fecha = LocalDate.parse(fechaStr.trim());
                reservas = reservas.stream()
                        .filter(r -> r.getFechaReserva() != null && r.getFechaReserva().equals(fecha))
                        .toList();
            } catch (Exception e) {
                // Ignorar error de parsing
            }
        }
        
        if (rolStr != null && !rolStr.trim().isEmpty()) {
            try {
                RolUsuario rol = RolUsuario.valueOf(rolStr.trim().toUpperCase());
                reservas = reservas.stream()
                        .filter(r -> r.getUsuario() != null && r.getUsuario().getRol() == rol)
                        .toList();
            } catch (Exception e) {
                // Ignorar error del enum
            }
        }
        
        return ResponseEntity.ok(reservas);
    }

    @PostMapping("/solicitudes/aceptar")
    @ResponseBody
    public ResponseEntity<String> aceptarSolicitud(@RequestParam("id") Long id) {
        Optional<Reserva> optReserva = reservaRepository.findById(id);
        if (optReserva.isEmpty()) {
            return ResponseEntity.badRequest().body("La solicitud no existe.");
        }
        
        Reserva reserva = optReserva.get();
        if (reserva.getEstado() != EstadoReserva.PENDIENTE) {
            return ResponseEntity.badRequest().body("Esta solicitud ya ha sido procesada.");
        }
        
        Libro libro = reserva.getLibro();
        if (libro == null) {
            return ResponseEntity.badRequest().body("El libro asociado no existe.");
        }
        
        // No se descuenta stock aquí; el stock se descuenta cuando se registra
        // físicamente la entrega del libro al usuario (endpoint /prestamos/registrar)
        
        // Cambiar estado de reserva a RECOGIDA (procesada)
        reserva.setEstado(EstadoReserva.RECOGIDA);
        reservaRepository.save(reserva);
        
        // Crear Prestamo (sin entrega física aún)
        Prestamo prestamo = new Prestamo();
        prestamo.setUsuario(reserva.getUsuario());
        prestamo.setLibro(libro);
        prestamo.setFechaPrestamo(reserva.getFechaReserva() != null ? reserva.getFechaReserva() : LocalDate.now());
        prestamo.setFechaDevolucionEsperada(reserva.getFechaLimiteRecojo() != null ? reserva.getFechaLimiteRecojo() : LocalDate.now().plusDays(7));
        prestamo.setEstado(EstadoPrestamo.ACTIVO);
        prestamo.setEntregado(false);
        prestamo.setCantidad(reserva.getCantidad());
        prestamoRepository.save(prestamo);
        
        return ResponseEntity.ok("Solicitud aceptada y préstamo registrado con éxito.");
    }

    @PostMapping("/solicitudes/rechazar")
    @ResponseBody
    public ResponseEntity<String> rechazarSolicitud(@RequestParam("id") Long id) {
        Optional<Reserva> optReserva = reservaRepository.findById(id);
        if (optReserva.isEmpty()) {
            return ResponseEntity.badRequest().body("La solicitud no existe.");
        }
        
        Reserva reserva = optReserva.get();
        if (reserva.getEstado() != EstadoReserva.PENDIENTE) {
            return ResponseEntity.badRequest().body("Esta solicitud ya ha sido procesada.");
        }
        
        // Cambiar estado de reserva a CANCELADA
        reserva.setEstado(EstadoReserva.CANCELADA);
        reservaRepository.save(reserva);
        
        return ResponseEntity.ok("Solicitud rechazada con éxito.");
    }

    private void actualizarPrestamosVencidos() {
        List<Prestamo> prestamos = prestamoRepository.findAll();
        LocalDate hoy = LocalDate.now();
        for (Prestamo p : prestamos) {
            if (p.isEntregado() && p.getEstado() == EstadoPrestamo.ACTIVO) {
                if (p.getFechaDevolucionEsperada() != null && hoy.isAfter(p.getFechaDevolucionEsperada())) {
                    p.setEstado(EstadoPrestamo.RETRASADO);
                    prestamoRepository.save(p);
                }
            }
        }
    }

    @PostMapping("/prestamos/registrar")
    @ResponseBody
    @org.springframework.transaction.annotation.Transactional
    public ResponseEntity<String> registrarEntrega(
            @RequestParam("id") Long id,
            @RequestParam("fechaPrestamo") String fechaPrestamoStr,
            @RequestParam("fechaDevolucionEsperada") String fechaDevolucionEsperadaStr) {
        
        Optional<Prestamo> optPrestamo = prestamoRepository.findById(id);
        if (optPrestamo.isEmpty()) {
            return ResponseEntity.badRequest().body("El préstamo no existe.");
        }
        
        Prestamo prestamo = optPrestamo.get();
        if (prestamo.isEntregado()) {
            return ResponseEntity.badRequest().body("Este préstamo ya está registrado como entregado.");
        }
        
        try {
            LocalDate fechaPrestamo = LocalDate.parse(fechaPrestamoStr);
            LocalDate fechaDevolucionEsperada = LocalDate.parse(fechaDevolucionEsperadaStr);
            
            if (fechaDevolucionEsperada.isBefore(fechaPrestamo)) {
                return ResponseEntity.badRequest().body("La fecha de devolución no puede ser anterior a la de préstamo.");
            }
            
            // Descontar stock al registrar la entrega física del libro
            Libro libroEntregado = prestamo.getLibro();
            if (libroEntregado != null) {
                if (libroEntregado.getStock() < prestamo.getCantidad()) {
                    return ResponseEntity.badRequest().body("No hay stock disponible suficiente para entregar este libro.");
                }
                libroEntregado.setStock(libroEntregado.getStock() - prestamo.getCantidad());
                if (libroEntregado.getStock() == 0) {
                    libroEntregado.setDisponible(false);
                }
                libroRepository.save(libroEntregado);
            }
            
            prestamo.setFechaPrestamo(fechaPrestamo);
            prestamo.setFechaDevolucionEsperada(fechaDevolucionEsperada);
            prestamo.setEntregado(true);
            
            if (LocalDate.now().isAfter(fechaDevolucionEsperada)) {
                prestamo.setEstado(EstadoPrestamo.RETRASADO);
            }
            
            prestamoRepository.save(prestamo);
            
            // Marcar la reserva como COMPLETADA para que ya no salga en "Mis solicitudes" pero se mantenga para notificaciones
            reservaRepository.findAll().stream()
                .filter(r -> r.getUsuario() != null && prestamo.getUsuario() != null && r.getUsuario().getId().equals(prestamo.getUsuario().getId()))
                .filter(r -> r.getLibro() != null && prestamo.getLibro() != null && r.getLibro().getId().equals(prestamo.getLibro().getId()))
                .filter(r -> r.getEstado() == EstadoReserva.RECOGIDA)
                .findFirst()
                .ifPresent(r -> {
                    r.setEstado(EstadoReserva.COMPLETADA);
                    reservaRepository.save(r);
                });
            
            return ResponseEntity.ok("Préstamo registrado como entregado con éxito.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Formato de fecha inválido.");
        }
    }

    @PostMapping("/prestamos/devolver")
    @ResponseBody
    @org.springframework.transaction.annotation.Transactional
    public ResponseEntity<String> devolverLibro(@RequestParam("id") Long id) {
        Optional<Prestamo> optPrestamo = prestamoRepository.findById(id);
        if (optPrestamo.isEmpty()) {
            return ResponseEntity.badRequest().body("El préstamo no existe.");
        }
        
        Prestamo prestamo = optPrestamo.get();
        if (prestamo.getEstado() == EstadoPrestamo.DEVUELTO) {
            return ResponseEntity.badRequest().body("Este préstamo ya está registrado como devuelto.");
        }
        
        // Cambiar estado a DEVUELTO y setear fecha de devolución real
        prestamo.setEstado(EstadoPrestamo.DEVUELTO);
        prestamo.setFechaDevolucionReal(LocalDate.now());
        prestamoRepository.save(prestamo);
        
        // Aumentar stock del libro
        Libro libro = prestamo.getLibro();
        if (libro != null) {
            libro.setStock(libro.getStock() + prestamo.getCantidad());
            libro.setDisponible(true);
            libroRepository.save(libro);
        }

        // Gamificación: Otorgar 10 puntos si la fecha de devolución es posterior a la de préstamo (mínimo 1 día)
        Usuario usuario = prestamo.getUsuario();
        if (usuario != null && prestamo.getFechaPrestamo() != null) {
            if (prestamo.getFechaDevolucionReal().isAfter(prestamo.getFechaPrestamo())) {
                Integer puntos = usuario.getPuntosLectura() != null ? usuario.getPuntosLectura() : 0;
                usuario.setPuntosLectura(puntos + 10);
                usuarioRepository.save(usuario);
            }
        }
        
        return ResponseEntity.ok("Libro registrado como devuelto con éxito.");
    }

    @PostMapping("/multas/pagar")
    @ResponseBody
    public ResponseEntity<String> pagarMulta(@RequestParam("id") Long id) {
        Optional<Prestamo> optPrestamo = prestamoRepository.findById(id);
        if (optPrestamo.isEmpty()) {
            return ResponseEntity.badRequest().body("El préstamo no existe.");
        }
        
        Prestamo prestamo = optPrestamo.get();
        if (prestamo.getEstado() != EstadoPrestamo.RETRASADO) {
            return ResponseEntity.badRequest().body("Este préstamo no tiene una multa activa.");
        }
        
        // Cambiar estado a DEVUELTO y registrar fecha real de devolución (hoy)
        prestamo.setEstado(EstadoPrestamo.DEVUELTO);
        prestamo.setFechaDevolucionReal(LocalDate.now());
        prestamoRepository.save(prestamo);
        
        // Aumentar stock del libro
        Libro libro = prestamo.getLibro();
        if (libro != null) {
            libro.setStock(libro.getStock() + prestamo.getCantidad());
            libro.setDisponible(true);
            libroRepository.save(libro);
        }
        
        return ResponseEntity.ok("Multa de 30 soles pagada y libro registrado como devuelto con éxito.");
    }

    @GetMapping("/escaner")
    public String escaner(Model model, Principal principal) {
        cargarUsuarioEnModelo(model, principal);
        return "Bibliotecario/escaner";
    }

    @GetMapping("/escaner/usuario")
    @ResponseBody
    public ResponseEntity<?> buscarUsuarioEscaner(@RequestParam("codigo") String codigo) {
        Optional<Usuario> optUsuario = usuarioRepository.findByCodigo(codigo);
        if (optUsuario.isEmpty()) {
            return ResponseEntity.badRequest().body("Usuario no encontrado.");
        }
        Usuario usuario = optUsuario.get();
        // Obtener libros actualmente prestados (ACTIVO o RETRASADO)
        List<Prestamo> prestados = prestamoRepository.findByUsuarioId(usuario.getId()).stream()
            .filter(p -> p.getEstado() == EstadoPrestamo.ACTIVO || p.getEstado() == EstadoPrestamo.RETRASADO)
            .toList();
        
        return ResponseEntity.ok(new java.util.HashMap<String, Object>() {{
            put("nombre", usuario.getNombre());
            put("codigo", usuario.getCodigo());
            put("rol", usuario.getRol().name());
            put("prestados", prestados.stream().map(p -> new java.util.HashMap<String, Object>() {{
                put("idPrestamo", p.getId());
                put("idLibro", p.getLibro() != null ? p.getLibro().getId() : null);
                put("titulo", p.getLibro() != null ? p.getLibro().getTitulo() : "Libro desconocido");
            }}).toList());
        }});
    }

    @GetMapping("/escaner/libro")
    @ResponseBody
    public ResponseEntity<?> buscarLibroEscaner(@RequestParam("id") Long id) {
        Optional<Libro> optLibro = libroRepository.findById(id);
        if (optLibro.isEmpty()) {
            return ResponseEntity.badRequest().body("Libro no encontrado.");
        }
        return ResponseEntity.ok(optLibro.get());
    }

    @org.springframework.beans.factory.annotation.Value("${biblioteca.limite.estudiante:3}")
    private int limiteEstudiante;

    @org.springframework.beans.factory.annotation.Value("${biblioteca.limite.profesor:20}")
    private int limiteProfesor;

    @PostMapping("/escaner/procesar")
    @ResponseBody
    @org.springframework.transaction.annotation.Transactional
    public ResponseEntity<String> procesarEscaner(
            @RequestParam("codigoUsuario") String codigoUsuario,
            @RequestParam(value = "idsDevolver", required = false) List<Long> idsDevolver,
            @RequestParam(value = "idsPrestar", required = false) List<Long> idsPrestar,
            @RequestParam(value = "fechaDevolucion", required = false) String fechaDevolucionStr) {

        Optional<Usuario> optUsuario = usuarioRepository.findByCodigo(codigoUsuario);
        if (optUsuario.isEmpty()) {
            return ResponseEntity.badRequest().body("Usuario no encontrado.");
        }
        Usuario usuario = optUsuario.get();

        int puntosGanados = 0;
        long cantidadADevolver = 0;

        // Procesar Devoluciones (recibimos IDs de prestamos)
        if (idsDevolver != null && !idsDevolver.isEmpty()) {
            for (Long idPrestamo : idsDevolver) {
                Optional<Prestamo> optPrestamo = prestamoRepository.findById(idPrestamo);
                if (optPrestamo.isPresent()) {
                    Prestamo prestamo = optPrestamo.get();
                    if (prestamo.getEstado() != EstadoPrestamo.DEVUELTO) {
                        cantidadADevolver += prestamo.getCantidad() > 0 ? prestamo.getCantidad() : 1;
                        prestamo.setEstado(EstadoPrestamo.DEVUELTO);
                        prestamo.setFechaDevolucionReal(LocalDate.now());
                        prestamoRepository.save(prestamo);
                        
                        Libro libro = prestamo.getLibro();
                        if (libro != null) {
                            libro.setStock(libro.getStock() + (prestamo.getCantidad() > 0 ? prestamo.getCantidad() : 1));
                            libro.setDisponible(true);
                            libroRepository.save(libro);
                        }

                        // Puntos gamificación
                        if (prestamo.getFechaPrestamo() != null && prestamo.getFechaDevolucionReal().isAfter(prestamo.getFechaPrestamo())) {
                            puntosGanados += 10;
                        }
                    }
                }
            }
        }

        // Agrupar IDs para saber las cantidades (ej. [5, 5, 5] -> Libro 5, cantidad 3)
        java.util.Map<Long, Long> cantidades = idsPrestar == null ? java.util.Collections.emptyMap() : 
            idsPrestar.stream().collect(java.util.stream.Collectors.groupingBy(id -> id, java.util.stream.Collectors.counting()));

        // Validar límite total
        int limite = usuario.getRol() == RolUsuario.ESTUDIANTE ? limiteEstudiante : limiteProfesor;
        
        long totalPrestamosActivos = prestamoRepository.findByUsuarioId(usuario.getId()).stream()
                .filter(p -> p.getEstado() == EstadoPrestamo.ACTIVO || p.getEstado() == EstadoPrestamo.RETRASADO)
                .mapToLong(p -> p.getCantidad() > 0 ? p.getCantidad() : 1)
                .sum();
                
        long totalNuevos = cantidades.values().stream().mapToLong(Long::longValue).sum();

        if ((totalPrestamosActivos - cantidadADevolver + totalNuevos) > limite) {
            return ResponseEntity.badRequest().body("Límite de préstamos excedido. El límite máximo es " + limite + " libros.");
        }

        // Validar duplicados para estudiante
        if (usuario.getRol() == RolUsuario.ESTUDIANTE) {
            for (Long count : cantidades.values()) {
                if (count > 1) {
                    return ResponseEntity.badRequest().body("Los estudiantes solo pueden llevar 1 copia de cada libro.");
                }
            }
        }

        // Procesar Nuevos Préstamos agrupados
        if (!cantidades.isEmpty()) {
            LocalDate fechaDevolucion = LocalDate.now().plusDays(7);
            if (fechaDevolucionStr != null && !fechaDevolucionStr.trim().isEmpty()) {
                try {
                    fechaDevolucion = LocalDate.parse(fechaDevolucionStr);
                } catch (Exception e) {}
            }

            for (java.util.Map.Entry<Long, Long> entry : cantidades.entrySet()) {
                Long idLibro = entry.getKey();
                int cantidadPedida = entry.getValue().intValue();

                Optional<Libro> optLibro = libroRepository.findById(idLibro);
                if (optLibro.isPresent()) {
                    Libro libro = optLibro.get();
                    if (libro.getStock() >= cantidadPedida) {
                        libro.setStock(libro.getStock() - cantidadPedida);
                        if (libro.getStock() == 0) libro.setDisponible(false);
                        libroRepository.save(libro);

                        Prestamo prestamo = new Prestamo();
                        prestamo.setUsuario(usuario);
                        prestamo.setLibro(libro);
                        prestamo.setFechaPrestamo(LocalDate.now());
                        prestamo.setFechaDevolucionEsperada(fechaDevolucion);
                        prestamo.setEstado(EstadoPrestamo.ACTIVO);
                        prestamo.setEntregado(true);
                        prestamo.setCantidad(cantidadPedida);
                        prestamoRepository.save(prestamo);
                    } else {
                        return ResponseEntity.badRequest().body("Stock insuficiente para el libro: " + libro.getTitulo());
                    }
                }
            }
        }

        if (puntosGanados > 0) {
            Integer puntosAnteriores = usuario.getPuntosLectura() != null ? usuario.getPuntosLectura() : 0;
            usuario.setPuntosLectura(puntosAnteriores + puntosGanados);
            usuarioRepository.save(usuario);
        }

        return ResponseEntity.ok("Operación completada con éxito.");
    }
}
