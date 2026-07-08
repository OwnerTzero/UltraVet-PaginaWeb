package com.ultravet.veterinaria.controller;

import com.ultravet.veterinaria.dto.SolicitudCitaForm;
import com.ultravet.veterinaria.model.Cita;
import com.ultravet.veterinaria.model.EstadoCita;
import com.ultravet.veterinaria.model.EstadoMascota;
import com.ultravet.veterinaria.model.Mascota;
import com.ultravet.veterinaria.model.PrioridadMascota;
import com.ultravet.veterinaria.model.Rol;
import com.ultravet.veterinaria.model.Servicio;
import com.ultravet.veterinaria.model.Usuario;
import com.ultravet.veterinaria.repository.CitaRepository;
import com.ultravet.veterinaria.repository.EstadoCitaRepository;
import com.ultravet.veterinaria.repository.EstadoMascotaRepository;
import com.ultravet.veterinaria.repository.MascotaRepository;
import com.ultravet.veterinaria.repository.PrioridadMascotaRepository;
import com.ultravet.veterinaria.repository.RolRepository;
import com.ultravet.veterinaria.repository.ServicioRepository;
import com.ultravet.veterinaria.repository.UsuarioRepository;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ServiciosController {

    private static final String ROL_CLIENTE = "CLIENTE";
    private static final String ESTADO_CITA_PENDIENTE = "PENDIENTE";
    private static final String ESTADO_CITA_CANCELADA = "CANCELADA";
    private static final String ESTADO_MASCOTA_REGISTRADA = "REGISTRADA";
    private static final String PRIORIDAD_MASCOTA_NORMAL = "NORMAL";

    private final ServicioRepository servicioRepository;
    private final CitaRepository citaRepository;
    private final UsuarioRepository usuarioRepository;
    private final MascotaRepository mascotaRepository;
    private final RolRepository rolRepository;
    private final EstadoCitaRepository estadoCitaRepository;
    private final EstadoMascotaRepository estadoMascotaRepository;
    private final PrioridadMascotaRepository prioridadMascotaRepository;

    public ServiciosController(ServicioRepository servicioRepository,
            CitaRepository citaRepository,
            UsuarioRepository usuarioRepository,
            MascotaRepository mascotaRepository,
            RolRepository rolRepository,
            EstadoCitaRepository estadoCitaRepository,
            EstadoMascotaRepository estadoMascotaRepository,
            PrioridadMascotaRepository prioridadMascotaRepository) {
        this.servicioRepository = servicioRepository;
        this.citaRepository = citaRepository;
        this.usuarioRepository = usuarioRepository;
        this.mascotaRepository = mascotaRepository;
        this.rolRepository = rolRepository;
        this.estadoCitaRepository = estadoCitaRepository;
        this.estadoMascotaRepository = estadoMascotaRepository;
        this.prioridadMascotaRepository = prioridadMascotaRepository;
    }

    @GetMapping("/servicios")
    public String servicios(Model model, HttpSession session) {
        prepararVistaServicios(model, session);
        return "servicios";
    }

    @PostMapping("/citas")
    @Transactional
    public String registrarCita(@Valid @ModelAttribute("citaForm") SolicitudCitaForm citaForm,
            BindingResult result,
            Model model,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Servicio servicio = null;
        if (citaForm.getServicioId() != null) {
            servicio = servicioRepository.findByIdAndActivoTrue(citaForm.getServicioId()).orElse(null);
            if (servicio == null) {
                result.rejectValue("servicioId", "servicio.noDisponible",
                        "El servicio seleccionado no esta disponible.");
            }
        }

        validarDniUnico(citaForm.getCorreo(), citaForm.getDni(), result);

        if (result.hasErrors()) {
            prepararVistaServicios(model, session);
            model.addAttribute("mensajeError", "Revisa los datos ingresados e intenta nuevamente.");
            model.addAttribute("abrirModalCita", true);
            return "servicios";
        }

        Usuario usuario = obtenerOCrearUsuario(citaForm);
        Mascota mascota = obtenerOCrearMascota(citaForm, usuario);
        EstadoCita estadoPendiente = estadoCitaRepository.findByNombre(ESTADO_CITA_PENDIENTE)
                .orElseThrow(() -> new IllegalStateException("No existe el estado de cita PENDIENTE."));

        Cita cita = new Cita();
        cita.setUsuario(usuario);
        cita.setMascota(mascota);
        cita.setServicio(servicio);
        cita.setEstado(estadoPendiente);
        cita.setFecha(citaForm.getFecha());
        cita.setHora(citaForm.getHora());
        cita.setComentario(limpiarTexto(citaForm.getComentario()));

        citaRepository.save(cita);
        actualizarSesion(session, usuario);

        redirectAttributes.addFlashAttribute("mensajeExito",
                "Cita registrada correctamente. Quedo con estado PENDIENTE.");
        return "redirect:/servicios#misCitas";
    }

    @PostMapping("/citas/{id}/cancelar")
    @Transactional
    public String cancelarCita(@PathVariable Long id,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Long usuarioId = obtenerUsuarioIdSesion(session);
        if (usuarioId == null) {
            redirectAttributes.addFlashAttribute("mensajeError",
                    "Inicia sesion para cancelar una cita registrada.");
            return "redirect:/servicios#misCitas";
        }

        Cita cita = citaRepository.findByIdAndUsuarioId(id, usuarioId).orElse(null);
        if (cita == null) {
            redirectAttributes.addFlashAttribute("mensajeError",
                    "No se encontro la cita seleccionada.");
            return "redirect:/servicios#misCitas";
        }

        EstadoCita estadoCancelada = estadoCitaRepository.findByNombre(ESTADO_CITA_CANCELADA)
                .orElseThrow(() -> new IllegalStateException("No existe el estado de cita CANCELADA."));
        cita.setEstado(estadoCancelada);
        citaRepository.save(cita);

        redirectAttributes.addFlashAttribute("mensajeExito", "Cita cancelada correctamente.");
        return "redirect:/servicios#misCitas";
    }

    private void prepararVistaServicios(Model model, HttpSession session) {
        model.addAttribute("servicios", servicioRepository.findByActivoTrueOrderByIdAsc());
        model.addAttribute("citas", obtenerCitasSesion(session));

        if (!model.containsAttribute("citaForm")) {
            model.addAttribute("citaForm", new SolicitudCitaForm());
        }
    }

    private Object obtenerCitasSesion(HttpSession session) {
        Long usuarioId = obtenerUsuarioIdSesion(session);
        if (usuarioId == null) {
            return java.util.List.of();
        }

        return citaRepository.findByUsuarioIdAndActivoTrueOrderByFechaDescHoraDesc(usuarioId);
    }

    private Usuario obtenerOCrearUsuario(SolicitudCitaForm form) {
        String correo = form.getCorreo().trim().toLowerCase();

        return usuarioRepository.findByCorreo(correo)
                .map(usuario -> actualizarDatosBasicos(usuario, form))
                .orElseGet(() -> crearUsuarioCliente(form, correo));
    }

    private Usuario actualizarDatosBasicos(Usuario usuario, SolicitudCitaForm form) {
        usuario.setNombre(form.getNombre().trim());
        usuario.setDni(form.getDni().trim());
        usuario.setSexo(form.getSexo());
        usuario.setTelefono(form.getTelefono().trim());
        usuario.setActivo(true);
        return usuarioRepository.save(usuario);
    }

    private Usuario crearUsuarioCliente(SolicitudCitaForm form, String correo) {
        Rol rolCliente = rolRepository.findByNombre(ROL_CLIENTE)
                .orElseThrow(() -> new IllegalStateException("No existe el rol CLIENTE."));

        Usuario usuario = new Usuario();
        usuario.setRol(rolCliente);
        usuario.setNombre(form.getNombre().trim());
        usuario.setDni(form.getDni().trim());
        usuario.setSexo(form.getSexo());
        usuario.setCorreo(correo);
        usuario.setTelefono(form.getTelefono().trim());
        usuario.setActivo(true);

        return usuarioRepository.save(usuario);
    }

    private Mascota obtenerOCrearMascota(SolicitudCitaForm form, Usuario usuario) {
        String nombreMascota = form.getMascotaNombre().trim();

        return mascotaRepository.findFirstByUsuarioAndNombreIgnoreCaseAndActivoTrue(usuario, nombreMascota)
                .map(mascota -> actualizarMascota(mascota, form))
                .orElseGet(() -> crearMascotaRegistrada(form, usuario, nombreMascota));
    }

    private Mascota actualizarMascota(Mascota mascota, SolicitudCitaForm form) {
        mascota.setTipo(form.getMascotaTipo().trim());
        mascota.setSexo(form.getMascotaSexo());
        mascota.setRaza(form.getMascotaRaza().trim());
        mascota.setFechaNacimiento(form.getMascotaFechaNacimiento());
        mascota.setActivo(true);
        return mascotaRepository.save(mascota);
    }

    private Mascota crearMascotaRegistrada(SolicitudCitaForm form, Usuario usuario, String nombreMascota) {
        EstadoMascota estadoRegistrada = estadoMascotaRepository.findByNombre(ESTADO_MASCOTA_REGISTRADA)
                .orElseThrow(() -> new IllegalStateException("No existe el estado de mascota REGISTRADA."));
        PrioridadMascota prioridadNormal = prioridadMascotaRepository.findByNombre(PRIORIDAD_MASCOTA_NORMAL)
                .orElseThrow(() -> new IllegalStateException("No existe la prioridad de mascota NORMAL."));

        Mascota mascota = new Mascota();
        mascota.setUsuario(usuario);
        mascota.setEstado(estadoRegistrada);
        mascota.setPrioridad(prioridadNormal);
        mascota.setNombre(nombreMascota);
        mascota.setTipo(form.getMascotaTipo().trim());
        mascota.setSexo(form.getMascotaSexo());
        mascota.setRaza(form.getMascotaRaza().trim());
        mascota.setFechaNacimiento(form.getMascotaFechaNacimiento());
        mascota.setActivo(true);

        return mascotaRepository.save(mascota);
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

    private void actualizarSesion(HttpSession session, Usuario usuario) {
        session.setAttribute("usuarioId", usuario.getId());
        session.setAttribute("usuarioNombre", usuario.getNombre());
        session.setAttribute("usuarioRol", usuario.getRol().getNombre());
    }

    private Long obtenerUsuarioIdSesion(HttpSession session) {
        Object usuarioId = session.getAttribute("usuarioId");
        if (usuarioId instanceof Long id) {
            return id;
        }

        return null;
    }

    private String limpiarTexto(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }

        return valor.trim();
    }
}
