package com.example.academicapp.dto;

import java.time.LocalDate;

/**
 * DTO de entrada usado para validar y transportar datos recibidos desde la app movil.
 */
public class ActualizarPerfilRequest {

    private String nombre;
    private String apellidos;
    private String telefono;
    private LocalDate fechaNacimiento;
    private String fotoPerfilBase64;

    public ActualizarPerfilRequest() {
    }

    public ActualizarPerfilRequest(String nombre, String apellidos, String telefono, LocalDate fechaNacimiento) {
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.telefono = telefono;
        this.fechaNacimiento = fechaNacimiento;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public String getFotoPerfilBase64() {
        return fotoPerfilBase64;
    }

    public void setFotoPerfilBase64(String fotoPerfilBase64) {
        this.fotoPerfilBase64 = fotoPerfilBase64;
    }
}
