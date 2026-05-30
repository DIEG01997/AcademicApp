package com.example.academicapp.dto;

import java.math.BigDecimal;

public class PercentilGlobalResponse {

    private boolean calculable;
    private String mensaje;
    private BigDecimal percentil;
    private Integer totalAlumnosComparados;
    private Integer minimoAlumnosRequeridos;

    public PercentilGlobalResponse() {
    }

    public PercentilGlobalResponse(boolean calculable, String mensaje, BigDecimal percentil,
                                   Integer totalAlumnosComparados, Integer minimoAlumnosRequeridos) {
        this.calculable = calculable;
        this.mensaje = mensaje;
        this.percentil = percentil;
        this.totalAlumnosComparados = totalAlumnosComparados;
        this.minimoAlumnosRequeridos = minimoAlumnosRequeridos;
    }

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

    public BigDecimal getPercentil() {
        return percentil;
    }

    public void setPercentil(BigDecimal percentil) {
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