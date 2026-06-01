package com.example.academicapp.api;

/**
 * DTO de peticion enviado desde Android al backend.
 */
public class CrearMensajeChatRequest {

    private String contenido;

    public CrearMensajeChatRequest(String contenido) {
        this.contenido = contenido;
    }

    public String getContenido() {
        return contenido;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
    }
}
