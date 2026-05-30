package com.example.academicapp.dto;

import java.math.BigDecimal;

public class MejorUnidadResponse {

    private Long idUnidad;
    private String nombreUnidad;
    private Long idAsignatura;
    private String nombreAsignatura;
    private BigDecimal notaFinal;

    public MejorUnidadResponse() {
    }

    public MejorUnidadResponse(Long idUnidad, String nombreUnidad,
                               Long idAsignatura, String nombreAsignatura,
                               BigDecimal notaFinal) {
        this.idUnidad = idUnidad;
        this.nombreUnidad = nombreUnidad;
        this.idAsignatura = idAsignatura;
        this.nombreAsignatura = nombreAsignatura;
        this.notaFinal = notaFinal;
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

    public BigDecimal getNotaFinal() {
        return notaFinal;
    }

    public void setNotaFinal(BigDecimal notaFinal) {
        this.notaFinal = notaFinal;
    }
}