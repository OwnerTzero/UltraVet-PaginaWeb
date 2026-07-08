package com.ultravet.veterinaria.controller.api;

import com.ultravet.veterinaria.model.Cita;
import com.ultravet.veterinaria.repository.CitaRepository;
import com.ultravet.veterinaria.security.UsuarioPrincipal;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/citas")
public class CitasApiController {

    private final CitaRepository citaRepository;

    public CitasApiController(CitaRepository citaRepository) {
        this.citaRepository = citaRepository;
    }

    @GetMapping("/mias")
    public ResponseEntity<?> misCitas(@AuthenticationPrincipal UsuarioPrincipal principal) {
        List<Cita> citas = citaRepository.findByUsuarioIdAndActivoTrueOrderByFechaDescHoraDesc(principal.getId());

        List<Map<String, Object>> respuesta = citas.stream()
                .map(cita -> Map.<String, Object>of(
                        "id", cita.getId(),
                        "fecha", cita.getFecha(),
                        "hora", cita.getHora(),
                        "servicio", cita.getServicio().getNombre(),
                        "estado", cita.getEstado().getNombre()))
                .toList();

        return ResponseEntity.ok(respuesta);
    }
}
