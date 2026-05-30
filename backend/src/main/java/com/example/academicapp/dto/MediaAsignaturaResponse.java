package com.example.academicapp.dto;

import java.math.BigDecimal;

public class MediaAsignaturaResponse {

    private Long idAsignatura;
    private String nombreAsignatura;
    private BigDecimal mediaAsignatura;
    private Integer unidadesComputadas;

    public MediaAsignaturaResponse() {
    }

    public MediaAsignaturaResponse(Long idAsignatura, String nombreAsignatura,
                                   BigDecimal mediaAsignatura, Integer unidadesComputadas) {
        this.idAsignatura = idAsignatura;
        this.nombreAsignatura = nombreAsignatura;
        this.mediaAsignatura = mediaAsignatura;
        this.unidadesComputadas = unidadesComputadas;
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

    public BigDecimal getMediaAsignatura() {
        return mediaAsignatura;
    }

    public void setMediaAsignatura(BigDecimal mediaAsignatura) {
        this.mediaAsignatura = mediaAsignatura;
    }

    public Integer getUnidadesComputadas() {
        return unidadesComputadas;
    }

    public void setUnidadesComputadas(Integer unidadesComputadas) {
        this.unidadesComputadas = unidadesComputadas;
    }
}