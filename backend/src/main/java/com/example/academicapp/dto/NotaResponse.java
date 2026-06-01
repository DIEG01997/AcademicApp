package com.example.academicapp.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO de salida que desacopla las entidades JPA del contrato publico de la API.
 */
public class NotaResponse {

    private Long idNota;
    private Long idUnidad;
    private Long idInstrumento;
    private BigDecimal valorCalificacion;
    private BigDecimal ponderacion;
    private LocalDateTime fechaRegistro;

    public NotaResponse() {
    }

    public NotaResponse(Long idNota, Long idUnidad, Long idInstrumento,
                        BigDecimal valorCalificacion, BigDecimal ponderacion,
                        LocalDateTime fechaRegistro) {
        this.idNota = idNota;
        this.idUnidad = idUnidad;
        this.idInstrumento = idInstrumento;
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

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }
}