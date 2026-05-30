package com.example.academicapp.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "instrumento_evaluacion")
public class InstrumentoEvaluacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_instrumento")
    private Long idInstrumento;

    @Column(name = "nombre_instrumento", nullable = false, unique = true, length = 100)
    private String nombreInstrumento;

    public InstrumentoEvaluacion() {
    }

    public InstrumentoEvaluacion(Long idInstrumento, String nombreInstrumento) {
        this.idInstrumento = idInstrumento;
        this.nombreInstrumento = nombreInstrumento;
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
}