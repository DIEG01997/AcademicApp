package com.example.academicapp.api;

/**
 * DTO de respuesta recibido desde la API y usado por las pantallas Android.
 */
public class AlumnoCursoResponse {

    private Long idAlumno;
    private String nombre;
    private String apellidos;
    private String fotoPerfilBase64;

    public Long getIdAlumno() {
        return idAlumno;
    }

    public String getNombre() {
        return nombre;
    }

    public String getApellidos() {
        return apellidos;
    }

    public String getFotoPerfilBase64() {
        return fotoPerfilBase64;
    }
}
