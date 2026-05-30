package com.example.academicapp.dto;

import java.math.BigDecimal;

public class MediaGlobalResponse {

    private BigDecimal mediaGlobal;
    private Integer asignaturasComputadas;

    public MediaGlobalResponse() {
    }

    public MediaGlobalResponse(BigDecimal mediaGlobal, Integer asignaturasComputadas) {
        this.mediaGlobal = mediaGlobal;
        this.asignaturasComputadas = asignaturasComputadas;
    }

    public BigDecimal getMediaGlobal() {
        return mediaGlobal;
    }

    public void setMediaGlobal(BigDecimal mediaGlobal) {
        this.mediaGlobal = mediaGlobal;
    }

    public Integer getAsignaturasComputadas() {
        return asignaturasComputadas;
    }

    public void setAsignaturasComputadas(Integer asignaturasComputadas) {
        this.asignaturasComputadas = asignaturasComputadas;
    }
}