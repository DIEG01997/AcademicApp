package com.example.academicapp.dto;

public class InstrumentoEvaluacionResponse {

    private Long idInstrumento;
    private String nombreInstrumento;

    public InstrumentoEvaluacionResponse() {
    }

    public InstrumentoEvaluacionResponse(Long idInstrumento, String nombreInstrumento) {
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