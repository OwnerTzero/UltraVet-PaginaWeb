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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AdminController {

    private static final String ESTADO_SOLICITUD_APROBADA = "APROBADA";
    private static final String ESTADO_MASCOTA_ADOPTADA   = "ADOPTADA";
    private static final String ESTADO_CITA_ATENDIDA      = "ATENDIDA";
    private static final String ESTADO_CITA_PENDIENTE     = "PENDIENTE";
    private static final String ESTADO_SOLICITUD_PENDIENTE = "PENDIENTE";
    private static final String ROL_CLIENTE = "CLIENTE";
    private static final int TAMANIO_PAGINA_ADMIN = 10;
    private static final int TAMANIO_RESUMEN_ADMIN = 5;
    private static final int TAMANIO_OPCIONES_ADMIN = 100;

    // Estados de mascota que pertenecen al modulo de adopcion (NO incluye REGISTRADA)
    private static final List<String> ESTADOS_ADOPCION = List.of("DISPONIBLE", "EN_PROCESO", "ADOPTADA");

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
    public String dashboard(Model model,
            @RequestParam(defaultValue = "0") int paginaCitas,
            @RequestParam(defaultValue = "0") int paginaSolicitudes,
            @RequestParam(defaultValue = "0") int paginaMascotas,
            @RequestParam(defaultValue = "0") int paginaServicios,
            @RequestParam(defaultValue = "0") int paginaUsuarios,
            @RequestParam(defaultValue = "0") int paginaPagos,
            @RequestParam(defaultValue = "0") int paginaAtenciones,
            @RequestParam(required = false) String filtroCitas,
            @RequestParam(required = false) Long estadoCitaId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate citaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate citaHasta,
            @RequestParam(required = false) String filtroSolicitudes,
            @RequestParam(required = false) Long estadoSolicitudId,
            @RequestParam(required = false) String filtroMascotas,
            @RequestParam(required = false) Long estadoMascotaId,
            @RequestParam(required = false) Long prioridadMascotaId,
            @RequestParam(required = false) String filtroServicios,
            @RequestParam(required = false) Long categoriaServicioId,
            @RequestParam(required = false) String filtroUsuarios,
            @RequestParam(required = false) Long rolId,
            @RequestParam(required = false) String filtroPagos,
            @RequestParam(required = false) Long metodoPagoId,
            @RequestParam(required = false) Long estadoPagoId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate pagoDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate pagoHasta,
            @RequestParam(required = false) String filtroAtenciones,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate atencionDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate atencionHasta) {
        AdminFiltros filtros = new AdminFiltros(filtroCitas, estadoCitaId, citaDesde, citaHasta,
                filtroSolicitudes, estadoSolicitudId, filtroMascotas, estadoMascotaId, prioridadMascotaId,
                filtroServicios, categoriaServicioId, filtroUsuarios, rolId, filtroPagos, metodoPagoId,
                estadoPagoId, pagoDesde, pagoHasta, filtroAtenciones, atencionDesde, atencionHasta);

        cargarDashboard(model, paginaCitas, paginaSolicitudes, paginaMascotas,
                paginaServicios, paginaUsuarios, paginaPagos, paginaAtenciones, filtros);
        return "admin/dashboard";
    }

    @PostMapping("/admin/mascotas/guardar")
    @Transactional
    public Object guardarMascota(@RequestParam(required = false) Long id,
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
    public Object eliminarMascota(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        return desactivar("mascotas", "Mascota eliminada del listado.", redirectAttributes, mascotaRepository, id);
    }

    @PostMapping("/admin/servicios/guardar")
    @Transactional
    public Object guardarServicio(@RequestParam(required = false) Long id,
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
    public Object eliminarServicio(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        return desactivar("servicios-admin", "Servicio desactivado correctamente.", redirectAttributes,
                servicioRepository, id);
    }

    @PostMapping("/admin/usuarios/guardar")
    @Transactional
    public Object guardarUsuario(@RequestParam(required = false) Long id,
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
    public Object eliminarUsuario(@PathVariable Long id,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Object usuarioIdSesion = session.getAttribute("usuarioId");
        if (usuarioIdSesion instanceof Long actual && actual.equals(id)) {
            return responderCrud("clientes", false, "No puedes desactivar tu propio usuario en sesion.",
                    redirectAttributes);
        }

        return desactivar("clientes", "Usuario desactivado correctamente.", redirectAttributes, usuarioRepository, id);
    }

    @PostMapping("/admin/citas/guardar")
    @Transactional
    public Object guardarCita(@RequestParam(required = false) Long id,
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
    public Object eliminarCita(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        return desactivar("citas", "Cita eliminada del listado.", redirectAttributes, citaRepository, id);
    }

    @PostMapping("/admin/solicitudes/guardar")
    @Transactional
    public Object guardarSolicitud(@RequestParam(required = false) Long id,
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
    public Object eliminarSolicitud(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        return desactivar("adopciones", "Solicitud eliminada del listado.", redirectAttributes,
                solicitudAdopcionRepository, id);
    }

    @PostMapping("/admin/pagos/guardar")
    @Transactional
    public Object guardarPago(@RequestParam(required = false) Long id,
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
    public Object eliminarPago(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        return desactivar("pagos", "Pago eliminado del listado.", redirectAttributes, pagoRepository, id);
    }

    @PostMapping("/admin/atenciones/guardar")
    @Transactional
    public Object guardarAtencion(@RequestParam(required = false) Long id,
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
    public Object eliminarAtencion(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        return desactivar("atenciones", "Atencion eliminada del listado.", redirectAttributes, atencionRepository, id);
    }

    // ─────────────────────────────────────────────────────────────
    // CARGA DEL DASHBOARD
    // ─────────────────────────────────────────────────────────────

    private void cargarDashboard(Model model,
            int paginaCitas,
            int paginaSolicitudes,
            int paginaMascotas,
            int paginaServicios,
            int paginaUsuarios,
            int paginaPagos,
            int paginaAtenciones,
            AdminFiltros filtros) {
        // Ventanas iniciales para selectores de modales. Evitan cargar miles de opciones al abrir el dashboard.
        List<Mascota> todasMascotas = mascotaRepository.findByActivoTrueOrderByIdAsc(opciones()).getContent();

        Page<Cita> citasPagina = citaRepository.findAll(filtroCitas(filtros),
                pagina(paginaCitas, Sort.by(Sort.Direction.DESC, "fecha").and(Sort.by(Sort.Direction.DESC, "hora"))));
        Page<SolicitudAdopcion> solicitudesPagina = solicitudAdopcionRepository.findAll(filtroSolicitudes(filtros),
                pagina(paginaSolicitudes, Sort.by(Sort.Direction.DESC, "fechaEnvio")));
        Page<Mascota> mascotasAdopcionPagina = mascotaRepository.findAll(filtroMascotas(filtros),
                pagina(paginaMascotas, Sort.by(Sort.Direction.ASC, "id")));
        Page<Servicio> serviciosPagina = servicioRepository.findAll(filtroServicios(filtros),
                pagina(paginaServicios, Sort.by(Sort.Direction.ASC, "id")));
        Page<Usuario> usuariosPagina = usuarioRepository.findAll(filtroUsuarios(filtros),
                pagina(paginaUsuarios, Sort.by(Sort.Direction.ASC, "id")));
        Page<Pago> pagosPagina = pagoRepository.findAll(filtroPagos(filtros),
                pagina(paginaPagos, Sort.by(Sort.Direction.DESC, "fechaPago").and(Sort.by(Sort.Direction.DESC, "id"))));
        Page<Atencion> atencionesPagina = atencionRepository.findAll(filtroAtenciones(filtros),
                pagina(paginaAtenciones, Sort.by(Sort.Direction.DESC, "fechaRegistro")));

        List<Usuario> usuariosOpciones = usuarioRepository.findByActivoTrueOrderByIdAsc(opciones()).getContent();
        List<Servicio> serviciosOpciones = servicioRepository.findByActivoTrueOrderByIdAsc(opciones()).getContent();
        List<Cita> citasOpciones = citaRepository.findByActivoTrueOrderByFechaDescHoraDesc(opciones()).getContent();

        long totalMascotas = mascotaRepository.countByActivoTrueAndEstadoNombreIn(ESTADOS_ADOPCION);
        long totalSolicitudes = solicitudAdopcionRepository.countByActivoTrue();
        long totalUsuarios = usuarioRepository.countByActivoTrue();
        long totalClientes = usuarioRepository.countByActivoTrueAndRolNombre(ROL_CLIENTE);
        long totalServicios = servicioRepository.countByActivoTrue();
        long totalCitas = citaRepository.countByActivoTrue();
        long totalPagos = pagoRepository.countByActivoTrue();
        long totalAtenciones = atencionRepository.countByActivoTrue();
        long citasPendientes = citaRepository.countByActivoTrueAndEstadoNombre(ESTADO_CITA_PENDIENTE);
        long solicitudesPendientes = solicitudAdopcionRepository
                .countByActivoTrueAndEstadoNombre(ESTADO_SOLICITUD_PENDIENTE);
        BigDecimal totalPagado = pagoRepository.sumMontoActivo();

        // Lista de mascotas para la tabla de adopciones (filtrada)
        model.addAttribute("mascotasAdopcion", mascotasAdopcionPagina.getContent());
        model.addAttribute("mascotasPagina", mascotasAdopcionPagina);

        // Lista completa para selectores de modales (citas, pagos, etc.)
        model.addAttribute("mascotas", todasMascotas);

        model.addAttribute("solicitudes", solicitudesPagina.getContent());
        model.addAttribute("solicitudesPagina", solicitudesPagina);
        model.addAttribute("usuarios", usuariosPagina.getContent());
        model.addAttribute("usuariosPagina", usuariosPagina);
        model.addAttribute("servicios", serviciosPagina.getContent());
        model.addAttribute("serviciosPagina", serviciosPagina);
        model.addAttribute("citas", citasPagina.getContent());
        model.addAttribute("citasPagina", citasPagina);
        model.addAttribute("pagos", pagosPagina.getContent());
        model.addAttribute("pagosPagina", pagosPagina);
        model.addAttribute("atenciones", atencionesPagina.getContent());
        model.addAttribute("atencionesPagina", atencionesPagina);

        model.addAttribute("usuariosOpciones", usuariosOpciones);
        model.addAttribute("serviciosOpciones", serviciosOpciones);
        model.addAttribute("citasOpciones", citasOpciones);

        model.addAttribute("ultimasCitas", citaRepository
                .findByActivoTrueOrderByFechaDescHoraDesc(resumen()).getContent());
        model.addAttribute("ultimasSolicitudes", solicitudAdopcionRepository
                .findByActivoTrueOrderByFechaEnvioDesc(resumen()).getContent());
        model.addAttribute("ultimosPagos", pagoRepository
                .findByActivoTrueOrderByFechaPagoDescIdDesc(resumen()).getContent());

        model.addAttribute("totalMascotas", totalMascotas);
        model.addAttribute("totalSolicitudes", totalSolicitudes);
        model.addAttribute("totalUsuarios", totalUsuarios);
        model.addAttribute("totalClientes", totalClientes);
        model.addAttribute("totalServicios", totalServicios);
        model.addAttribute("totalCitas", totalCitas);
        model.addAttribute("totalPagos", totalPagos);
        model.addAttribute("totalAtenciones", totalAtenciones);
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
        model.addAttribute("filtros", filtros);
    }

    // ─────────────────────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────────────────────

    public record AdminFiltros(
            String filtroCitas,
            Long estadoCitaId,
            LocalDate citaDesde,
            LocalDate citaHasta,
            String filtroSolicitudes,
            Long estadoSolicitudId,
            String filtroMascotas,
            Long estadoMascotaId,
            Long prioridadMascotaId,
            String filtroServicios,
            Long categoriaServicioId,
            String filtroUsuarios,
            Long rolId,
            String filtroPagos,
            Long metodoPagoId,
            Long estadoPagoId,
            LocalDate pagoDesde,
            LocalDate pagoHasta,
            String filtroAtenciones,
            LocalDate atencionDesde,
            LocalDate atencionHasta) {
    }

    private Specification<Cita> filtroCitas(AdminFiltros filtros) {
        return Specification.<Cita>where(activo()).and((root, query, cb) -> {
            List<Predicate> condiciones = new ArrayList<>();
            String patron = patronBusqueda(filtros.filtroCitas());

            if (patron != null) {
                var usuario = root.join("usuario", JoinType.LEFT);
                var mascota = root.join("mascota", JoinType.LEFT);
                var servicio = root.join("servicio", JoinType.LEFT);

                condiciones.add(cb.or(
                        like(cb, usuario.get("nombre"), patron),
                        like(cb, usuario.get("correo"), patron),
                        like(cb, mascota.get("nombre"), patron),
                        like(cb, servicio.get("nombre"), patron),
                        like(cb, root.get("comentario"), patron)));
            }

            if (filtros.estadoCitaId() != null) {
                condiciones.add(cb.equal(root.get("estado").get("id"), filtros.estadoCitaId()));
            }

            if (filtros.citaDesde() != null) {
                condiciones.add(cb.greaterThanOrEqualTo(root.get("fecha"), filtros.citaDesde()));
            }

            if (filtros.citaHasta() != null) {
                condiciones.add(cb.lessThanOrEqualTo(root.get("fecha"), filtros.citaHasta()));
            }

            return and(cb, condiciones);
        });
    }

    private Specification<SolicitudAdopcion> filtroSolicitudes(AdminFiltros filtros) {
        return Specification.<SolicitudAdopcion>where(activo()).and((root, query, cb) -> {
            List<Predicate> condiciones = new ArrayList<>();
            String patron = patronBusqueda(filtros.filtroSolicitudes());

            if (patron != null) {
                var usuario = root.join("usuario", JoinType.LEFT);
                var mascota = root.join("mascota", JoinType.LEFT);

                condiciones.add(cb.or(
                        like(cb, usuario.get("nombre"), patron),
                        like(cb, usuario.get("correo"), patron),
                        like(cb, mascota.get("nombre"), patron),
                        like(cb, root.get("distrito"), patron),
                        like(cb, root.get("experienciaMascotas"), patron)));
            }

            if (filtros.estadoSolicitudId() != null) {
                condiciones.add(cb.equal(root.get("estado").get("id"), filtros.estadoSolicitudId()));
            }

            return and(cb, condiciones);
        });
    }

    private Specification<Mascota> filtroMascotas(AdminFiltros filtros) {
        return Specification.<Mascota>where(activo()).and((root, query, cb) -> {
            List<Predicate> condiciones = new ArrayList<>();
            String patron = patronBusqueda(filtros.filtroMascotas());

            condiciones.add(root.get("estado").get("nombre").in(ESTADOS_ADOPCION));

            if (patron != null) {
                var usuario = root.join("usuario", JoinType.LEFT);

                condiciones.add(cb.or(
                        like(cb, root.get("nombre"), patron),
                        like(cb, root.get("tipo"), patron),
                        like(cb, root.get("raza"), patron),
                        like(cb, root.get("ubicacion"), patron),
                        like(cb, usuario.get("nombre"), patron)));
            }

            if (filtros.estadoMascotaId() != null) {
                condiciones.add(cb.equal(root.get("estado").get("id"), filtros.estadoMascotaId()));
            }

            if (filtros.prioridadMascotaId() != null) {
                condiciones.add(cb.equal(root.get("prioridad").get("id"), filtros.prioridadMascotaId()));
            }

            return and(cb, condiciones);
        });
    }

    private Specification<Servicio> filtroServicios(AdminFiltros filtros) {
        return Specification.<Servicio>where(activo()).and((root, query, cb) -> {
            List<Predicate> condiciones = new ArrayList<>();
            String patron = patronBusqueda(filtros.filtroServicios());

            if (patron != null) {
                condiciones.add(cb.or(
                        like(cb, root.get("nombre"), patron),
                        like(cb, root.get("descripcion"), patron),
                        like(cb, root.get("duracionEstimada"), patron)));
            }

            if (filtros.categoriaServicioId() != null) {
                condiciones.add(cb.equal(root.get("categoria").get("id"), filtros.categoriaServicioId()));
            }

            return and(cb, condiciones);
        });
    }

    private Specification<Usuario> filtroUsuarios(AdminFiltros filtros) {
        return Specification.<Usuario>where(activo()).and((root, query, cb) -> {
            List<Predicate> condiciones = new ArrayList<>();
            String patron = patronBusqueda(filtros.filtroUsuarios());

            if (patron != null) {
                condiciones.add(cb.or(
                        like(cb, root.get("nombre"), patron),
                        like(cb, root.get("correo"), patron),
                        like(cb, root.get("dni"), patron),
                        like(cb, root.get("telefono"), patron)));
            }

            if (filtros.rolId() != null) {
                condiciones.add(cb.equal(root.get("rol").get("id"), filtros.rolId()));
            }

            return and(cb, condiciones);
        });
    }

    private Specification<Pago> filtroPagos(AdminFiltros filtros) {
        return Specification.<Pago>where(activo()).and((root, query, cb) -> {
            List<Predicate> condiciones = new ArrayList<>();
            String patron = patronBusqueda(filtros.filtroPagos());

            if (patron != null) {
                var cita = root.join("cita", JoinType.LEFT);
                var usuario = cita.join("usuario", JoinType.LEFT);
                var mascota = cita.join("mascota", JoinType.LEFT);
                var servicio = cita.join("servicio", JoinType.LEFT);

                condiciones.add(cb.or(
                        like(cb, usuario.get("nombre"), patron),
                        like(cb, mascota.get("nombre"), patron),
                        like(cb, servicio.get("nombre"), patron)));
            }

            if (filtros.metodoPagoId() != null) {
                condiciones.add(cb.equal(root.get("metodoPago").get("id"), filtros.metodoPagoId()));
            }

            if (filtros.estadoPagoId() != null) {
                condiciones.add(cb.equal(root.get("estadoPago").get("id"), filtros.estadoPagoId()));
            }

            if (filtros.pagoDesde() != null) {
                condiciones.add(cb.greaterThanOrEqualTo(root.get("fechaPago"), filtros.pagoDesde().atStartOfDay()));
            }

            if (filtros.pagoHasta() != null) {
                condiciones.add(cb.lessThan(root.get("fechaPago"), filtros.pagoHasta().plusDays(1).atStartOfDay()));
            }

            return and(cb, condiciones);
        });
    }

    private Specification<Atencion> filtroAtenciones(AdminFiltros filtros) {
        return Specification.<Atencion>where(activo()).and((root, query, cb) -> {
            List<Predicate> condiciones = new ArrayList<>();
            String patron = patronBusqueda(filtros.filtroAtenciones());

            if (patron != null) {
                var cita = root.join("cita", JoinType.LEFT);
                var usuario = cita.join("usuario", JoinType.LEFT);
                var mascota = cita.join("mascota", JoinType.LEFT);
                var servicio = cita.join("servicio", JoinType.LEFT);

                condiciones.add(cb.or(
                        like(cb, mascota.get("nombre"), patron),
                        like(cb, usuario.get("nombre"), patron),
                        like(cb, servicio.get("nombre"), patron),
                        like(cb, root.get("diagnostico"), patron),
                        like(cb, root.get("tratamiento"), patron),
                        like(cb, root.get("observaciones"), patron)));
            }

            if (filtros.atencionDesde() != null) {
                condiciones.add(cb.greaterThanOrEqualTo(root.get("fechaRegistro"),
                        filtros.atencionDesde().atStartOfDay()));
            }

            if (filtros.atencionHasta() != null) {
                condiciones.add(cb.lessThan(root.get("fechaRegistro"),
                        filtros.atencionHasta().plusDays(1).atStartOfDay()));
            }

            return and(cb, condiciones);
        });
    }

    private <T extends AuditableEntity> Specification<T> activo() {
        return (root, query, cb) -> cb.isTrue(root.get("activo"));
    }

    private Predicate and(CriteriaBuilder cb, List<Predicate> condiciones) {
        return condiciones.isEmpty() ? cb.conjunction() : cb.and(condiciones.toArray(Predicate[]::new));
    }

    private Predicate like(CriteriaBuilder cb, Expression<String> campo, String patron) {
        return cb.like(cb.lower(campo), patron);
    }

    private String patronBusqueda(String valor) {
        String limpio = limpiar(valor);
        return limpio == null ? null : "%" + limpio.toLowerCase(Locale.ROOT) + "%";
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

    private <T extends AuditableEntity> Object desactivar(String anchor,
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

    private Object ejecutarCrud(String anchor,
            String mensaje,
            RedirectAttributes redirectAttributes,
            Runnable accion) {

        try {
            accion.run();
            return responderCrud(anchor, true, mensaje, redirectAttributes);
        } catch (DataIntegrityViolationException ex) {
            return responderCrud(anchor, false,
                    "No se pudo guardar el registro porque entra en conflicto con datos existentes.",
                    redirectAttributes);
        } catch (RuntimeException ex) {
            return responderCrud(anchor, false, ex.getMessage(), redirectAttributes);
        }
    }

    private Object responderCrud(String anchor, boolean exito, String mensaje, RedirectAttributes redirectAttributes) {
        if (esSolicitudAjax()) {
            HttpStatus estado = exito ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(estado)
                    .body(Map.of("ok", exito, "message", mensaje, "anchor", anchor));
        }

        redirectAttributes.addFlashAttribute(exito ? "adminSuccess" : "adminError", mensaje);
        return redirigir(anchor);
    }

    private boolean esSolicitudAjax() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes atributos) {
            return "true".equalsIgnoreCase(atributos.getRequest().getHeader("X-Admin-Ajax"));
        }

        return false;
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

    private Pageable pagina(int numeroPagina) {
        return PageRequest.of(Math.max(numeroPagina, 0), TAMANIO_PAGINA_ADMIN);
    }

    private Pageable pagina(int numeroPagina, Sort sort) {
        return PageRequest.of(Math.max(numeroPagina, 0), TAMANIO_PAGINA_ADMIN, sort);
    }

    private Pageable resumen() {
        return PageRequest.of(0, TAMANIO_RESUMEN_ADMIN);
    }

    private Pageable opciones() {
        return PageRequest.of(0, TAMANIO_OPCIONES_ADMIN);
    }

    private String redirigir(String anchor) {
        return "redirect:/admin#" + anchor;
    }
}
