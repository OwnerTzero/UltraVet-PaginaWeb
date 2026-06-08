package com.ultravet.veterinaria.controller;

import com.ultravet.veterinaria.model.SolicitudAdopcion;
import com.ultravet.veterinaria.repository.SolicitudAdopcionRepository;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AdopcionController {

    private final SolicitudAdopcionRepository adopcionRepository;

    // Inyección de dependencias por constructor para asegurar el desacoplamiento
    public AdopcionController(SolicitudAdopcionRepository adopcionRepository) {
        this.adopcionRepository = adopcionRepository;
    }

    @PostMapping("/adoptar")
    public String adoptar(@Valid @ModelAttribute("solicitud") SolicitudAdopcion solicitud,
            BindingResult result,
            Model model) {

        // Interceptación lógica de errores mediante el escudo de Spring Validation
        if (result.hasErrors()) {
            // Si hay campos inválidos, detenemos el guardado y recargamos la vista HTML
            return "adopcion";
        }

        // Si los datos son íntegros, invocamos la persistencia real en MySQL
        adopcionRepository.save(solicitud);

        // Inyección de mensaje de éxito para la confirmación visual en el cliente
        model.addAttribute("mensajeExito",
                "Solicitud enviada correctamente para adoptar a " + solicitud.getMascota());

        // Limpiamos el formulario enviando un objeto nuevo vacío para la siguiente
        // interacción
        model.addAttribute("solicitud", new SolicitudAdopcion());

        return "adopcion";
    }
}