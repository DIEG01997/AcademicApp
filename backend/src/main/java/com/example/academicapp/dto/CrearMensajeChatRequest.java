package com.example.academicapp.dto;

/**
 * DTO de entrada usado para validar y transportar datos recibidos desde la app movil.
 */
public class CrearMensajeChatRequest {

    private String contenido;

    public String getContenido() {
        return contenido;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
    }
}
