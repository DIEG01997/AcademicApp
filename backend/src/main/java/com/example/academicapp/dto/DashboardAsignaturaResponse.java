package com.example.academicapp.dto;

import java.math.BigDecimal;
import java.util.List;

public class DashboardAsignaturaResponse {

    private Long idAsignatura;
    private String nombreAsignatura;
    private BigDecimal mediaAsignatura;
    private Integer unidadesComputadas;
    private Integer unidadesEvaluadas;
    private Integer unidadesSuspensas;
    private BigDecimal porcentajeSuspensos;
    private List<DashboardUnidadResponse> unidades;

    public DashboardAsignaturaResponse() {
    }

    public DashboardAsignaturaResponse(Long idAsignatura, String nombreAsignatura,
                                       BigDecimal mediaAsignatura, Integer unidadesComputadas,
                                       Integer unidadesEvaluadas, Integer unidadesSuspensas,
                                       BigDecimal porcentajeSuspensos) {
        this(idAsignatura, nombreAsignatura, mediaAsignatura, unidadesComputadas,
                unidadesEvaluadas, unidadesSuspensas, porcentajeSuspensos, List.of());
    }

    public DashboardAsignaturaResponse(Long idAsignatura, String nombreAsignatura,
                                       BigDecimal mediaAsignatura, Integer unidadesComputadas,
                                       Integer unidadesEvaluadas, Integer unidadesSuspensas,
                                       BigDecimal porcentajeSuspensos,
                                       List<DashboardUnidadResponse> unidades) {
        this.idAsignatura = idAsignatura;
        this.nombreAsignatura = nombreAsignatura;
        this.mediaAsignatura = mediaAsignatura;
        this.unidadesComputadas = unidadesComputadas;
        this.unidadesEvaluadas = unidadesEvaluadas;
        this.unidadesSuspensas = unidadesSuspensas;
        this.porcentajeSuspensos = porcentajeSuspensos;
        this.unidades = unidades;
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

    public Integer getUnidadesEvaluadas() {
        return unidadesEvaluadas;
    }

    public void setUnidadesEvaluadas(Integer unidadesEvaluadas) {
        this.unidadesEvaluadas = unidadesEvaluadas;
    }

    public Integer getUnidadesSuspensas() {
        return unidadesSuspensas;
    }

    public void setUnidadesSuspensas(Integer unidadesSuspensas) {
        this.unidadesSuspensas = unidadesSuspensas;
    }

    public BigDecimal getPorcentajeSuspensos() {
        return porcentajeSuspensos;
    }

    public void setPorcentajeSuspensos(BigDecimal porcentajeSuspensos) {
        this.porcentajeSuspensos = porcentajeSuspensos;
    }

    public List<DashboardUnidadResponse> getUnidades() {
        return unidades;
    }

    public void setUnidades(List<DashboardUnidadResponse> unidades) {
        this.unidades = unidades;
    }
}
