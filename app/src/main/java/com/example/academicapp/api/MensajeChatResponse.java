package com.example.academicapp.api;

/**
 * DTO de respuesta recibido desde la API y usado por las pantallas Android.
 */
public class MensajeChatResponse {

    private Long idMensaje;
    private Long idAlumno;
    private String nombreAlumno;
    private String fotoPerfilBase64;
    private String contenido;
    private String fechaEnvio;
    private boolean propio;

    public Long getIdMensaje() {
        return idMensaje;
    }

    public Long getIdAlumno() {
        return idAlumno;
    }

    public String getNombreAlumno() {
        return nombreAlumno;
    }

    public String getFotoPerfilBase64() {
        return fotoPerfilBase64;
    }

    public String getContenido() {
        return contenido;
    }

    public String getFechaEnvio() {
        return fechaEnvio;
    }

    public boolean isPropio() {
        return propio;
    }
}
