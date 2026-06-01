package com.example.academicapp.api;

/**
 * DTO de respuesta recibido desde la API y usado por las pantallas Android.
 */
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
