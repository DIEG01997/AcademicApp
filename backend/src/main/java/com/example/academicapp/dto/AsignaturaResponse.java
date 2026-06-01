package com.example.academicapp.dto;

/**
 * DTO de salida que desacopla las entidades JPA del contrato publico de la API.
 */
public class AsignaturaResponse {

    private Long idAsignatura;
    private String nombreAsignatura;
    private String descripcion;

    public AsignaturaResponse() {
    }

    public AsignaturaResponse(Long idAsignatura, String nombreAsignatura, String descripcion) {
        this.idAsignatura = idAsignatura;
        this.nombreAsignatura = nombreAsignatura;
        this.descripcion = descripcion;
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

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}