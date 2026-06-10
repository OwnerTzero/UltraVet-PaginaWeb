package com.ultravet.veterinaria.controller;

import com.ultravet.veterinaria.model.Atencion;
import com.ultravet.veterinaria.model.AuditableEntity;
import com.ultravet.veterinaria.model.Cita;
import com.ultravet.veterinaria.model.EstadoMascota;
import com.ultravet.veterinaria.model.EstadoSolicitud;
import com.ultravet.veterinaria.model.Mascota;
import com.ultravet.veterinaria.model.Pago;
import com.ultravet.veterinaria.model.Servicio;
import com.ultravet.veterinaria.model.SolicitudAdopcion;
import com.ultravet.veterinaria.model.Usuario;
import com.ultravet.veterinaria.repository.AtencionRepository;
import com.ultravet.veterinaria.repository.CategoriaServicioRepository;
import com.ultravet.veterinaria.repository.CitaRepository;
import com.ultravet.veterinaria.repository.EstadoCitaRepository;
import com.ultravet.veterinaria.repository.EstadoMascotaRepository;
import com.ultravet.veterinaria.repository.EstadoPagoRepository;
import com.ultravet.veterinaria.repository.EstadoSolicitudRepository;
import com.ultravet.veterinaria.repository.MascotaRepository;
import com.ultravet.veterinaria.repository.MetodoPagoRepository;
import com.ultravet.veterinaria.repository.PagoRepository;
import com.ultravet.veterinaria.repository.PrioridadMascotaRepository;
import com.ultravet.veterinaria.repository.RolRepository;
import com.ultravet.veterinaria.repository.ServicioRepository;
import com.ultravet.veterinaria.repository.SolicitudAdopcionRepository;
import com.ultravet.veterinaria.repository.UsuarioRepository;
import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AdminController {

    private static final String ESTADO_SOLICITUD_APROBADA = "APROBADA";
    private static final String ESTADO_MASCOTA_ADOPTADA = "ADOPTADA";
    private static final String ESTADO_CITA_ATENDIDA = "ATENDIDA";

    private final MascotaRepository mascotaRepository;
    private final SolicitudAdopcionRepository solicitudAdopcionRepository;
    private final UsuarioRepository usuarioRepository;
    private final ServicioRepository servicioRepository;
    private final CitaRepository citaRepository;
    private final PagoRepository pagoRepository;
    private final AtencionRepository atencionRepository;
    private final CategoriaServicioRepository categoriaServicioRepository;
    private final EstadoMascotaRepository estadoMascotaRepository;
    private final PrioridadMascotaRepository prioridadMascotaRepository;
    private final EstadoCitaRepository estadoCitaRepository;
    private final EstadoSolicitudRepository estadoSolicitudRepository;
    private final MetodoPagoRepository metodoPagoRepository;
    private final EstadoPagoRepository estadoPagoRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminController(MascotaRepository mascotaRepository,
            SolicitudAdopcionRepository solicitudAdopcionRepository,
            UsuarioRepository usuarioRepository,
            ServicioRepository servicioRepository,
            CitaRepository citaRepository,
            PagoRepository pagoRepository,
            AtencionRepository atencionRepository,
            CategoriaServicioRepository categoriaServicioRepository,
            EstadoMascotaRepository estadoMascotaRepository,
            PrioridadMascotaRepository prioridadMascotaRepository,
            EstadoCitaRepository estadoCitaRepository,
            EstadoSolicitudRepository estadoSolicitudRepository,
            MetodoPagoRepository metodoPagoRepository,
            EstadoPagoRepository estadoPagoRepository,
            RolRepository rolRepository,
            PasswordEncoder passwordEncoder) {
        this.mascotaRepository = mascotaRepository;
        this.solicitudAdopcionRepository = solicitudAdopcionRepository;
        this.usuarioRepository = usuarioRepository;
        this.servicioRepository = servicioRepository;
        this.citaRepository = citaRepository;
        this.pagoRepository = pagoRepository;
        this.atencionRepository = atencionRepository;
        this.categoriaServicioRepository = categoriaServicioRepository;
        this.estadoMascotaRepository = estadoMascotaRepository;
        this.prioridadMascotaRepository = prioridadMascotaRepository;
        this.estadoCitaRepository = estadoCitaRepository;
        this.estadoSolicitudRepository = estadoSolicitudRepository;
        this.metodoPagoRepository = metodoPagoRepository;
        this.estadoPagoRepository = estadoPagoRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/admin")
    public String dashboard(Model model) {
        cargarDashboard(model);
        return "admin/dashboard";
    }

    @PostMapping("/admin/mascotas/guardar")
    @Transactional
    public String guardarMascota(@RequestParam(required = false) Long id,
            @RequestParam(required = false) Long usuarioId,
            @RequestParam Long estadoId,
            @RequestParam Long prioridadId,
            @RequestParam String nombre,
            @RequestParam(required = false) String sexo,
            @RequestParam(required = false) String raza,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaNacimiento,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) String descripcion,
            @RequestParam(required = false) String ubicacion,
            @RequestParam(required = false) String urlImagen,
            RedirectAttributes redirectAttributes) {

        return ejecutarCrud("mascotas", "Mascota guardada correctamente.", redirectAttributes, () -> {
            Mascota mascota = id == null ? new Mascota() : requerir(mascotaRepository, id, "mascota");
            mascota.setUsuario(usuarioId == null ? null : requerir(usuarioRepository, usuarioId, "usuario"));
            mascota.setEstado(requerir(estadoMascotaRepository, estadoId, "estado de mascota"));
            mascota.setPrioridad(requerir(prioridadMascotaRepository, prioridadId, "prioridad"));
            mascota.setNombre(requerirTexto(nombre, "nombre de mascota"));
            mascota.setSexo(limpiar(sexo));
            mascota.setRaza(limpiar(raza));
            mascota.setFechaNacimiento(fechaNacimiento);
            mascota.setTipo(limpiar(tipo));
            mascota.setDescripcion(limpiar(descripcion));
            mascota.setUbicacion(limpiar(ubicacion));
            mascota.setUrlImagen(limpiar(urlImagen));
            mascota.setActivo(true);
            mascotaRepository.save(mascota);
        });
    }

    @PostMapping("/admin/mascotas/{id}/eliminar")
    @Transactional
    public String eliminarMascota(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        return desactivar("mascotas", "Mascota eliminada del listado.", redirectAttributes, mascotaRepository, id);
    }

    @PostMapping("/admin/servicios/guardar")
    @Transactional
    public String guardarServicio(@RequestParam(required = false) Long id,
            @RequestParam Long categoriaId,
            @RequestParam String nombre,
            @RequestParam(required = false) String descripcion,
            @RequestParam(required = false) String duracionEstimada,
            @RequestParam BigDecimal precio,
            RedirectAttributes redirectAttributes) {

        return ejecutarCrud("servicios-admin", "Servicio guardado correctamente.", redirectAttributes, () -> {
            Servicio servicio = id == null ? new Servicio() : requerir(servicioRepository, id, "servicio");
            servicio.setCategoria(requerir(categoriaServicioRepository, categoriaId, "categoria"));
            servicio.setNombre(requerirTexto(nombre, "nombre del servicio"));
            servicio.setDescripcion(limpiar(descripcion));
            servicio.setDuracionEstimada(limpiar(duracionEstimada));
            servicio.setPrecio(precio);
            servicio.setActivo(true);
            servicioRepository.save(servicio);
        });
    }

    @PostMapping("/admin/servicios/{id}/eliminar")
    @Transactional
    public String eliminarServicio(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        return desactivar("servicios-admin", "Servicio desactivado correctamente.", redirectAttributes,
                servicioRepository, id);
    }

    @PostMapping("/admin/usuarios/guardar")
    @Transactional
    public String guardarUsuario(@RequestParam(required = false) Long id,
            @RequestParam Long rolId,
            @RequestParam String nombre,
            @RequestParam String correo,
            @RequestParam(required = false) String dni,
            @RequestParam(required = false) String sexo,
            @RequestParam(required = false) String telefono,
            @RequestParam(required = false) String password,
            RedirectAttributes redirectAttributes) {

        return ejecutarCrud("clientes", "Usuario guardado correctamente.", redirectAttributes, () -> {
            String correoNormalizado = requerirTexto(correo, "correo").toLowerCase();
            validarCorreoUnico(correoNormalizado, id);
            validarDniUnico(limpiar(dni), id);

            Usuario usuario = id == null ? new Usuario() : requerir(usuarioRepository, id, "usuario");
            usuario.setRol(requerir(rolRepository, rolId, "rol"));
            usuario.setNombre(requerirTexto(nombre, "nombre"));
            usuario.setCorreo(correoNormalizado);
            usuario.setDni(limpiar(dni));
            usuario.setSexo(limpiar(sexo));
            usuario.setTelefono(limpiar(telefono));
            usuario.setActivo(true);

            if (password != null && !password.isBlank()) {
                usuario.setPasswordHash(passwordEncoder.encode(password));
            }

            usuarioRepository.save(usuario);
        });
    }

    @PostMapping("/admin/usuarios/{id}/eliminar")
    @Transactional
    public String eliminarUsuario(@PathVariable Long id,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Object usuarioIdSesion = session.getAttribute("usuarioId");
        if (usuarioIdSesion instanceof Long actual && actual.equals(id)) {
            redirectAttributes.addFlashAttribute("adminError", "No puedes desactivar tu propio usuario en sesion.");
            return redirigir("clientes");
        }

        return desactivar("clientes", "Usuario desactivado correctamente.", redirectAttributes, usuarioRepository, id);
    }

    @PostMapping("/admin/citas/guardar")
    @Transactional
    public String guardarCita(@RequestParam(required = false) Long id,
            @RequestParam Long usuarioId,
            @RequestParam Long mascotaId,
            @RequestParam Long servicioId,
            @RequestParam Long estadoId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime hora,
            @RequestParam(required = false) String comentario,
            RedirectAttributes redirectAttributes) {

        return ejecutarCrud("citas", "Cita guardada correctamente.", redirectAttributes, () -> {
            Cita cita = id == null ? new Cita() : requerir(citaRepository, id, "cita");
            cita.setUsuario(requerir(usuarioRepository, usuarioId, "usuario"));
            cita.setMascota(requerir(mascotaRepository, mascotaId, "mascota"));
            cita.setServicio(requerir(servicioRepository, servicioId, "servicio"));
            cita.setEstado(requerir(estadoCitaRepository, estadoId, "estado de cita"));
            cita.setFecha(fecha);
            cita.setHora(hora);
            cita.setComentario(limpiar(comentario));
            cita.setActivo(true);
            citaRepository.save(cita);
        });
    }

    @PostMapping("/admin/citas/{id}/eliminar")
    @Transactional
    public String eliminarCita(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        return desactivar("citas", "Cita eliminada del listado.", redirectAttributes, citaRepository, id);
    }

    @PostMapping("/admin/solicitudes/guardar")
    @Transactional
    public String guardarSolicitud(@RequestParam(required = false) Long id,
            @RequestParam Long usuarioId,
            @RequestParam Long mascotaId,
            @RequestParam Long estadoId,
            @RequestParam(required = false) String distrito,
            @RequestParam(required = false) String experienciaMascotas,
            RedirectAttributes redirectAttributes) {

        return ejecutarCrud("adopciones", "Solicitud guardada correctamente.", redirectAttributes, () -> {
            SolicitudAdopcion solicitud = id == null ? new SolicitudAdopcion()
                    : requerir(solicitudAdopcionRepository, id, "solicitud");
            solicitud.setUsuario(requerir(usuarioRepository, usuarioId, "usuario"));
            solicitud.setMascota(requerir(mascotaRepository, mascotaId, "mascota"));
            solicitud.setEstado(requerir(estadoSolicitudRepository, estadoId, "estado de solicitud"));
            solicitud.setDistrito(limpiar(distrito));
            solicitud.setExperienciaMascotas(limpiar(experienciaMascotas));
            solicitud.setActivo(true);

            if (solicitud.getFechaEnvio() == null) {
                solicitud.setFechaEnvio(LocalDateTime.now());
            }

            sincronizarMascotaAdoptada(solicitud);
            solicitudAdopcionRepository.save(solicitud);
        });
    }

    @PostMapping("/admin/solicitudes/{id}/eliminar")
    @Transactional
    public String eliminarSolicitud(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        return desactivar("adopciones", "Solicitud eliminada del listado.", redirectAttributes,
                solicitudAdopcionRepository, id);
    }

    @PostMapping("/admin/pagos/guardar")
    @Transactional
    public String guardarPago(@RequestParam(required = false) Long id,
            @RequestParam Long citaId,
            @RequestParam Long metodoPagoId,
            @RequestParam Long estadoPagoId,
            @RequestParam BigDecimal monto,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaPago,
            RedirectAttributes redirectAttributes) {

        return ejecutarCrud("pagos", "Pago guardado correctamente.", redirectAttributes, () -> {
            Pago pago = id == null ? new Pago() : requerir(pagoRepository, id, "pago");
            pago.setCita(requerir(citaRepository, citaId, "cita"));
            pago.setMetodoPago(requerir(metodoPagoRepository, metodoPagoId, "metodo de pago"));
            pago.setEstadoPago(requerir(estadoPagoRepository, estadoPagoId, "estado de pago"));
            pago.setMonto(monto);
            pago.setFechaPago(fechaPago);
            pago.setActivo(true);
            pagoRepository.save(pago);
        });
    }

    @PostMapping("/admin/pagos/{id}/eliminar")
    @Transactional
    public String eliminarPago(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        return desactivar("pagos", "Pago eliminado del listado.", redirectAttributes, pagoRepository, id);
    }

    @PostMapping("/admin/atenciones/guardar")
    @Transactional
    public String guardarAtencion(@RequestParam(required = false) Long id,
            @RequestParam Long citaId,
            @RequestParam(required = false) String diagnostico,
            @RequestParam(required = false) String tratamiento,
            @RequestParam(required = false) String observaciones,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaRegistro,
            RedirectAttributes redirectAttributes) {

        return ejecutarCrud("atenciones", "Atencion guardada correctamente.", redirectAttributes, () -> {
            validarCitaSinAtencionDuplicada(citaId, id);

            Atencion atencion = id == null ? new Atencion() : requerir(atencionRepository, id, "atencion");
            Cita cita = requerir(citaRepository, citaId, "cita");
            atencion.setCita(cita);
            atencion.setDiagnostico(limpiar(diagnostico));
            atencion.setTratamiento(limpiar(tratamiento));
            atencion.setObservaciones(limpiar(observaciones));
            atencion.setFechaRegistro(fechaRegistro == null ? LocalDateTime.now() : fechaRegistro);
            atencion.setActivo(true);

            estadoCitaRepository.findByNombre(ESTADO_CITA_ATENDIDA).ifPresent(cita::setEstado);
            atencionRepository.save(atencion);
        });
    }

    @PostMapping("/admin/atenciones/{id}/eliminar")
    @Transactional
    public String eliminarAtencion(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        return desactivar("atenciones", "Atencion eliminada del listado.", redirectAttributes, atencionRepository, id);
    }

    private void cargarDashboard(Model model) {
        List<Mascota> mascotas = mascotaRepository.findByActivoTrueOrderByIdAsc();
        List<SolicitudAdopcion> solicitudes = solicitudAdopcionRepository.findByActivoTrueOrderByFechaEnvioDesc();
        List<Usuario> usuarios = usuarioRepository.findByActivoTrueOrderByIdAsc();
        List<Servicio> servicios = servicioRepository.findByActivoTrueOrderByIdAsc();
        List<Cita> citas = citaRepository.findByActivoTrueOrderByFechaDescHoraDesc();
        List<Pago> pagos = pagoRepository.findByActivoTrueOrderByFechaPagoDescIdDesc();
        List<Atencion> atenciones = atencionRepository.findByActivoTrueOrderByFechaRegistroDesc();

        long totalClientes = usuarios.stream()
                .filter(usuario -> usuario.getRol() != null && "CLIENTE".equals(usuario.getRol().getNombre()))
                .count();
        long citasPendientes = citas.stream()
                .filter(cita -> cita.getEstado() != null && "PENDIENTE".equals(cita.getEstado().getNombre()))
                .count();
        long solicitudesPendientes = solicitudes.stream()
                .filter(solicitud -> solicitud.getEstado() != null
                        && "PENDIENTE".equals(solicitud.getEstado().getNombre()))
                .count();
        BigDecimal totalPagado = pagoRepository.sumMontoActivo();

        model.addAttribute("mascotas", mascotas);
        model.addAttribute("solicitudes", solicitudes);
        model.addAttribute("usuarios", usuarios);
        model.addAttribute("servicios", servicios);
        model.addAttribute("citas", citas);
        model.addAttribute("pagos", pagos);
        model.addAttribute("atenciones", atenciones);

        model.addAttribute("ultimasCitas", limitar(citas, 5));
        model.addAttribute("ultimasSolicitudes", limitar(solicitudes, 5));
        model.addAttribute("ultimosPagos", limitar(pagos, 5));

        model.addAttribute("totalMascotas", mascotas.size());
        model.addAttribute("totalSolicitudes", solicitudes.size());
        model.addAttribute("totalUsuarios", usuarios.size());
        model.addAttribute("totalClientes", totalClientes);
        model.addAttribute("totalServicios", servicios.size());
        model.addAttribute("totalCitas", citas.size());
        model.addAttribute("totalPagos", pagos.size());
        model.addAttribute("totalAtenciones", atenciones.size());
        model.addAttribute("citasPendientes", citasPendientes);
        model.addAttribute("solicitudesPendientes", solicitudesPendientes);
        model.addAttribute("totalPagado", totalPagado);

        model.addAttribute("roles", listar(rolRepository));
        model.addAttribute("categoriasServicio", listar(categoriaServicioRepository));
        model.addAttribute("estadosMascota", listar(estadoMascotaRepository));
        model.addAttribute("prioridadesMascota", listar(prioridadMascotaRepository));
        model.addAttribute("estadosCita", listar(estadoCitaRepository));
        model.addAttribute("estadosSolicitud", listar(estadoSolicitudRepository));
        model.addAttribute("metodosPago", listar(metodoPagoRepository));
        model.addAttribute("estadosPago", listar(estadoPagoRepository));
    }

    private void sincronizarMascotaAdoptada(SolicitudAdopcion solicitud) {
        EstadoSolicitud estado = solicitud.getEstado();
        if (estado == null || !ESTADO_SOLICITUD_APROBADA.equals(estado.getNombre())) {
            return;
        }

        Mascota mascota = solicitud.getMascota();
        mascota.setUsuario(solicitud.getUsuario());
        estadoMascotaRepository.findByNombre(ESTADO_MASCOTA_ADOPTADA).ifPresent(mascota::setEstado);
        mascotaRepository.save(mascota);
    }

    private void validarCorreoUnico(String correo, Long idActual) {
        Optional<Usuario> usuario = usuarioRepository.findByCorreoIgnoreCase(correo);
        if (usuario.isPresent() && idsDiferentes(usuario.get().getId(), idActual)) {
            throw new IllegalArgumentException("Ya existe un usuario con ese correo.");
        }
    }

    private void validarDniUnico(String dni, Long idActual) {
        if (dni == null || dni.isBlank()) {
            return;
        }

        Optional<Usuario> usuario = usuarioRepository.findByDni(dni);
        if (usuario.isPresent() && idsDiferentes(usuario.get().getId(), idActual)) {
            throw new IllegalArgumentException("Ya existe un usuario con ese DNI.");
        }
    }

    private void validarCitaSinAtencionDuplicada(Long citaId, Long atencionId) {
        Optional<Atencion> atencion = atencionRepository.findByCitaIdAndActivoTrue(citaId);
        if (atencion.isPresent() && idsDiferentes(atencion.get().getId(), atencionId)) {
            throw new IllegalArgumentException("La cita seleccionada ya tiene una atencion registrada.");
        }
    }

    private <T> T requerir(JpaRepository<T, Long> repository, Long id, String nombre) {
        if (id == null) {
            throw new IllegalArgumentException("Selecciona " + nombre + ".");
        }

        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No se encontro " + nombre + "."));
    }

    private <T extends AuditableEntity> String desactivar(String anchor,
            String mensaje,
            RedirectAttributes redirectAttributes,
            JpaRepository<T, Long> repository,
            Long id) {

        return ejecutarCrud(anchor, mensaje, redirectAttributes, () -> {
            T entidad = requerir(repository, id, "registro");
            entidad.setActivo(false);
            repository.save(entidad);
        });
    }

    private String ejecutarCrud(String anchor,
            String mensaje,
            RedirectAttributes redirectAttributes,
            Runnable accion) {

        try {
            accion.run();
            redirectAttributes.addFlashAttribute("adminSuccess", mensaje);
        } catch (DataIntegrityViolationException ex) {
            redirectAttributes.addFlashAttribute("adminError",
                    "No se pudo guardar el registro porque entra en conflicto con datos existentes.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("adminError", ex.getMessage());
        }

        return redirigir(anchor);
    }

    private String requerirTexto(String valor, String campo) {
        String limpio = limpiar(valor);
        if (limpio == null) {
            throw new IllegalArgumentException("Completa el campo " + campo + ".");
        }

        return limpio;
    }

    private String limpiar(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }

        return valor.trim();
    }

    private boolean idsDiferentes(Long actual, Long esperado) {
        return esperado == null || !Objects.equals(actual, esperado);
    }

    private <T> List<T> listar(JpaRepository<T, Long> repository) {
        return repository.findAll(Sort.by(Sort.Direction.ASC, "id"));
    }

    private <T> List<T> limitar(List<T> datos, int limite) {
        return datos.stream()
                .limit(limite)
                .toList();
    }

    private String redirigir(String anchor) {
        return "redirect:/admin#" + anchor;
    }
}
