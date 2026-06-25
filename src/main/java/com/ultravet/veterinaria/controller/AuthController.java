package com.ultravet.veterinaria.controller;

import com.ultravet.veterinaria.dto.LoginForm;
import com.ultravet.veterinaria.dto.RegistroUsuarioForm;
import com.ultravet.veterinaria.model.Rol;
import com.ultravet.veterinaria.model.Usuario;
import com.ultravet.veterinaria.repository.RolRepository;
import com.ultravet.veterinaria.repository.UsuarioRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private static final String ACCESO_ADMIN = "ADMIN";
    private static final String ACCESO_CLIENTE = "CLIENTE";
    private static final String ROL_ADMIN = "ADMIN";
    private static final String ROL_CLIENTE = "CLIENTE";

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UsuarioRepository usuarioRepository,
            RolRepository rolRepository,
            PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/registro")
    @Transactional
    public String registrar(
            @Valid @ModelAttribute("registroUsuarioForm") RegistroUsuarioForm form,
            BindingResult result,
            HttpServletRequest request,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        normalizarRegistro(form);
        validarRegistro(form, result);

        if (result.hasErrors()) {
            return redireccionConErrores(request, redirectAttributes, "registroUsuarioForm", form, result,
                    "registro", "Revisa los datos del registro.");
        }

        Usuario usuario = usuarioRepository.findByCorreoIgnoreCase(form.getCorreo())
                .orElseGet(Usuario::new);

        if (usuario.getRol() == null) {
            Rol rolCliente = rolRepository.findByNombre(ROL_CLIENTE)
                    .orElseThrow(() -> new IllegalStateException("No existe el rol CLIENTE."));
            usuario.setRol(rolCliente);
        }

        usuario.setNombre(form.getNombre());
        usuario.setDni(form.getDni());
        usuario.setCorreo(form.getCorreo());
        usuario.setTelefono(form.getTelefono());
        usuario.setPasswordHash(passwordEncoder.encode(form.getPassword()));
        usuario.setActivo(true);

        usuarioRepository.save(usuario);
        actualizarSesion(session, usuario);

        redirectAttributes.addFlashAttribute("authSuccess", "Cuenta registrada correctamente.");
        return redireccionAnterior(request);
    }

    @PostMapping("/login")
    @Transactional
    public String login(
            @Valid @ModelAttribute("loginForm") LoginForm form,
            BindingResult result,
            HttpServletRequest request,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        normalizarLogin(form);
        validarIdentificadorLogin(form, result);

        if (result.hasErrors()) {
            return redireccionConErrores(request, redirectAttributes, "loginForm", form, result,
                    "login", "Revisa tus datos de acceso.");
        }

        Optional<Usuario> usuarioEncontrado = esAccesoAdmin(form)
                ? usuarioRepository.findByDni(form.getDni())
                : usuarioRepository.findByCorreoIgnoreCase(form.getCorreo());

        if (usuarioEncontrado.isEmpty() || !estaActivo(usuarioEncontrado.get())) {
            result.reject("login.invalido", "Usuario o contrasena incorrectos.");
            return redireccionConErrores(request, redirectAttributes, "loginForm", form, result,
                    "login", "Usuario o contrasena incorrectos.");
        }

        Usuario usuario = usuarioEncontrado.get();
        if (esAccesoAdmin(form) && !tieneRol(usuario, ROL_ADMIN)) {
            result.reject("login.invalido", "Usuario o contrasena incorrectos.");
            return redireccionConErrores(request, redirectAttributes, "loginForm", form, result,
                    "login", "Usuario o contrasena incorrectos.");
        }

        if (!tienePassword(usuario)) {
            result.reject("login.sinPassword",
                    "Esta cuenta aun no tiene contrasena. Registrate con el mismo correo para activarla.");
            return redireccionConErrores(request, redirectAttributes, "loginForm", form, result,
                    "login", "Esta cuenta aun no tiene contrasena.");
        }

        if (!passwordCoincideYActualizaSiHaceFalta(usuario, form.getPassword())) {
            result.reject("login.invalido", "Usuario o contrasena incorrectos.");
            return redireccionConErrores(request, redirectAttributes, "loginForm", form, result,
                    "login", "Usuario o contrasena incorrectos.");
        }

        actualizarSesion(session, usuario);

        if (tieneRol(usuario, ROL_ADMIN)) {
            return "redirect:/admin";
        }

        return redireccionAnterior(request);
    }

    private void validarRegistro(RegistroUsuarioForm form, BindingResult result) {
        if (!Objects.equals(form.getPassword(), form.getConfirmPassword())) {
            result.rejectValue("confirmPassword", "password.noCoincide", "Las contrasenas no coinciden.");
        }

        Optional<Usuario> usuarioCorreo = Optional.empty();
        if (!result.hasFieldErrors("correo")) {
            usuarioCorreo = usuarioRepository.findByCorreoIgnoreCase(form.getCorreo());
            if (usuarioCorreo.isPresent() && tienePassword(usuarioCorreo.get())) {
                result.rejectValue("correo", "correo.duplicado", "Ya existe una cuenta con ese correo.");
            }
        }

        if (!result.hasFieldErrors("dni")) {
            Optional<Usuario> usuarioDni = usuarioRepository.findByDni(form.getDni());
            if (usuarioDni.isPresent() && idsDiferentes(usuarioDni.get(), usuarioCorreo.orElse(null))) {
                result.rejectValue("dni", "dni.duplicado", "Ya existe una cuenta con ese DNI.");
            }
        }
    }

    private void validarIdentificadorLogin(LoginForm form, BindingResult result) {
        if (esAccesoAdmin(form)) {
            if (form.getDni().isBlank()) {
                result.rejectValue("dni", "dni.requerido", "Ingresa tu DNI.");
            }
            return;
        }

        if (form.getCorreo().isBlank()) {
            result.rejectValue("correo", "correo.requerido", "Ingresa tu correo.");
        }
    }

    private boolean passwordCoincideYActualizaSiHaceFalta(Usuario usuario, String password) {
        String passwordHash = usuario.getPasswordHash();

        if (esHashBCrypt(passwordHash)) {
            return passwordEncoder.matches(password, passwordHash);
        }

        if (passwordHash.equals(password)) {
            usuario.setPasswordHash(passwordEncoder.encode(password));
            usuarioRepository.save(usuario);
            return true;
        }

        return false;
    }

    private boolean esHashBCrypt(String valor) {
        return valor != null
                && (valor.startsWith("$2a$") || valor.startsWith("$2b$") || valor.startsWith("$2y$"));
    }

    private boolean tienePassword(Usuario usuario) {
        return usuario.getPasswordHash() != null && !usuario.getPasswordHash().isBlank();
    }

    private boolean tieneRol(Usuario usuario, String rol) {
        return usuario.getRol() != null && rol.equalsIgnoreCase(usuario.getRol().getNombre());
    }

    private boolean estaActivo(Usuario usuario) {
        return usuario.getActivo() == null || Boolean.TRUE.equals(usuario.getActivo());
    }

    private boolean esAccesoAdmin(LoginForm form) {
        return ACCESO_ADMIN.equalsIgnoreCase(form.getTipoAcceso());
    }

    private boolean idsDiferentes(Usuario primero, Usuario segundo) {
        if (primero == null || segundo == null) {
            return primero != segundo;
        }

        return !Objects.equals(primero.getId(), segundo.getId());
    }

    private void normalizarRegistro(RegistroUsuarioForm form) {
        form.setCorreo(normalizarCorreo(form.getCorreo()));
    }

    private void normalizarLogin(LoginForm form) {
        form.setCorreo(normalizarCorreo(form.getCorreo()));
        form.setTipoAcceso(form.getTipoAcceso().toUpperCase(Locale.ROOT));

        if (form.getTipoAcceso().isBlank()) {
            form.setTipoAcceso(ACCESO_CLIENTE);
        }
    }

    private String normalizarCorreo(String correo) {
        return correo == null ? "" : correo.trim().toLowerCase(Locale.ROOT);
    }

    private void actualizarSesion(HttpSession session, Usuario usuario) {
        session.setAttribute("usuarioId", usuario.getId());
        session.setAttribute("usuarioNombre", usuario.getNombre());
        session.setAttribute("usuarioRol", usuario.getRol().getNombre());
    }

    private String redireccionConErrores(HttpServletRequest request,
            RedirectAttributes redirectAttributes,
            String formName,
            Object form,
            BindingResult result,
            String authMode,
            String mensaje) {

        redirectAttributes.addFlashAttribute(formName, form);
        redirectAttributes.addFlashAttribute(BindingResult.MODEL_KEY_PREFIX + formName, result);
        redirectAttributes.addFlashAttribute("abrirAuthModal", true);
        redirectAttributes.addFlashAttribute("authMode", authMode);
        redirectAttributes.addFlashAttribute("authError", mensaje);

        return redireccionAnterior(request);
    }

    private String redireccionAnterior(HttpServletRequest request) {
        String referer = request.getHeader("Referer");
        if (referer == null || referer.isBlank()) {
            return "redirect:/";
        }

        try {
            URI uri = URI.create(referer);
            String path = uri.getPath();

            if (path == null || path.isBlank()) {
                return "redirect:/";
            }

            String contextPath = request.getContextPath();
            if (contextPath != null && !contextPath.isBlank() && path.startsWith(contextPath)) {
                path = path.substring(contextPath.length());
            }

            if (!path.startsWith("/")) {
                path = "/" + path;
            }

            String query = uri.getQuery();
            return "redirect:" + path + (query == null ? "" : "?" + query);
        } catch (IllegalArgumentException ex) {
            return "redirect:/";
        }
    }
}
