package com.example.academicapp.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO de salida que desacopla las entidades JPA del contrato publico de la API.
 */
public class DashboardTrimestreResponse {

    private Integer numeroEvaluacion;
    private String nombreEvaluacion;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private BigDecimal media;
    private Integer asignaturasComputadas;

    public DashboardTrimestreResponse() {
    }

    public DashboardTrimestreResponse(Integer numeroEvaluacion, String nombreEvaluacion,
                                      BigDecimal media, Integer asignaturasComputadas) {
        this(numeroEvaluacion, nombreEvaluacion, null, null, media, asignaturasComputadas);
    }

    public DashboardTrimestreResponse(Integer numeroEvaluacion, String nombreEvaluacion,
                                      LocalDate fechaInicio, LocalDate fechaFin,
                                      BigDecimal media, Integer asignaturasComputadas) {
        this.numeroEvaluacion = numeroEvaluacion;
        this.nombreEvaluacion = nombreEvaluacion;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.media = media;
        this.asignaturasComputadas = asignaturasComputadas;
    }

    public Integer getNumeroEvaluacion() {
        return numeroEvaluacion;
    }

    public void setNumeroEvaluacion(Integer numeroEvaluacion) {
        this.numeroEvaluacion = numeroEvaluacion;
    }

    public String getNombreEvaluacion() {
        return nombreEvaluacion;
    }

    public void setNombreEvaluacion(String nombreEvaluacion) {
        this.nombreEvaluacion = nombreEvaluacion;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDate fechaFin) {
        this.fechaFin = fechaFin;
    }

    public BigDecimal getMedia() {
        return media;
    }

    public void setMedia(BigDecimal media) {
        this.media = media;
    }

    public Integer getAsignaturasComputadas() {
        return asignaturasComputadas;
    }

    public void setAsignaturasComputadas(Integer asignaturasComputadas) {
        this.asignaturasComputadas = asignaturasComputadas;
    }
}
