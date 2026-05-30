package com.example.academicapp.dto;

public class AlumnoCursoResponse {

    private Long idAlumno;
    private String nombre;
    private String apellidos;
    private String fotoPerfilBase64;

    public AlumnoCursoResponse() {
    }

    public AlumnoCursoResponse(Long idAlumno, String nombre, String apellidos) {
        this(idAlumno, nombre, apellidos, null);
    }

    public AlumnoCursoResponse(Long idAlumno, String nombre, String apellidos, String fotoPerfilBase64) {
        this.idAlumno = idAlumno;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.fotoPerfilBase64 = fotoPerfilBase64;
    }

    public Long getIdAlumno() {
        return idAlumno;
    }

    public void setIdAlumno(Long idAlumno) {
        this.idAlumno = idAlumno;
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

    public String getFotoPerfilBase64() {
        return fotoPerfilBase64;
    }

    public void setFotoPerfilBase64(String fotoPerfilBase64) {
        this.fotoPerfilBase64 = fotoPerfilBase64;
    }
}
