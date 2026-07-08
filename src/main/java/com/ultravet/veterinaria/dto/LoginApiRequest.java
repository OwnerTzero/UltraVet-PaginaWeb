package com.ultravet.veterinaria.dto;

import jakarta.validation.constraints.NotBlank;

public class LoginApiRequest {

    @NotBlank(message = "Ingresa tu correo o DNI.")
    private String identificador;

    @NotBlank(message = "Ingresa tu contrasena.")
    private String password;

    public String getIdentificador() {
        return identificador;
    }

    public void setIdentificador(String identificador) {
        this.identificador = identificador == null ? "" : identificador.trim();
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
