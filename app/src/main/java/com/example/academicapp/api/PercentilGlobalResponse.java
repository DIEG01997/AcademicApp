package com.example.academicapp.api;

/**
 * DTO de respuesta recibido desde la API y usado por las pantallas Android.
 */
public class PercentilGlobalResponse {

    private boolean calculable;
    private String mensaje;
    private String percentil;
    private Integer totalAlumnosComparados;
    private Integer minimoAlumnosRequeridos;

    public boolean isCalculable() {
        return calculable;
    }

    public void setCalculable(boolean calculable) {
        this.calculable = calculable;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getPercentil() {
        return percentil;
    }

    public void setPercentil(String percentil) {
        this.percentil = percentil;
    }

    public Integer getTotalAlumnosComparados() {
        return totalAlumnosComparados;
    }

    public void setTotalAlumnosComparados(Integer totalAlumnosComparados) {
        this.totalAlumnosComparados = totalAlumnosComparados;
    }

    public Integer getMinimoAlumnosRequeridos() {
        return minimoAlumnosRequeridos;
    }

    public void setMinimoAlumnosRequeridos(Integer minimoAlumnosRequeridos) {
        this.minimoAlumnosRequeridos = minimoAlumnosRequeridos;
    }
}
