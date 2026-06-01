package com.example.academicapp.dto;

/**
 * DTO de salida que desacopla las entidades JPA del contrato publico de la API.
 */
public class AuthResponse {

    private String mensaje;

    public AuthResponse() {
    }

    public AuthResponse(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
}