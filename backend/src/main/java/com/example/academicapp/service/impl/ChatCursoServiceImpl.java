package com.example.academicapp.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.academicapp.dto.ChatCursoEstadoResponse;
import com.example.academicapp.dto.CrearMensajeChatRequest;
import com.example.academicapp.dto.MensajeChatResponse;
import com.example.academicapp.entity.Alumno;
import com.example.academicapp.entity.ChatCursoLectura;
import com.example.academicapp.entity.MensajeChatCurso;
import com.example.academicapp.repository.AlumnoRepository;
import com.example.academicapp.repository.ChatCursoLecturaRepository;
import com.example.academicapp.repository.MensajeChatCursoRepository;
import com.example.academicapp.service.ChatCursoService;

@Service
/**
 * Implementacion de la logica de negocio asociada a este servicio del backend.
 */
public class ChatCursoServiceImpl implements ChatCursoService {

    private final AlumnoRepository alumnoRepository;
    private final MensajeChatCursoRepository mensajeRepository;
    private final ChatCursoLecturaRepository lecturaRepository;

    public ChatCursoServiceImpl(AlumnoRepository alumnoRepository,
                                MensajeChatCursoRepository mensajeRepository,
                                ChatCursoLecturaRepository lecturaRepository) {
        this.alumnoRepository = alumnoRepository;
        this.mensajeRepository = mensajeRepository;
        this.lecturaRepository = lecturaRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<MensajeChatResponse> getMensajes(String correoEducativo) {
        Alumno alumno = obtenerAlumno(correoEducativo);
        return mensajeRepository.findByCursoAndFechaEnvioAfterRegistrationOrderByFechaEnvioAsc(
                        alumno.getCurso(),
                        alumno.getFechaRegistro()
                )
                .stream()
                .map(mensaje -> toResponse(mensaje, alumno))
                .toList();
    }

    @Override
    @Transactional
    public MensajeChatResponse enviarMensaje(String correoEducativo, CrearMensajeChatRequest request) {
        Alumno alumno = obtenerAlumno(correoEducativo);
        String contenido = request == null ? "" : request.getContenido();
        if (contenido == null || contenido.trim().isEmpty()) {
            throw new RuntimeException("El mensaje no puede estar vacío");
        }

        MensajeChatCurso mensaje = new MensajeChatCurso();
        mensaje.setAlumno(alumno);
        mensaje.setCurso(alumno.getCurso());
        mensaje.setContenido(contenido.trim());
        mensaje.setFechaEnvio(LocalDateTime.now());

        return toResponse(mensajeRepository.save(mensaje), alumno);
    }

    @Override
    @Transactional
    public ChatCursoEstadoResponse getEstado(String correoEducativo) {
        Alumno alumno = obtenerAlumno(correoEducativo);
        Long ultimoVisible = getUltimoMensajeVisible(alumno);
        ChatCursoLectura lectura = lecturaRepository.findById(alumno.getIdAlumno())
                .orElseGet(() -> inicializarLectura(alumno, ultimoVisible));

        long noLeidos = mensajeRepository.countUnreadVisibleForAlumno(
                alumno.getCurso(),
                alumno.getFechaRegistro(),
                lectura.getUltimoMensajeVistoId(),
                alumno
        );

        return new ChatCursoEstadoResponse(noLeidos, ultimoVisible == 0 ? null : ultimoVisible);
    }

    @Override
    @Transactional
    public void marcarMensajesComoVistos(String correoEducativo) {
        Alumno alumno = obtenerAlumno(correoEducativo);
        Long ultimoVisible = getUltimoMensajeVisible(alumno);
        ChatCursoLectura lectura = lecturaRepository.findById(alumno.getIdAlumno())
                .orElseGet(() -> inicializarLectura(alumno, 0L));

        if (ultimoVisible > lectura.getUltimoMensajeVistoId()) {
            lectura.setUltimoMensajeVistoId(ultimoVisible);
            lecturaRepository.save(lectura);
        }
    }

    private Alumno obtenerAlumno(String correoEducativo) {
        return alumnoRepository.findByCorreoEducativo(correoEducativo)
                .orElseThrow(() -> new RuntimeException("Alumno no encontrado"));
    }

    private Long getUltimoMensajeVisible(Alumno alumno) {
        Long ultimoVisible = mensajeRepository.findMaxIdVisibleForAlumno(alumno.getCurso(), alumno.getFechaRegistro());
        return ultimoVisible == null ? 0L : ultimoVisible;
    }

    private ChatCursoLectura inicializarLectura(Alumno alumno, Long ultimoMensajeVistoId) {
        ChatCursoLectura lectura = new ChatCursoLectura();
        lectura.setAlumno(alumno);
        lectura.setUltimoMensajeVistoId(ultimoMensajeVistoId == null ? 0L : ultimoMensajeVistoId);
        return lecturaRepository.save(lectura);
    }

    private MensajeChatResponse toResponse(MensajeChatCurso mensaje, Alumno alumnoActual) {
        Alumno autor = mensaje.getAlumno();
        return new MensajeChatResponse(
                mensaje.getIdMensaje(),
                autor.getIdAlumno(),
                autor.getNombre() + " " + autor.getApellidos(),
                autor.getFotoPerfilBase64(),
                mensaje.getContenido(),
                mensaje.getFechaEnvio(),
                autor.getIdAlumno().equals(alumnoActual.getIdAlumno())
        );
    }
}
