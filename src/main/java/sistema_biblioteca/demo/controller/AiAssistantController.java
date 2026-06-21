package sistema_biblioteca.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sistema_biblioteca.demo.model.Libro;
import sistema_biblioteca.demo.repository.LibroRepository;
import sistema_biblioteca.demo.service.GeminiService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/ai")
public class AiAssistantController {

    @Autowired
    private GeminiService geminiService;

    @Autowired
    private LibroRepository libroRepository;

    public static class ChatRequest {
        public String message;
    }

    @PostMapping("/chat")
    public ResponseEntity<String> chatWithAssistant(@RequestBody ChatRequest request) {
        if (request == null || request.message == null || request.message.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Mensaje vacío.");
        }

        // Obtener el catálogo de libros activos
        List<Libro> librosActivos = libroRepository.findAll().stream()
                .filter(Libro::isActivo)
                .toList();

        // Crear una representación en texto del catálogo para que la IA la entienda
        String catalogo = librosActivos.stream()
                .map(l -> String.format("- Título: '%s', Autor: %s, Categoría: %s, Stock: %d", 
                        l.getTitulo(), 
                        l.getAutor() != null ? l.getAutor().getNombreCompleto() : "Desconocido", 
                        l.getCategoria() != null ? l.getCategoria().getNombre() : "General",
                        l.getStock()))
                .collect(Collectors.joining("; "));

        String respuesta = geminiService.obtenerRecomendacion(request.message, catalogo);
        return ResponseEntity.ok(respuesta);
    }
}
