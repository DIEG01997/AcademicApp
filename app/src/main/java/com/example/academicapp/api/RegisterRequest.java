package com.example.academicapp.api;

public class RegisterRequest {

    private String nombre;
    private String apellidos;
    private String correoEducativo;
    private String password;
    private String fechaNacimiento;
    private String telefono;
    private Long idCurso;

    public RegisterRequest(String nombre, String apellidos, String correoEducativo, String password,
                           String fechaNacimiento, String telefono, Long idCurso) {
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

    public String getApellidos() {
        return apellidos;
    }

    public String getCorreoEducativo() {
        return correoEducativo;
    }

    public String getPassword() {
        return password;
    }

    public String getFechaNacimiento() {
        return fechaNacimiento;
    }

    public String getTelefono() {
        return telefono;
    }

    public Long getIdCurso() {
        return idCurso;
    }
}
