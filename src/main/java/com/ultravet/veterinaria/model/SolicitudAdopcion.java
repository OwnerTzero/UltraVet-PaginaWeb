package com.ultravet.veterinaria.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "solicitudes_adopcion")
public class SolicitudAdopcion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 3, max = 50, message = "El nombre debe tener entre 3 y 50 caracteres")
    @Column(nullable = false, length = 50)
    private String nombre;

    @NotBlank(message = "El correo electrónico es obligatorio")
    @Email(message = "Debe ingresar un formato de correo electrónico válido")
    @Column(nullable = false, length = 100)
    private String correo;

    @NotBlank(message = "El teléfono de contacto es obligatorio")
    @Pattern(regexp = "^[0-9]{9,15}$", message = "El teléfono debe contener entre 9 y 15 dígitos numéricos")
    @Column(nullable = false, length = 15)
    private String telefono;

    @NotBlank(message = "Debe especificar la mascota que desea adoptar")
    @Column(nullable = false, length = 50)
    private String mascota;

    // Constructor vacío obligatorio para que JPA pueda instanciar las filas de la
    // BD
    public SolicitudAdopcion() {
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getMascota() {
        return mascota;
    }

    public void setMascota(String mascota) {
        this.mascota = mascota;
    }
}