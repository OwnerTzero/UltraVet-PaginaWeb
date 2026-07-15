package com.ultravet.veterinaria.controller;

import com.ultravet.veterinaria.dto.SolicitudAdopcionForm;
import com.ultravet.veterinaria.model.EstadoSolicitud;
import com.ultravet.veterinaria.model.Mascota;
import com.ultravet.veterinaria.model.Rol;
import com.ultravet.veterinaria.model.SolicitudAdopcion;
import com.ultravet.veterinaria.model.Usuario;
import com.ultravet.veterinaria.repository.EstadoSolicitudRepository;
import com.ultravet.veterinaria.repository.MascotaRepository;
import com.ultravet.veterinaria.repository.RolRepository;
import com.ultravet.veterinaria.repository.SolicitudAdopcionRepository;
import com.ultravet.veterinaria.repository.UsuarioRepository;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AdopcionController {

    private static final String ROL_CLIENTE = "CLIENTE";
    private static final String ESTADO_SOLICITUD_PENDIENTE = "PENDIENTE";
    private static final String ESTADO_MASCOTA_DISPONIBLE = "DISPONIBLE";
    private static final List<String> ESTADOS_ADOPCION = List.of("DISPONIBLE", "EN_PROCESO", "ADOPTADA");

    private final SolicitudAdopcionRepository adopcionRepository;
    private final UsuarioRepository usuarioRepository;
    private final MascotaRepository mascotaRepository;
    private final RolRepository rolRepository;
    private final EstadoSolicitudRepository estadoSolicitudRepository;

    public AdopcionController(SolicitudAdopcionRepository adopcionRepository,
            UsuarioRepository usuarioRepository,
            MascotaRepository mascotaRepository,
            RolRepository rolRepository,
            EstadoSolicitudRepository estadoSolicitudRepository) {
        this.adopcionRepository = adopcionRepository;
        this.usuarioRepository = usuarioRepository;
        this.mascotaRepository = mascotaRepository;
        this.rolRepository = rolRepository;
        this.estadoSolicitudRepository = estadoSolicitudRepository;
    }

    @GetMapping("/adopcion")
    public String adopcion(Model model) {
        prepararVistaAdopcion(model);
        return "adopcion";
    }

    @PostMapping("/adoptar")
    @Transactional
    public String adoptar(@Valid @ModelAttribute("solicitud") SolicitudAdopcionForm solicitudForm,
            BindingResult result,
            Model model) {

        prepararVistaAdopcion(model);
        validarDniUnico(solicitudForm.getCorreo(), solicitudForm.getDni(), result);

        if (result.hasErrors()) {
            model.addAttribute("mensajeError", "Revisa los datos ingresados e intenta nuevamente.");
            return "adopcion";
        }

        Mascota mascota = mascotaRepository.findByIdAndActivoTrue(solicitudForm.getMascotaId())
                .orElse(null);

        if (mascota == null) {
            model.addAttribute("mensajeError", "La mascota seleccionada no existe o no esta activa.");
            return "adopcion";
        }

        if (!estaDisponibleParaAdopcion(mascota)) {
            model.addAttribute("mensajeError", "La mascota seleccionada no esta disponible para adopcion.");
            return "adopcion";
        }

        Usuario usuario = obtenerOCrearUsuario(solicitudForm);

        if (adopcionRepository.existsByUsuarioAndMascota(usuario, mascota)) {
            model.addAttribute("mensajeError",
                    "Ya existe una solicitud de adopcion para " + mascota.getNombre() + " con este correo.");
            return "adopcion";
        }

        EstadoSolicitud estadoPendiente = estadoSolicitudRepository.findByNombre(ESTADO_SOLICITUD_PENDIENTE)
                .orElseThrow(() -> new IllegalStateException("No existe el estado de solicitud PENDIENTE."));

        SolicitudAdopcion solicitud = new SolicitudAdopcion();
        solicitud.setUsuario(usuario);
        solicitud.setMascota(mascota);
        solicitud.setEstado(estadoPendiente);
        solicitud.setDistrito(limpiarTexto(solicitudForm.getDistrito()));
        solicitud.setExperienciaMascotas(limpiarTexto(solicitudForm.getExperienciaMascotas()));
        solicitud.setFechaEnvio(LocalDateTime.now());

        adopcionRepository.save(solicitud);

        model.addAttribute("mensajeExito",
                "Solicitud enviada correctamente para adoptar a " + mascota.getNombre());
        model.addAttribute("solicitud", new SolicitudAdopcionForm());

        return "adopcion";
    }

    private void prepararVistaAdopcion(Model model) {
        model.addAttribute("mascotas",
                mascotaRepository.findByActivoTrueAndEstadoNombreInOrderByIdAsc(ESTADOS_ADOPCION));

        if (!model.containsAttribute("solicitud")) {
            model.addAttribute("solicitud", new SolicitudAdopcionForm());
        }
    }

    private boolean estaDisponibleParaAdopcion(Mascota mascota) {
        return mascota.getEstado() != null
                && ESTADO_MASCOTA_DISPONIBLE.equalsIgnoreCase(mascota.getEstado().getNombre());
    }

    private Usuario obtenerOCrearUsuario(SolicitudAdopcionForm form) {
        return usuarioRepository.findByCorreo(form.getCorreo().trim().toLowerCase())
                .map(usuario -> actualizarDatosBasicos(usuario, form))
                .orElseGet(() -> crearUsuarioCliente(form));
    }

    private Usuario actualizarDatosBasicos(Usuario usuario, SolicitudAdopcionForm form) {
        usuario.setNombre(form.getNombre().trim());
        usuario.setDni(form.getDni().trim());
        usuario.setSexo(form.getSexo());
        usuario.setTelefono(form.getTelefono().trim());
        return usuarioRepository.save(usuario);
    }

    private Usuario crearUsuarioCliente(SolicitudAdopcionForm form) {
        Rol rolCliente = rolRepository.findByNombre(ROL_CLIENTE)
                .orElseThrow(() -> new IllegalStateException("No existe el rol CLIENTE."));

        Usuario usuario = new Usuario();
        usuario.setRol(rolCliente);
        usuario.setNombre(form.getNombre().trim());
        usuario.setDni(form.getDni().trim());
        usuario.setSexo(form.getSexo());
        usuario.setCorreo(form.getCorreo().trim().toLowerCase());
        usuario.setTelefono(form.getTelefono().trim());
        usuario.setActivo(true);

        return usuarioRepository.save(usuario);
    }

    private void validarDniUnico(String correo, String dni, BindingResult result) {
        if (result.hasFieldErrors("correo") || result.hasFieldErrors("dni")) {
            return;
        }

        Optional<Usuario> usuarioCorreo = usuarioRepository.findByCorreoIgnoreCase(correo.trim().toLowerCase());
        Optional<Usuario> usuarioDni = usuarioRepository.findByDni(dni.trim());

        if (usuarioDni.isPresent()
                && (usuarioCorreo.isEmpty()
                        || !Objects.equals(usuarioDni.get().getId(), usuarioCorreo.get().getId()))) {
            result.rejectValue("dni", "dni.duplicado", "Ya existe una cuenta con ese DNI.");
        }
    }

    private String limpiarTexto(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }

        return valor.trim();
    }
}
