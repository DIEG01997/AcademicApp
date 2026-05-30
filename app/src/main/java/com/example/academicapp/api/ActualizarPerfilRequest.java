package com.example.academicapp.api;

public class ActualizarPerfilRequest {

    private String nombre;
    private String apellidos;
    private String telefono;
    private String fechaNacimiento;
    private String fotoPerfilBase64;

    public ActualizarPerfilRequest(String nombre, String apellidos, String telefono, String fechaNacimiento) {
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.telefono = telefono;
        this.fechaNacimiento = fechaNacimiento;
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

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(String fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public String getFotoPerfilBase64() {
        return fotoPerfilBase64;
    }

    public void setFotoPerfilBase64(String fotoPerfilBase64) {
        this.fotoPerfilBase64 = fotoPerfilBase64;
    }
}
