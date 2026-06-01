package com.example.academicapp.entity;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "curso")
/**
 * Entidad JPA que representa una tabla del modelo de datos academico.
 */
public class Curso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_curso")
    private Long idCurso;

    @Column(name = "numero_curso", nullable = false)
    private Integer numeroCurso;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_ciclo", nullable = false)
    private Ciclo ciclo;

    @ManyToMany
    @JoinTable(
        name = "curso_asignatura",
        joinColumns = @JoinColumn(name = "id_curso"),
        inverseJoinColumns = @JoinColumn(name = "id_asignatura")
    )
    private Set<Asignatura> asignaturas = new HashSet<>();

    public Curso() {
    }

    public Curso(Long idCurso, Integer numeroCurso, Ciclo ciclo, Set<Asignatura> asignaturas) {
        this.idCurso = idCurso;
        this.numeroCurso = numeroCurso;
        this.ciclo = ciclo;
        this.asignaturas = asignaturas;
    }

    public Long getIdCurso() {
        return idCurso;
    }

    public void setIdCurso(Long idCurso) {
        this.idCurso = idCurso;
    }

    public Integer getNumeroCurso() {
        return numeroCurso;
    }

    public void setNumeroCurso(Integer numeroCurso) {
        this.numeroCurso = numeroCurso;
    }

    public Ciclo getCiclo() {
        return ciclo;
    }

    public void setCiclo(Ciclo ciclo) {
        this.ciclo = ciclo;
    }

    public Set<Asignatura> getAsignaturas() {
        return asignaturas;
    }

    public void setAsignaturas(Set<Asignatura> asignaturas) {
        this.asignaturas = asignaturas;
    }
}