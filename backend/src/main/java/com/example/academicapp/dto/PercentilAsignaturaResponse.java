package com.example.academicapp.dto;

import java.math.BigDecimal;

public class PercentilAsignaturaResponse {

    private boolean calculable;
    private String mensaje;
    private Long idAsignatura;
    private String nombreAsignatura;
    private BigDecimal percentil;
    private Integer totalAlumnosComparados;
    private Integer minimoAlumnosRequeridos;

    public PercentilAsignaturaResponse() {
    }

    public PercentilAsignaturaResponse(boolean calculable, String mensaje, Long idAsignatura,
                                       String nombreAsignatura, BigDecimal percentil,
                                       Integer totalAlumnosComparados, Integer minimoAlumnosRequeridos) {
        this.calculable = calculable;
        this.mensaje = mensaje;
        this.idAsignatura = idAsignatura;
        this.nombreAsignatura = nombreAsignatura;
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