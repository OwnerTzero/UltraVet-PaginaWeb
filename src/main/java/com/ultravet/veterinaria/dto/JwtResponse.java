package com.ultravet.veterinaria.dto;

public class JwtResponse {

    private final String token;
    private final String tipo;
    private final long expiraEnMs;
    private final String nombre;
    private final String rol;

    public JwtResponse(String token, String tipo, long expiraEnMs, String nombre, String rol) {
        this.token = token;
        this.tipo = tipo;
        this.expiraEnMs = expiraEnMs;
        this.nombre = nombre;
        this.rol = rol;
    }

    public String getToken() {
        return token;
    }

    public String getTipo() {
        return tipo;
    }

    public long getExpiraEnMs() {
        return expiraEnMs;
    }

    public String getNombre() {
        return nombre;
    }

    public String getRol() {
        return rol;
    }
}
