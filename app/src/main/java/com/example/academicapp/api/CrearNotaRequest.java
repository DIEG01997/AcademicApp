package com.example.academicapp.api;

public class CrearNotaRequest {

    private Long idUnidad;
    private Long idInstrumento;
    private double valorCalificacion;
    private double ponderacion;

    public CrearNotaRequest(Long idUnidad, Long idInstrumento, double valorCalificacion, double ponderacion) {
        this.idUnidad = idUnidad;
        this.idInstrumento = idInstrumento;
        this.valorCalificacion = valorCalificacion;
        this.ponderacion = ponderacion;
    }

    public Long getIdUnidad() {
        return idUnidad;
    }

    public Long getIdInstrumento() {
        return idInstrumento;
    }

    public double getValorCalificacion() {
        return valorCalificacion;
    }

    public double getPonderacion() {
        return ponderacion;
    }
}
