package com.example.academicapp.api;

public class UnidadDidacticaResponse {

    private Long idUnidad;
    private String nombreUnidad;
    private String descripcion;
    private Integer ordenUnidad;

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

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Integer getOrdenUnidad() {
        return ordenUnidad;
    }

    public void setOrdenUnidad(Integer ordenUnidad) {
        this.ordenUnidad = ordenUnidad;
    }
}
