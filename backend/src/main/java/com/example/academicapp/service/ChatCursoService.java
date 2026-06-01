package com.example.academicapp.service;

import java.util.List;

import com.example.academicapp.dto.ChatCursoEstadoResponse;
import com.example.academicapp.dto.CrearMensajeChatRequest;
import com.example.academicapp.dto.MensajeChatResponse;

/**
 * Interfaz de servicio que define las operaciones de negocio disponibles para los controladores.
 */
public interface ChatCursoService {

    List<MensajeChatResponse> getMensajes(String correoEducativo);

    MensajeChatResponse enviarMensaje(String correoEducativo, CrearMensajeChatRequest request);

    ChatCursoEstadoResponse getEstado(String correoEducativo);

    void marcarMensajesComoVistos(String correoEducativo);
}
