package com.example.academicapp.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public class CrearNotaRequest {

    @NotNull
    private Long idUnidad;

    @NotNull
    private Long idInstrumento;

    @NotNull
    @DecimalMin(value = "0.00", message = "La calificación no puede ser menor que 0")
    @DecimalMax(value = "10.00", message = "La calificación no puede ser mayor que 10")
    private BigDecimal valorCalificacion;

    @NotNull
    @DecimalMin(value = "0.00", message = "La ponderación no puede ser menor que 0")
    @DecimalMax(value = "100.00", message = "La ponderación no puede ser mayor que 100")
    private BigDecimal ponderacion;

    public CrearNotaRequest() {
    }

    public CrearNotaRequest(Long idUnidad, Long idInstrumento, BigDecimal valorCalificacion, BigDecimal ponderacion) {
        this.idUnidad = idUnidad;
        this.idInstrumento = idInstrumento;
        this.valorCalificacion = valorCalificacion;
        this.ponderacion = ponderacion;
    }

    public Long getIdUnidad() {
        return idUnidad;
    }

    public void setIdUnidad(Long idUnidad) {
        this.idUnidad = idUnidad;
    }

    public Long getIdInstrumento() {
        return idInstrumento;
    }

    public void setIdInstrumento(Long idInstrumento) {
        this.idInstrumento = idInstrumento;
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
}