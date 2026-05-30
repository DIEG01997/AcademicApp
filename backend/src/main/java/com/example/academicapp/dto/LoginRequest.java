package com.example.academicapp.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class LoginRequest {

    @NotBlank
    @Email
    @Pattern(
        regexp = "^[A-Za-z0-9._%+-]+@educarex\\.es$",
        message = "El correo debe terminar en @educarex.es"
    )
    private String correoEducativo;

    @NotBlank
    private String password;

    public LoginRequest() {
    }

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