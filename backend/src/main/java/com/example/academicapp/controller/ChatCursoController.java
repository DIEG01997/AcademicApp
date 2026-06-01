package com.example.academicapp.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.academicapp.dto.ChatCursoEstadoResponse;
import com.example.academicapp.dto.CrearMensajeChatRequest;
import com.example.academicapp.dto.MensajeChatResponse;
import com.example.academicapp.service.ChatCursoService;

@RestController
@RequestMapping("/api/chat/curso")
/**
 * Controlador REST que expone operaciones HTTP del backend para la app movil.
 */
public class ChatCursoController {

    private final ChatCursoService chatCursoService;

    public ChatCursoController(ChatCursoService chatCursoService) {
        this.chatCursoService = chatCursoService;
    }

    @GetMapping("/mensajes")
    public List<MensajeChatResponse> getMensajes(Authentication authentication) {
        return chatCursoService.getMensajes(authentication.getName());
    }

    @GetMapping("/estado")
    public ChatCursoEstadoResponse getEstado(Authentication authentication) {
        return chatCursoService.getEstado(authentication.getName());
    }

    @PatchMapping("/mensajes/vistos")
    public void marcarMensajesComoVistos(Authentication authentication) {
        chatCursoService.marcarMensajesComoVistos(authentication.getName());
    }

    @PostMapping("/mensajes")
    public MensajeChatResponse enviarMensaje(Authentication authentication, @RequestBody CrearMensajeChatRequest request) {
        return chatCursoService.enviarMensaje(authentication.getName(), request);
    }
}
