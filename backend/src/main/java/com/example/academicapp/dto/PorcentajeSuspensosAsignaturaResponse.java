package com.example.academicapp.dto;

import java.math.BigDecimal;

/**
 * DTO de salida que desacopla las entidades JPA del contrato publico de la API.
 */
public class PorcentajeSuspensosAsignaturaResponse {

    private Long idAsignatura;
    private String nombreAsignatura;
    private Integer unidadesEvaluadas;
    private Integer unidadesSuspensas;
    private BigDecimal porcentajeSuspensos;

    public PorcentajeSuspensosAsignaturaResponse() {
    }

    public PorcentajeSuspensosAsignaturaResponse(Long idAsignatura, String nombreAsignatura,
                                                 Integer unidadesEvaluadas, Integer unidadesSuspensas,
                                                 BigDecimal porcentajeSuspensos) {
        this.idAsignatura = idAsignatura;
        this.nombreAsignatura = nombreAsignatura;
        this.unidadesEvaluadas = unidadesEvaluadas;
        this.unidadesSuspensas = unidadesSuspensas;
        this.porcentajeSuspensos = porcentajeSuspensos;
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
}