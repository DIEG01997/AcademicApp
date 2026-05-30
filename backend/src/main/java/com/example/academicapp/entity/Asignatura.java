package com.example.academicapp.entity;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "asignatura")
public class Asignatura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_asignatura")
    private Long idAsignatura;

    @Column(name = "nombre_asignatura", nullable = false, length = 120)
    private String nombreAsignatura;

    @Column(name = "descripcion")
    private String descripcion;

    @ManyToMany(mappedBy = "asignaturas")
    private Set<Curso> cursos = new HashSet<>();

    public Asignatura() {
    }

    public Asignatura(Long idAsignatura, String nombreAsignatura, String descripcion, Set<Curso> cursos) {
        this.idAsignatura = idAsignatura;
        this.nombreAsignatura = nombreAsignatura;
        this.descripcion = descripcion;
        this.cursos = cursos;
    }

    public Long getIdAsignatura() {
        return idAsignatura;
    }

    public void setIdAsignatura(Long idAsignatura) {
        this.idAsignatura = idAsignatura;
    }

    public String getNombreAsignatura() {
        return nombreAsignatura;
    }

    public void setNombreAsignatura(String nombreAsignatura) {
        this.nombreAsignatura = nombreAsignatura;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Set<Curso> getCursos() {
        return cursos;
    }

    public void setCursos(Set<Curso> cursos) {
        this.cursos = cursos;
    }
}