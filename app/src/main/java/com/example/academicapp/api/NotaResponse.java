package com.example.academicapp.api;

/**
 * DTO de respuesta recibido desde la API y usado por las pantallas Android.
 */
public class NotaResponse {

    private Long idNota;
    private Long idUnidad;
    private Long idInstrumento;
    private String valorCalificacion;
    private String ponderacion;
    private String fechaRegistro;

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

    public String getValorCalificacion() {
        return valorCalificacion;
    }

    public void setValorCalificacion(String valorCalificacion) {
        this.valorCalificacion = valorCalificacion;
    }

    public String getPonderacion() {
        return ponderacion;
    }

    public void setPonderacion(String ponderacion) {
        this.ponderacion = ponderacion;
    }

    public String getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(String fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }
}
