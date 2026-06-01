package com.example.academicapp.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "alumno")
/**
 * Entidad JPA que representa una tabla del modelo de datos academico.
 */
public class Alumno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_alumno")
    private Long idAlumno;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "apellidos", nullable = false, length = 150)
    private String apellidos;

    @Column(name = "correo_educativo", nullable = false, unique = true, length = 150)
    private String correoEducativo;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "fecha_nacimiento", nullable = false)
    private LocalDate fechaNacimiento;

    @Column(name = "telefono", length = 20)
    private String telefono;

    @Column(name = "foto_perfil_base64", columnDefinition = "LONGTEXT")
    private String fotoPerfilBase64;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro;

    @Column(name = "tutorial_completado", nullable = false)
    private boolean tutorialCompletado = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_curso", nullable = false)
    private Curso curso;

    public Alumno() {
    }

    public Alumno(Long idAlumno, String nombre, String apellidos, String correoEducativo, String passwordHash,
                  LocalDate fechaNacimiento, String telefono, LocalDateTime fechaRegistro, Curso curso) {
        this.idAlumno = idAlumno;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.correoEducativo = correoEducativo;
        this.passwordHash = passwordHash;
        this.fechaNacimiento = fechaNacimiento;
        this.telefono = telefono;
        this.fechaRegistro = fechaRegistro;
        this.curso = curso;
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

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
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

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public boolean isTutorialCompletado() {
        return tutorialCompletado;
    }

    public void setTutorialCompletado(boolean tutorialCompletado) {
        this.tutorialCompletado = tutorialCompletado;
    }

    public Curso getCurso() {
        return curso;
    }

    public void setCurso(Curso curso) {
        this.curso = curso;
    }
}
