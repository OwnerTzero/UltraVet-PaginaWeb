package com.ultravet.veterinaria.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class LoginForm {

    @Pattern(regexp = "CLIENTE|ADMIN", message = "Selecciona un tipo de acceso valido.")
    private String tipoAcceso = "CLIENTE";

    @Email(message = "Ingresa un correo valido.")
    @Size(max = 120, message = "El correo no debe superar 120 caracteres.")
    private String correo;

    @Pattern(regexp = "^$|\\d{8}", message = "El DNI debe tener 8 digitos.")
    private String dni = "";

    @NotBlank(message = "Ingresa tu contrasena.")
    @Size(max = 72, message = "La contrasena no debe superar 72 caracteres.")
    private String password;

    public String getTipoAcceso() {
        return tipoAcceso;
    }

    public void setTipoAcceso(String tipoAcceso) {
        this.tipoAcceso = limpiar(tipoAcceso);
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = limpiar(correo);
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = limpiar(dni);
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    private String limpiar(String valor) {
        return valor == null ? "" : valor.trim();
    }
}
