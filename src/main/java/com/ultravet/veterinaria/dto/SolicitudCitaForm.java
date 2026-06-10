package com.ultravet.veterinaria.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalTime;
import org.springframework.format.annotation.DateTimeFormat;

public class SolicitudCitaForm {

    @NotNull(message = "Debe seleccionar un servicio")
    private Long servicioId;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
    private String nombre;

    @NotBlank(message = "El correo electronico es obligatorio")
    @Email(message = "Debe ingresar un correo electronico valido")
    private String correo;

    @NotBlank(message = "El telefono de contacto es obligatorio")
    @Pattern(regexp = "^[0-9 ]{7,15}$", message = "El telefono debe contener entre 7 y 15 digitos")
    private String telefono;

    @NotBlank(message = "El nombre de la mascota es obligatorio")
    @Size(max = 80, message = "El nombre de la mascota debe tener como maximo 80 caracteres")
    private String mascotaNombre;

    @NotBlank(message = "El tipo de mascota es obligatorio")
    @Size(max = 50, message = "El tipo de mascota debe tener como maximo 50 caracteres")
    private String mascotaTipo;

    @NotNull(message = "Debe seleccionar una fecha")
    @FutureOrPresent(message = "La fecha no puede ser anterior a hoy")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fecha;

    @NotNull(message = "Debe seleccionar una hora")
    @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
    private LocalTime hora;

    @Size(max = 500, message = "El comentario debe tener como maximo 500 caracteres")
    private String comentario;

    public Long getServicioId() {
        return servicioId;
    }

    public void setServicioId(Long servicioId) {
        this.servicioId = servicioId;
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

    public String getMascotaNombre() {
        return mascotaNombre;
    }

    public void setMascotaNombre(String mascotaNombre) {
        this.mascotaNombre = mascotaNombre;
    }

    public String getMascotaTipo() {
        return mascotaTipo;
    }

    public void setMascotaTipo(String mascotaTipo) {
        this.mascotaTipo = mascotaTipo;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public LocalTime getHora() {
        return hora;
    }

    public void setHora(LocalTime hora) {
        this.hora = hora;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }
}
