package com.example.academicapp.dto;

import java.math.BigDecimal;

/**
 * DTO de salida que desacopla las entidades JPA del contrato publico de la API.
 */
public class PorcentajeSuspensosAsignaturasResponse {

    private Integer asignaturasEvaluadas;
    private Integer asignaturasSuspensas;
    private BigDecimal porcentajeSuspensos;

    public PorcentajeSuspensosAsignaturasResponse() {
    }

    public PorcentajeSuspensosAsignaturasResponse(Integer asignaturasEvaluadas,
                                                   Integer asignaturasSuspensas,
                                                   BigDecimal porcentajeSuspensos) {
        this.asignaturasEvaluadas = asignaturasEvaluadas;
        this.asignaturasSuspensas = asignaturasSuspensas;
        this.porcentajeSuspensos = porcentajeSuspensos;
    }

    public Integer getAsignaturasEvaluadas() {
        return asignaturasEvaluadas;
    }

    public void setAsignaturasEvaluadas(Integer asignaturasEvaluadas) {
        this.asignaturasEvaluadas = asignaturasEvaluadas;
    }

    public Integer getAsignaturasSuspensas() {
        return asignaturasSuspensas;
    }

    public void setAsignaturasSuspensas(Integer asignaturasSuspensas) {
        this.asignaturasSuspensas = asignaturasSuspensas;
    }

    public BigDecimal getPorcentajeSuspensos() {
        return porcentajeSuspensos;
    }

    public void setPorcentajeSuspensos(BigDecimal porcentajeSuspensos) {
        this.porcentajeSuspensos = porcentajeSuspensos;
    }
}