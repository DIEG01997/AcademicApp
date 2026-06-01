package com.example.academicapp.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * DTO de entrada usado para validar y transportar datos recibidos desde la app movil.
 */
public class RegisterRequest {

    @NotBlank
    private String nombre;

    @NotBlank
    private String apellidos;

    @NotBlank
    @Email
    @Pattern(
        regexp = "^[A-Za-z0-9._%+-]+@educarex\\.es$",
        message = "El correo debe terminar en @educarex.es"
    )
    private String correoEducativo;

    @NotBlank
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$",
        message = "La contraseña debe tener al menos 8 caracteres, una mayúscula, una minúscula y un número"
    )
    private String password;

    @NotNull
    private LocalDate fechaNacimiento;

    private String telefono;

    @NotNull
    private Long idCurso;

    public RegisterRequest() {
    }

    public RegisterRequest(String nombre, String apellidos, String correoEducativo, String password,
                           LocalDate fechaNacimiento, String telefono, Long idCurso) {
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.correoEducativo = correoEducativo;
        this.password = password;
        this.fechaNacimiento = fechaNacimiento;
        this.telefono = telefono;
        this.idCurso = idCurso;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
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

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public Long getIdCurso() {
        return idCurso;
    }

    public void setIdCurso(Long idCurso) {
        this.idCurso = idCurso;
    }
}