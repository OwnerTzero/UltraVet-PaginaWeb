package com.ultravet.veterinaria.security;

import com.ultravet.veterinaria.model.Usuario;
import com.ultravet.veterinaria.repository.UsuarioRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String identificador) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByCorreoIgnoreCase(identificador)
                .or(() -> usuarioRepository.findByDni(identificador))
                .orElseThrow(() -> new UsernameNotFoundException(
                        "No existe un usuario con ese correo o DNI."));

        if (usuario.getPasswordHash() == null || usuario.getPasswordHash().isBlank()) {
            throw new UsernameNotFoundException("El usuario aun no tiene contrasena configurada.");
        }

        if (usuario.getActivo() != null && !Boolean.TRUE.equals(usuario.getActivo())) {
            throw new UsernameNotFoundException("El usuario esta inactivo.");
        }

        return new UsuarioPrincipal(usuario);
    }
}
