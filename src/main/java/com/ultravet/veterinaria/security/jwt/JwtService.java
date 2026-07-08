package com.ultravet.veterinaria.security.jwt;

import com.ultravet.veterinaria.security.UsuarioPrincipal;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final Key key;
    private final long expirationMs;

    public JwtService(@Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration-ms}") long expirationMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    public String generarToken(UsuarioPrincipal principal) {
        Instant ahora = Instant.now();
        String rol = principal.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElse("ROLE_CLIENTE");

        return Jwts.builder()
                .setSubject(principal.getUsername())
                .claim("id", principal.getId())
                .claim("nombre", principal.getUsuario().getNombre())
                .claim("rol", rol)
                .setIssuedAt(Date.from(ahora))
                .setExpiration(Date.from(ahora.plusMillis(expirationMs)))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Jws<Claims> validar(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token);
    }

    public String extraerIdentificador(String token) {
        return validar(token).getBody().getSubject();
    }

    public long getExpirationMs() {
        return expirationMs;
    }
}
