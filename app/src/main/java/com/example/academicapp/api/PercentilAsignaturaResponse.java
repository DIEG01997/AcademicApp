package com.example.academicapp.api;

public class PercentilAsignaturaResponse {

    private boolean calculable;
    private String mensaje;
    private Long idAsignatura;
    private String nombreAsignatura;
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
