package com.ultravet.veterinaria.controller.api;

import com.ultravet.veterinaria.dto.JwtResponse;
import com.ultravet.veterinaria.dto.LoginApiRequest;
import com.ultravet.veterinaria.security.UsuarioDetailsService;
import com.ultravet.veterinaria.security.UsuarioPrincipal;
import com.ultravet.veterinaria.security.jwt.JwtService;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthApiController {

    private final AuthenticationManager authenticationManager;
    private final UsuarioDetailsService usuarioDetailsService;
    private final JwtService jwtService;

    public AuthApiController(AuthenticationManager authenticationManager,
            UsuarioDetailsService usuarioDetailsService,
            JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.usuarioDetailsService = usuarioDetailsService;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginApiRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getIdentificador(), request.getPassword()));
        } catch (AuthenticationException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Correo/DNI o contrasena incorrectos."));
        }

        UsuarioPrincipal principal =
                (UsuarioPrincipal) usuarioDetailsService.loadUserByUsername(request.getIdentificador());

        String token = jwtService.generarToken(principal);
        String rol = principal.getAuthorities().iterator().next().getAuthority();

        JwtResponse body = new JwtResponse(token, "Bearer", jwtService.getExpirationMs(),
                principal.getUsuario().getNombre(), rol);

        return ResponseEntity.ok(body);
    }
}
