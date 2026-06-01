package com.example.academicapp.api;

/**
 * DTO de respuesta recibido desde la API y usado por las pantallas Android.
 */
public class DashboardTrimestreResponse {

    private Integer numeroEvaluacion;
    private String nombreEvaluacion;
    private String fechaInicio;
    private String fechaFin;
    private String media;
    private Integer asignaturasComputadas;

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

    public String getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(String fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public String getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(String fechaFin) {
        this.fechaFin = fechaFin;
    }

    public String getMedia() {
        return media;
    }

    public void setMedia(String media) {
        this.media = media;
    }

    public Integer getAsignaturasComputadas() {
        return asignaturasComputadas;
    }

    public void setAsignaturasComputadas(Integer asignaturasComputadas) {
        this.asignaturasComputadas = asignaturasComputadas;
    }
}
