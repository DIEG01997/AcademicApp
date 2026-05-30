package com.example.academicapp.api;

import java.util.List;

public class DashboardResponse {

    private MediaGlobalResponse mediaGlobal;
    private ResumenAsignaturaResponse mejorAsignatura;
    private ResumenAsignaturaResponse peorAsignatura;
    private ResumenUnidadResponse mejorUnidad;
    private ResumenUnidadResponse peorUnidad;
    private PorcentajeSuspensosAsignaturasResponse porcentajeSuspensosAsignaturas;
    private List<DashboardAsignaturaResponse> asignaturas;
    private List<DashboardTrimestreResponse> evolucionTrimestres;

    public MediaGlobalResponse getMediaGlobal() {
        return mediaGlobal;
    }

    public void setMediaGlobal(MediaGlobalResponse mediaGlobal) {
        this.mediaGlobal = mediaGlobal;
    }

    public ResumenAsignaturaResponse getMejorAsignatura() {
        return mejorAsignatura;
    }

    public void setMejorAsignatura(ResumenAsignaturaResponse mejorAsignatura) {
        this.mejorAsignatura = mejorAsignatura;
    }

    public ResumenAsignaturaResponse getPeorAsignatura() {
        return peorAsignatura;
    }

    public void setPeorAsignatura(ResumenAsignaturaResponse peorAsignatura) {
        this.peorAsignatura = peorAsignatura;
    }

    public ResumenUnidadResponse getMejorUnidad() {
        return mejorUnidad;
    }

    public void setMejorUnidad(ResumenUnidadResponse mejorUnidad) {
        this.mejorUnidad = mejorUnidad;
    }

    public ResumenUnidadResponse getPeorUnidad() {
        return peorUnidad;
    }

    public void setPeorUnidad(ResumenUnidadResponse peorUnidad) {
        this.peorUnidad = peorUnidad;
    }

    public PorcentajeSuspensosAsignaturasResponse getPorcentajeSuspensosAsignaturas() {
        return porcentajeSuspensosAsignaturas;
    }

    public void setPorcentajeSuspensosAsignaturas(PorcentajeSuspensosAsignaturasResponse porcentajeSuspensosAsignaturas) {
        this.porcentajeSuspensosAsignaturas = porcentajeSuspensosAsignaturas;
    }

    public List<DashboardAsignaturaResponse> getAsignaturas() {
        return asignaturas;
    }

    public void setAsignaturas(List<DashboardAsignaturaResponse> asignaturas) {
        this.asignaturas = asignaturas;
    }

    public List<DashboardTrimestreResponse> getEvolucionTrimestres() {
        return evolucionTrimestres;
    }

    public void setEvolucionTrimestres(List<DashboardTrimestreResponse> evolucionTrimestres) {
        this.evolucionTrimestres = evolucionTrimestres;
    }
}
