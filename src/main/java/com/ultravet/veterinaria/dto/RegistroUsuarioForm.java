package com.ultravet.veterinaria.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class RegistroUsuarioForm {

    @NotBlank(message = "Ingresa tu nombre.")
    @Size(max = 100, message = "El nombre no debe superar 100 caracteres.")
    private String nombre;

    @NotBlank(message = "Ingresa tu DNI.")
    @Pattern(regexp = "\\d{8}", message = "El DNI debe tener 8 digitos.")
    private String dni;

    @NotBlank(message = "Ingresa tu correo.")
    @Email(message = "Ingresa un correo valido.")
    @Size(max = 120, message = "El correo no debe superar 120 caracteres.")
    private String correo;

    @NotBlank(message = "Ingresa tu telefono.")
    @Pattern(regexp = "[0-9 +()\\-]{7,20}", message = "Ingresa un telefono valido.")
    private String telefono;

    @NotBlank(message = "Ingresa una contrasena.")
    @Size(min = 6, max = 72, message = "La contrasena debe tener entre 6 y 72 caracteres.")
    private String password;

    @NotBlank(message = "Confirma tu contrasena.")
    private String confirmPassword;

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = limpiar(nombre);
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = limpiar(dni);
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = limpiar(correo);
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = limpiar(telefono);
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    private String limpiar(String valor) {
        return valor == null ? "" : valor.trim();
    }
}
