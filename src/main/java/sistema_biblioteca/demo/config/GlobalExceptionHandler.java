package sistema_biblioteca.demo.config;

import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleGlobalException(Exception ex, Model model) {
        
        System.err.println("[ERROR CRÍTICO INTERCEPTADO] " + ex.getMessage());
        
        model.addAttribute("error", "Ocurrió un error interno en el servidor. El equipo de soporte ha sido notificado.");
        
        return "error";
    }
}
