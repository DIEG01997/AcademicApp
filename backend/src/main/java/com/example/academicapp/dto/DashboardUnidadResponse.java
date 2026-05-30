package com.example.academicapp.dto;

import java.math.BigDecimal;

public class DashboardUnidadResponse {

    private Long idUnidad;
    private String nombreUnidad;
    private Integer ordenUnidad;
    private Integer numeroEvaluacion;
    private BigDecimal notaFinal;

    public DashboardUnidadResponse() {
    }

    public DashboardUnidadResponse(Long idUnidad, String nombreUnidad, Integer ordenUnidad,
                                   Integer numeroEvaluacion, BigDecimal notaFinal) {
        this.idUnidad = idUnidad;
        this.nombreUnidad = nombreUnidad;
        this.ordenUnidad = ordenUnidad;
        this.numeroEvaluacion = numeroEvaluacion;
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

    public Integer getOrdenUnidad() {
        return ordenUnidad;
    }

    public void setOrdenUnidad(Integer ordenUnidad) {
        this.ordenUnidad = ordenUnidad;
    }

    public Integer getNumeroEvaluacion() {
        return numeroEvaluacion;
    }

    public void setNumeroEvaluacion(Integer numeroEvaluacion) {
        this.numeroEvaluacion = numeroEvaluacion;
    }

    public BigDecimal getNotaFinal() {
        return notaFinal;
    }

    public void setNotaFinal(BigDecimal notaFinal) {
        this.notaFinal = notaFinal;
    }
}
