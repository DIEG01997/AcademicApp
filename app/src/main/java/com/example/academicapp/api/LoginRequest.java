package com.example.academicapp.api;

/**
 * DTO de peticion enviado desde Android al backend.
 */
public class LoginRequest {

    private String correoEducativo;
    private String password;

    public LoginRequest(String correoEducativo, String password) {
        this.correoEducativo = correoEducativo;
        this.password = password;
    }

    public String getCorreoEducativo() {
        return correoEducativo;
    }

    public void setCorreoEducativo(String correoEducativo) {
        this.correoEducativo = correoEducativo;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
