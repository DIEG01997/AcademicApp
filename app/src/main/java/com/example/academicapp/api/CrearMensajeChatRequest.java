package com.example.academicapp.api;

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
