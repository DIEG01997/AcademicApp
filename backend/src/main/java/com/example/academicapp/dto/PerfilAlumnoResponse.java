package com.example.academicapp.dto;

import java.time.LocalDate;

/**
 * DTO de salida que desacopla las entidades JPA del contrato publico de la API.
 */
public class PerfilAlumnoResponse {

    private Long idAlumno;
    private String nombre;
    private String apellidos;
    private String correoEducativo;
    private LocalDate fechaNacimiento;
    private String telefono;
    private String fotoPerfilBase64;
    private Integer numeroCurso;
    private String siglasCiclo;
    private String nombreCiclo;
    private boolean tutorialCompletado;

    public PerfilAlumnoResponse() {
    }

    public PerfilAlumnoResponse(Long idAlumno, String nombre, String apellidos, String correoEducativo,
                                LocalDate fechaNacimiento, String telefono, Integer numeroCurso,
                                String siglasCiclo, String nombreCiclo) {
        this(idAlumno, nombre, apellidos, correoEducativo, fechaNacimiento, telefono, null,
                numeroCurso, siglasCiclo, nombreCiclo, false);
    }

    public PerfilAlumnoResponse(Long idAlumno, String nombre, String apellidos, String correoEducativo,
                                LocalDate fechaNacimiento, String telefono, String fotoPerfilBase64,
                                Integer numeroCurso, String siglasCiclo, String nombreCiclo) {
        this(idAlumno, nombre, apellidos, correoEducativo, fechaNacimiento, telefono, fotoPerfilBase64,
                numeroCurso, siglasCiclo, nombreCiclo, false);
    }

    public PerfilAlumnoResponse(Long idAlumno, String nombre, String apellidos, String correoEducativo,
                                LocalDate fechaNacimiento, String telefono, String fotoPerfilBase64,
                                Integer numeroCurso, String siglasCiclo, String nombreCiclo,
                                boolean tutorialCompletado) {
        this.idAlumno = idAlumno;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.correoEducativo = correoEducativo;
        this.fechaNacimiento = fechaNacimiento;
        this.telefono = telefono;
        this.fotoPerfilBase64 = fotoPerfilBase64;
        this.numeroCurso = numeroCurso;
        this.siglasCiclo = siglasCiclo;
        this.nombreCiclo = nombreCiclo;
        this.tutorialCompletado = tutorialCompletado;
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

    public String getCorreoEducativo() {
        return correoEducativo;
    }

    public void setCorreoEducativo(String correoEducativo) {
        this.correoEducativo = correoEducativo;
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

    public String getFotoPerfilBase64() {
        return fotoPerfilBase64;
    }

    public void setFotoPerfilBase64(String fotoPerfilBase64) {
        this.fotoPerfilBase64 = fotoPerfilBase64;
    }

    public Integer getNumeroCurso() {
        return numeroCurso;
    }

    public void setNumeroCurso(Integer numeroCurso) {
        this.numeroCurso = numeroCurso;
    }

    public String getSiglasCiclo() {
        return siglasCiclo;
    }

    public void setSiglasCiclo(String siglasCiclo) {
        this.siglasCiclo = siglasCiclo;
    }

    public String getNombreCiclo() {
        return nombreCiclo;
    }

    public void setNombreCiclo(String nombreCiclo) {
        this.nombreCiclo = nombreCiclo;
    }

    public boolean isTutorialCompletado() {
        return tutorialCompletado;
    }

    public void setTutorialCompletado(boolean tutorialCompletado) {
        this.tutorialCompletado = tutorialCompletado;
    }
}
