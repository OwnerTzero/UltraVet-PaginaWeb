package com.ultravet.veterinaria.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class SolicitudAdopcionForm {

    @NotNull(message = "Debe seleccionar una mascota")
    private Long mascotaId;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
    private String nombre;

    @NotBlank(message = "El correo electronico es obligatorio")
    @Email(message = "Debe ingresar un correo electronico valido")
    private String correo;

    @NotBlank(message = "El telefono de contacto es obligatorio")
    @Pattern(regexp = "^[0-9 ]{7,15}$", message = "El telefono debe contener entre 7 y 15 digitos")
    private String telefono;

    @NotBlank(message = "El DNI es obligatorio")
    @Pattern(regexp = "\\d{8}", message = "El DNI debe tener 8 digitos")
    private String dni;

    @NotBlank(message = "Debe seleccionar su sexo")
    @Pattern(regexp = "MASCULINO|FEMENINO|NO_INDICA", message = "Seleccione un sexo valido")
    private String sexo;

    @NotBlank(message = "El distrito es obligatorio")
    @Size(max = 120, message = "El distrito debe tener como maximo 120 caracteres")
    private String distrito;

    @NotBlank(message = "Debe indicar si tiene experiencia con mascotas")
    @Size(max = 80, message = "La experiencia debe tener como maximo 80 caracteres")
    private String experienciaMascotas;

    public Long getMascotaId() {
        return mascotaId;
    }

    public void setMascotaId(Long mascotaId) {
        this.mascotaId = mascotaId;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public String getSexo() {
        return sexo;
    }

    public void setSexo(String sexo) {
        this.sexo = sexo;
    }

    public String getDistrito() {
        return distrito;
    }

    public void setDistrito(String distrito) {
        this.distrito = distrito;
    }

    public String getExperienciaMascotas() {
        return experienciaMascotas;
    }

    public void setExperienciaMascotas(String experienciaMascotas) {
        this.experienciaMascotas = experienciaMascotas;
    }
}
