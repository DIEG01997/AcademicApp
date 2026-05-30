package com.example.academicapp.dto;

import java.math.BigDecimal;

public class NotaFinalUnidadResponse {

    private Long idUnidad;
    private String nombreUnidad;
    private BigDecimal notaFinal;

    public NotaFinalUnidadResponse() {
    }

    public NotaFinalUnidadResponse(Long idUnidad, String nombreUnidad, BigDecimal notaFinal) {
        this.idUnidad = idUnidad;
        this.nombreUnidad = nombreUnidad;
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

    public BigDecimal getNotaFinal() {
        return notaFinal;
    }

    public void setNotaFinal(BigDecimal notaFinal) {
        this.notaFinal = notaFinal;
    }
}