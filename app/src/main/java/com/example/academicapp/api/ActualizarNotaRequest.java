package com.example.academicapp.api;

public class ActualizarNotaRequest {

    private double valorCalificacion;
    private double ponderacion;

    public ActualizarNotaRequest(double valorCalificacion, double ponderacion) {
        this.valorCalificacion = valorCalificacion;
        this.ponderacion = ponderacion;
    }

    public double getValorCalificacion() {
        return valorCalificacion;
    }

    public double getPonderacion() {
        return ponderacion;
    }
}
