package com.example.academicapp.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class NotaListadoResponse {

    private Long idNota;
    private Long idAsignatura;
    private String nombreAsignatura;
    private Long idUnidad;
    private String nombreUnidad;
    private Integer ordenUnidad;
    private Integer numeroEvaluacion;
    private Long idInstrumento;
    private String nombreInstrumento;
    private BigDecimal valorCalificacion;
    private BigDecimal ponderacion;
    private LocalDateTime fechaRegistro;

    public NotaListadoResponse() {
    }

    public NotaListadoResponse(Long idNota, Long idAsignatura, String nombreAsignatura,
                               Long idUnidad, String nombreUnidad, Integer ordenUnidad,
                               Integer numeroEvaluacion,
                               Long idInstrumento, String nombreInstrumento,
                               BigDecimal valorCalificacion, BigDecimal ponderacion,
                               LocalDateTime fechaRegistro) {
        this.idNota = idNota;
        this.idAsignatura = idAsignatura;
        this.nombreAsignatura = nombreAsignatura;
        this.idUnidad = idUnidad;
        this.nombreUnidad = nombreUnidad;
        this.ordenUnidad = ordenUnidad;
        this.numeroEvaluacion = numeroEvaluacion;
        this.idInstrumento = idInstrumento;
        this.nombreInstrumento = nombreInstrumento;
        this.valorCalificacion = valorCalificacion;
        this.ponderacion = ponderacion;
        this.fechaRegistro = fechaRegistro;
    }

    public Long getIdNota() {
        return idNota;
    }

    public void setIdNota(Long idNota) {
        this.idNota = idNota;
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

    public Integer getOrdenUnidad() {
        return ordenUnidad;
    }

    public void setOrdenUnidad(Integer ordenUnidad) {
        this.ordenUnidad = ordenUnidad;
    }

    public Integer getNumeroEvaluacion() {
        return numeroEvaluacion;
    }

    public void setNumeroEvaluacion(Integer numeroEvaluacion) {
        this.numeroEvaluacion = numeroEvaluacion;
    }

    public Long getIdInstrumento() {
        return idInstrumento;
    }

    public void setIdInstrumento(Long idInstrumento) {
        this.idInstrumento = idInstrumento;
    }

    public String getNombreInstrumento() {
        return nombreInstrumento;
    }

    public void setNombreInstrumento(String nombreInstrumento) {
        this.nombreInstrumento = nombreInstrumento;
    }

    public BigDecimal getValorCalificacion() {
        return valorCalificacion;
    }

    public void setValorCalificacion(BigDecimal valorCalificacion) {
        this.valorCalificacion = valorCalificacion;
    }

    public BigDecimal getPonderacion() {
        return ponderacion;
    }

    public void setPonderacion(BigDecimal ponderacion) {
        this.ponderacion = ponderacion;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }
}
