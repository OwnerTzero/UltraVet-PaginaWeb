package com.ultravet.veterinaria.controller.api;

import com.ultravet.veterinaria.repository.CitaRepository;
import com.ultravet.veterinaria.repository.UsuarioRepository;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminApiController {

    private final UsuarioRepository usuarioRepository;
    private final CitaRepository citaRepository;

    public AdminApiController(UsuarioRepository usuarioRepository, CitaRepository citaRepository) {
        this.usuarioRepository = usuarioRepository;
        this.citaRepository = citaRepository;
    }

    @GetMapping("/resumen")
    public ResponseEntity<?> resumen() {
        int usuariosActivos = usuarioRepository.findByActivoTrueOrderByIdAsc().size();
        int citasActivas = citaRepository.findByActivoTrueOrderByFechaDescHoraDesc().size();

        return ResponseEntity.ok(Map.of(
                "usuariosActivos", usuariosActivos,
                "citasActivas", citasActivas));
    }
}
