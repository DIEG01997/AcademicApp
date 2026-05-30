package com.example.academicapp.api;

public class PorcentajeSuspensosAsignaturasResponse {

    private Integer asignaturasEvaluadas;
    private Integer asignaturasSuspensas;
    private String porcentajeSuspensos;

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

    public String getPorcentajeSuspensos() {
        return porcentajeSuspensos;
    }

    public void setPorcentajeSuspensos(String porcentajeSuspensos) {
        this.porcentajeSuspensos = porcentajeSuspensos;
    }
}
