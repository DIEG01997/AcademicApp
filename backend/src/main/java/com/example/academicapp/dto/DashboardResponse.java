package com.example.academicapp.dto;

import java.util.List;

/**
 * DTO de salida que desacopla las entidades JPA del contrato publico de la API.
 */
public class DashboardResponse {

    private MediaGlobalResponse mediaGlobal;
    private MejorAsignaturaResponse mejorAsignatura;
    private PeorAsignaturaResponse peorAsignatura;
    private MejorUnidadResponse mejorUnidad;
    private PeorUnidadResponse peorUnidad;
    private PorcentajeSuspensosAsignaturasResponse porcentajeSuspensosAsignaturas;
    private List<DashboardAsignaturaResponse> asignaturas;
    private List<DashboardTrimestreResponse> evolucionTrimestres;

    public DashboardResponse() {
    }

    public DashboardResponse(MediaGlobalResponse mediaGlobal,
                             MejorAsignaturaResponse mejorAsignatura,
                             PeorAsignaturaResponse peorAsignatura,
                             MejorUnidadResponse mejorUnidad,
                             PeorUnidadResponse peorUnidad,
                             PorcentajeSuspensosAsignaturasResponse porcentajeSuspensosAsignaturas,
                             List<DashboardAsignaturaResponse> asignaturas) {
        this(mediaGlobal, mejorAsignatura, peorAsignatura, mejorUnidad, peorUnidad,
                porcentajeSuspensosAsignaturas, asignaturas, List.of());
    }

    public DashboardResponse(MediaGlobalResponse mediaGlobal,
                             MejorAsignaturaResponse mejorAsignatura,
                             PeorAsignaturaResponse peorAsignatura,
                             MejorUnidadResponse mejorUnidad,
                             PeorUnidadResponse peorUnidad,
                             PorcentajeSuspensosAsignaturasResponse porcentajeSuspensosAsignaturas,
                             List<DashboardAsignaturaResponse> asignaturas,
                             List<DashboardTrimestreResponse> evolucionTrimestres) {
        this.mediaGlobal = mediaGlobal;
        this.mejorAsignatura = mejorAsignatura;
        this.peorAsignatura = peorAsignatura;
        this.mejorUnidad = mejorUnidad;
        this.peorUnidad = peorUnidad;
        this.porcentajeSuspensosAsignaturas = porcentajeSuspensosAsignaturas;
        this.asignaturas = asignaturas;
        this.evolucionTrimestres = evolucionTrimestres;
    }

    public MediaGlobalResponse getMediaGlobal() {
        return mediaGlobal;
    }

    public void setMediaGlobal(MediaGlobalResponse mediaGlobal) {
        this.mediaGlobal = mediaGlobal;
    }

    public MejorAsignaturaResponse getMejorAsignatura() {
        return mejorAsignatura;
    }

    public void setMejorAsignatura(MejorAsignaturaResponse mejorAsignatura) {
        this.mejorAsignatura = mejorAsignatura;
    }

    public PeorAsignaturaResponse getPeorAsignatura() {
        return peorAsignatura;
    }

    public void setPeorAsignatura(PeorAsignaturaResponse peorAsignatura) {
        this.peorAsignatura = peorAsignatura;
    }

    public MejorUnidadResponse getMejorUnidad() {
        return mejorUnidad;
    }

    public void setMejorUnidad(MejorUnidadResponse mejorUnidad) {
        this.mejorUnidad = mejorUnidad;
    }

    public PeorUnidadResponse getPeorUnidad() {
        return peorUnidad;
    }

    public void setPeorUnidad(PeorUnidadResponse peorUnidad) {
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
