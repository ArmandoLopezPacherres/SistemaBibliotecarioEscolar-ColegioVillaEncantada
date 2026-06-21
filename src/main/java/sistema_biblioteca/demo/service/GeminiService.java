package sistema_biblioteca.demo.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    public String obtenerRecomendacion(String promptUsuario, String catalogo) {
        if ("YOUR_API_KEY_HERE".equals(apiKey) || apiKey == null || apiKey.trim().isEmpty()) {
            return "El Asistente Bibliotecario IA no está configurado (Falta la API Key de Gemini).";
        }
        
        try {
            String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + apiKey.trim();
            
            String systemPrompt = "Eres el Asistente Bibliotecario IA del Colegio Villa Encantada. " +
                "Tu objetivo es recomendar libros basados SOLO en este catálogo: [" +
                catalogo + "]. " +
                "REGLA DE ORO: Sé muy amigable, entusiasta y empático con el estudiante, pero NUNCA repitas tu eslogan de presentación ('Hola, soy el asistente...'). Responde directo a la consulta con calidez. " +
                "Formatea tu respuesta utilizando etiquetas HTML básicas (como <b>, <ul>, <li>) en lugar de markdown. " +
                "IMPORTANTE: Cuando recomiendes un libro, incluye siempre al final este botón HTML exacto (reemplaza TITULO con el título del libro en minúsculas): " +
                "<button class=\"btn btn-sm text-white mt-2\" style=\"background-color: #0c2947; border-radius: 15px;\" onclick=\"var inp = document.getElementById('searchInput'); inp.value='TITULO'; inp.dispatchEvent(new Event('input')); if(typeof aplicarFiltros === 'function'){aplicarFiltros();}\">Buscar libro</button>";
            
            String fullPrompt = systemPrompt + "\n\nPregunta del estudiante: " + promptUsuario;
            
            // Construimos el JSON a mano de manera segura usando ObjectMapper
            ObjectMapper mapper = new ObjectMapper();
            var parts = mapper.createObjectNode().put("text", fullPrompt);
            var content = mapper.createObjectNode().set("parts", mapper.createArrayNode().add(parts));
            var root = mapper.createObjectNode().set("contents", mapper.createArrayNode().add(content));
            
            String jsonBody = mapper.writeValueAsString(root);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                JsonNode responseNode = mapper.readTree(response.body());
                return responseNode.path("candidates").get(0)
                        .path("content").path("parts").get(0)
                        .path("text").asText();
            } else {
                return "Lo siento, tuve un problema interno de IA. Código: " + response.statusCode();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Error al procesar la solicitud con el Asistente Bibliotecario IA.";
        }
    }
}
