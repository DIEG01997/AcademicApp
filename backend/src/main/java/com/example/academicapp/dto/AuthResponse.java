package com.example.academicapp.dto;

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