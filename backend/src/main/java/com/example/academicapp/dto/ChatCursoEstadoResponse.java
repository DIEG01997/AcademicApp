package com.example.academicapp.dto;

/**
 * DTO de salida que desacopla las entidades JPA del contrato publico de la API.
 */
public class ChatCursoEstadoResponse {

    private long mensajesNoLeidos;
    private Long ultimoMensajeId;

    public ChatCursoEstadoResponse(long mensajesNoLeidos, Long ultimoMensajeId) {
        this.mensajesNoLeidos = mensajesNoLeidos;
        this.ultimoMensajeId = ultimoMensajeId;
    }

    public long getMensajesNoLeidos() {
        return mensajesNoLeidos;
    }

    public void setMensajesNoLeidos(long mensajesNoLeidos) {
        this.mensajesNoLeidos = mensajesNoLeidos;
    }

    public Long getUltimoMensajeId() {
        return ultimoMensajeId;
    }

    public void setUltimoMensajeId(Long ultimoMensajeId) {
        this.ultimoMensajeId = ultimoMensajeId;
    }
}
