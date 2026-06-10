package com.ultravet.veterinaria.controller;

import com.ultravet.veterinaria.dto.LoginForm;
import com.ultravet.veterinaria.dto.RegistroUsuarioForm;
import com.ultravet.veterinaria.model.Rol;
import com.ultravet.veterinaria.model.Usuario;
import com.ultravet.veterinaria.repository.RolRepository;
import com.ultravet.veterinaria.repository.UsuarioRepository;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthController {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;

    public AuthController(
            UsuarioRepository usuarioRepository,
            RolRepository rolRepository) {

        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
    }

    @PostMapping("/registro")
    public String registrar(
            RegistroUsuarioForm form,
            RedirectAttributes redirectAttributes) {

        if (usuarioRepository.findByCorreo(form.getCorreo()).isPresent()) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Ya existe un usuario con ese correo");

            return "redirect:/";
        }

        Rol rolCliente = rolRepository
                .findByNombre("CLIENTE")
                .orElseThrow();

        Usuario usuario = new Usuario();

        usuario.setNombre(form.getNombre());
        usuario.setCorreo(form.getCorreo());
        usuario.setTelefono(form.getTelefono());

        // temporal
        usuario.setPasswordHash(form.getPassword());

        usuario.setRol(rolCliente);

        usuarioRepository.save(usuario);

        redirectAttributes.addFlashAttribute(
                "success",
                "Usuario registrado correctamente");

        return "redirect:/";
    }

    @PostMapping("/login")
    public String login(
        LoginForm form,
        HttpSession session,
        RedirectAttributes redirectAttributes) {

        Usuario usuario = usuarioRepository
            .findByCorreoAndPasswordHash(
                    form.getCorreo(),
                    form.getPassword())
            .orElse(null);

    if (usuario == null) {

        redirectAttributes.addFlashAttribute(
                "error",
                "Correo o contraseña incorrectos");

        return "redirect:/";
    }

    session.setAttribute("usuarioId", usuario.getId());
    session.setAttribute("usuarioNombre", usuario.getNombre());
    session.setAttribute("usuarioRol", usuario.getRol().getNombre());

    return "redirect:/";
}

@GetMapping("/logout")
public String logout(HttpSession session) {

    session.invalidate();

    return "redirect:/";
}


}