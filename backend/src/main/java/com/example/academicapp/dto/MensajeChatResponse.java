package com.example.academicapp.dto;

import java.time.LocalDateTime;

/**
 * DTO de salida que desacopla las entidades JPA del contrato publico de la API.
 */
public class MensajeChatResponse {

    private Long idMensaje;
    private Long idAlumno;
    private String nombreAlumno;
    private String fotoPerfilBase64;
    private String contenido;
    private LocalDateTime fechaEnvio;
    private boolean propio;

    public MensajeChatResponse(Long idMensaje, Long idAlumno, String nombreAlumno,
                               String contenido, LocalDateTime fechaEnvio, boolean propio) {
        this(idMensaje, idAlumno, nombreAlumno, null, contenido, fechaEnvio, propio);
    }

    public MensajeChatResponse(Long idMensaje, Long idAlumno, String nombreAlumno,
                               String fotoPerfilBase64, String contenido,
                               LocalDateTime fechaEnvio, boolean propio) {
        this.idMensaje = idMensaje;
        this.idAlumno = idAlumno;
        this.nombreAlumno = nombreAlumno;
        this.fotoPerfilBase64 = fotoPerfilBase64;
        this.contenido = contenido;
        this.fechaEnvio = fechaEnvio;
        this.propio = propio;
    }

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

    public LocalDateTime getFechaEnvio() {
        return fechaEnvio;
    }

    public boolean isPropio() {
        return propio;
    }
}
