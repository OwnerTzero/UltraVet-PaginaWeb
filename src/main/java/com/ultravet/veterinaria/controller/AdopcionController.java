package com.ultravet.veterinaria.controller;

import com.ultravet.veterinaria.model.SolicitudAdopcion;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AdopcionController {

    @PostMapping("/adoptar")
    public String adoptar(SolicitudAdopcion solicitud, Model model) {

        System.out.println("Nueva solicitud:");
        System.out.println("Nombre: " + solicitud.getNombre());
        System.out.println("Mascota: " + solicitud.getMascota());

        model.addAttribute("mensajeExito",
                "Solicitud enviada correctamente para adoptar a " +
                        solicitud.getMascota());

        return "adopcion";
    }
}