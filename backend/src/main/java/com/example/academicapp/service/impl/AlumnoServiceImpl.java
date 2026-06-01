package com.example.academicapp.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.academicapp.dto.ActualizarPerfilRequest;
import com.example.academicapp.dto.AlumnoCursoResponse;
import com.example.academicapp.dto.AsignaturaResponse;
import com.example.academicapp.dto.PerfilAlumnoResponse;
import com.example.academicapp.dto.UnidadDidacticaResponse;
import com.example.academicapp.entity.Alumno;
import com.example.academicapp.entity.UnidadDidactica;
import com.example.academicapp.repository.AlumnoRepository;
import com.example.academicapp.repository.NotaRepository;
import com.example.academicapp.repository.UnidadDidacticaRepository;
import com.example.academicapp.service.AlumnoService;

@Service
/**
 * Implementacion de la logica de negocio asociada a este servicio del backend.
 */
public class AlumnoServiceImpl implements AlumnoService {

    private final AlumnoRepository alumnoRepository;
    private final UnidadDidacticaRepository unidadDidacticaRepository;
    private final NotaRepository notaRepository;

    public AlumnoServiceImpl(AlumnoRepository alumnoRepository,
                             UnidadDidacticaRepository unidadDidacticaRepository,
                             NotaRepository notaRepository) {
        this.alumnoRepository = alumnoRepository;
        this.unidadDidacticaRepository = unidadDidacticaRepository;
        this.notaRepository = notaRepository;
    }

    private Alumno obtenerAlumno(String correoEducativo) {
        return alumnoRepository.findByCorreoEducativo(correoEducativo)
                .orElseThrow(() -> new RuntimeException("Alumno no encontrado"));
    }

    /*
     * El perfil se devuelve como DTO para no exponer directamente relaciones
     * JPA ni campos internos como el hash de password.
     */
    private PerfilAlumnoResponse construirPerfilAlumnoResponse(Alumno alumno) {
        return new PerfilAlumnoResponse(
                alumno.getIdAlumno(),
                alumno.getNombre(),
                alumno.getApellidos(),
                alumno.getCorreoEducativo(),
                alumno.getFechaNacimiento(),
                alumno.getTelefono(),
                alumno.getFotoPerfilBase64(),
                alumno.getCurso().getNumeroCurso(),
                alumno.getCurso().getCiclo().getSiglas(),
                alumno.getCurso().getCiclo().getNombreCiclo(),
                alumno.isTutorialCompletado()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PerfilAlumnoResponse getPerfil(String correoEducativo) {
        Alumno alumno = obtenerAlumno(correoEducativo);
        return construirPerfilAlumnoResponse(alumno);
    }

    @Override
    @Transactional
    public PerfilAlumnoResponse actualizarPerfil(String correoEducativo, ActualizarPerfilRequest request) {
        Alumno alumno = obtenerAlumno(correoEducativo);

        if (request.getNombre() != null && !request.getNombre().trim().isEmpty()) {
            alumno.setNombre(request.getNombre().trim());
        }

        if (request.getApellidos() != null && !request.getApellidos().trim().isEmpty()) {
            alumno.setApellidos(request.getApellidos().trim());
        }

        if (request.getTelefono() != null) {
            String telefono = request.getTelefono().trim();
            if (telefono.isEmpty()) {
                alumno.setTelefono(null);
            } else if (telefono.matches("^[0-9]{9}$")) {
                alumno.setTelefono(telefono);
            } else {
                throw new RuntimeException("El telefono debe estar vacio o contener exactamente 9 digitos");
            }
        }

        if (request.getFechaNacimiento() != null) {
            alumno.setFechaNacimiento(request.getFechaNacimiento());
        }

        if (request.getFotoPerfilBase64() != null) {
            String fotoPerfil = request.getFotoPerfilBase64().trim();
            alumno.setFotoPerfilBase64(fotoPerfil.isEmpty() ? null : fotoPerfil);
        }

        Alumno alumnoActualizado = alumnoRepository.save(alumno);
        return construirPerfilAlumnoResponse(alumnoActualizado);
    }

    @Override
    @Transactional
    public void marcarTutorialCompletado(String correoEducativo) {
        Alumno alumno = obtenerAlumno(correoEducativo);
        if (!alumno.isTutorialCompletado()) {
            alumno.setTutorialCompletado(true);
            alumnoRepository.save(alumno);
        }
    }

    @Override
    @Transactional
    public void eliminarCuenta(String correoEducativo) {
        Alumno alumno = obtenerAlumno(correoEducativo);

        notaRepository.deleteByAlumno_IdAlumno(alumno.getIdAlumno());
        alumnoRepository.delete(alumno);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AsignaturaResponse> getAsignaturas(String correoEducativo) {
        Alumno alumno = obtenerAlumno(correoEducativo);

        return alumno.getCurso().getAsignaturas().stream()
                .map(asignatura -> new AsignaturaResponse(
                        asignatura.getIdAsignatura(),
                        asignatura.getNombreAsignatura(),
                        asignatura.getDescripcion()
                ))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UnidadDidacticaResponse> getUnidadesDidacticas(String correoEducativo, Long idAsignatura) {
        Alumno alumno = obtenerAlumno(correoEducativo);

        // Evita que un alumno consulte unidades de asignaturas que no pertenecen
        // a su curso aunque conozca el identificador numerico.
        boolean perteneceAlCurso = alumno.getCurso().getAsignaturas()
                .stream()
                .anyMatch(asignatura -> asignatura.getIdAsignatura().equals(idAsignatura));

        if (!perteneceAlCurso) {
            throw new RuntimeException("La asignatura no pertenece al curso del alumno");
        }

        List<UnidadDidactica> unidades = unidadDidacticaRepository
                .findByAsignatura_IdAsignaturaOrderByOrdenUnidadAsc(idAsignatura);

        return unidades.stream()
                .map(unidad -> new UnidadDidacticaResponse(
                        unidad.getIdUnidad(),
                        unidad.getNombreUnidad(),
                        unidad.getDescripcion(),
                        unidad.getOrdenUnidad()
                ))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AlumnoCursoResponse> getCompanerosCurso(String correoEducativo) {
        Alumno alumno = obtenerAlumno(correoEducativo);
        return alumnoRepository.findByCurso_IdCursoOrderByApellidosAscNombreAsc(alumno.getCurso().getIdCurso()).stream()
                .map(companero -> new AlumnoCursoResponse(
                        companero.getIdAlumno(),
                        companero.getNombre(),
                        companero.getApellidos(),
                        companero.getFotoPerfilBase64()
                ))
                .toList();
    }
}
