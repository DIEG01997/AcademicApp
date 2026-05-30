package com.example.academicapp.dto;

public class UnidadDidacticaResponse {

    private Long idUnidad;
    private String nombreUnidad;
    private String descripcion;
    private Integer ordenUnidad;

    public UnidadDidacticaResponse() {
    }

    public UnidadDidacticaResponse(Long idUnidad, String nombreUnidad, String descripcion, Integer ordenUnidad) {
        this.idUnidad = idUnidad;
        this.nombreUnidad = nombreUnidad;
        this.descripcion = descripcion;
        this.ordenUnidad = ordenUnidad;
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