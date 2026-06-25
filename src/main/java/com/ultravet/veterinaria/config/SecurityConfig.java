package com.ultravet.veterinaria.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            //1. RUTAS
            .authorizeHttpRequests(auth -> auth

                .requestMatchers("/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll()
                .requestMatchers("/", "/servicios", "/adopcion").permitAll()
                .requestMatchers("/login", "/registro", "/citas", "/citas/**", "/adoptar").permitAll()
                .requestMatchers("/admin/**", "/admin").authenticated()
                .anyRequest().permitAll()
            )

            // 2. CSRF
            // Deshabilitamos el CSRF de Spring Security porque tu app ya maneja
            // sus propios formularios con Thymeleaf y sesión HTTP propia.
            // Si lo dejas activo, todos tus formularios POST fallarán con 403.
            .csrf(csrf -> csrf.disable())

            //3. LOGOUT
            // Cuando el usuario va a /logout, Spring Security invalida la sesión
            // y redirige al inicio. Así tu @GetMapping("/logout") existente
            // en AuthController queda reemplazado por este (más seguro).
            .logout(logout -> logout
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .logoutSuccessUrl("/")
                .permitAll()
            )

            //4. ACCESO DENEGADO
            // Si alguien intenta entrar a /admin sin estar logueado,
            // Spring Security lo manda al inicio en vez de mostrar error 403.
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {
                    response.sendRedirect(request.getContextPath() + "/");
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.sendRedirect(request.getContextPath() + "/");
                })
            );

        return http.build();
    }
}
