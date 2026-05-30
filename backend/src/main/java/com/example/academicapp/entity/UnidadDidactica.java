package com.example.academicapp.entity;

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
@Table(name = "unidad_didactica")
public class UnidadDidactica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_unidad")
    private Long idUnidad;

    @Column(name = "nombre_unidad", nullable = false, length = 120)
    private String nombreUnidad;

    @Column(name = "descripcion")
    private String descripcion;

    @Column(name = "orden_unidad", nullable = false)
    private Integer ordenUnidad;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_asignatura", nullable = false)
    private Asignatura asignatura;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_evaluacion", nullable = false)
    private Evaluacion evaluacion;

    public UnidadDidactica() {
    }

    public UnidadDidactica(Long idUnidad, String nombreUnidad, String descripcion, Integer ordenUnidad,
                           Asignatura asignatura, Evaluacion evaluacion) {
        this.idUnidad = idUnidad;
        this.nombreUnidad = nombreUnidad;
        this.descripcion = descripcion;
        this.ordenUnidad = ordenUnidad;
        this.asignatura = asignatura;
        this.evaluacion = evaluacion;
    }

    public Long getIdUnidad() {
        return idUnidad;
    }

    public void setIdUnidad(Long idUnidad) {
        this.idUnidad = idUnidad;
    }

    public String getNombreUnidad() {
        return nombreUnidad;
    }

    public void setNombreUnidad(String nombreUnidad) {
        this.nombreUnidad = nombreUnidad;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Integer getOrdenUnidad() {
        return ordenUnidad;
    }

    public void setOrdenUnidad(Integer ordenUnidad) {
        this.ordenUnidad = ordenUnidad;
    }

    public Asignatura getAsignatura() {
        return asignatura;
    }

    public void setAsignatura(Asignatura asignatura) {
        this.asignatura = asignatura;
    }

    public Evaluacion getEvaluacion() {
        return evaluacion;
    }

    public void setEvaluacion(Evaluacion evaluacion) {
        this.evaluacion = evaluacion;
    }
}