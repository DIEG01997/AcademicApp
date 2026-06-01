package com.example.academicapp.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

/**
 * DTO de entrada usado para validar y transportar datos recibidos desde la app movil.
 */
public class ActualizarNotaRequest {

    @NotNull
    @DecimalMin(value = "0.00", message = "La calificación no puede ser menor que 0")
    @DecimalMax(value = "10.00", message = "La calificación no puede ser mayor que 10")
    private BigDecimal valorCalificacion;

    @NotNull
    @DecimalMin(value = "0.00", message = "La ponderación no puede ser menor que 0")
    @DecimalMax(value = "100.00", message = "La ponderación no puede ser mayor que 100")
    private BigDecimal ponderacion;

    public ActualizarNotaRequest() {
    }

    public ActualizarNotaRequest(BigDecimal valorCalificacion, BigDecimal ponderacion) {
        this.valorCalificacion = valorCalificacion;
        this.ponderacion = ponderacion;
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