package com.example.academicapp.api;

public class ResumenAsignaturaResponse {

    private Long idAsignatura;
    private String nombreAsignatura;
    private String mediaAsignatura;
    private Integer unidadesComputadas;

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

    public String getMediaAsignatura() {
        return mediaAsignatura;
    }

    public void setMediaAsignatura(String mediaAsignatura) {
        this.mediaAsignatura = mediaAsignatura;
    }

    public Integer getUnidadesComputadas() {
        return unidadesComputadas;
    }

    public void setUnidadesComputadas(Integer unidadesComputadas) {
        this.unidadesComputadas = unidadesComputadas;
    }
}
