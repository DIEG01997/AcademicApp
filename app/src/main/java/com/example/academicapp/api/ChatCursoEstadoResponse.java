package com.example.academicapp.api;

public class ChatCursoEstadoResponse {

    private long mensajesNoLeidos;
    private Long ultimoMensajeId;

    public long getMensajesNoLeidos() {
        return mensajesNoLeidos;
    }

    public Long getUltimoMensajeId() {
        return ultimoMensajeId;
    }
}
