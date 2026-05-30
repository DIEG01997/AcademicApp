package com.example.academicapp.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.academicapp.dto.ActualizarNotaRequest;
import com.example.academicapp.dto.CrearNotaRequest;
import com.example.academicapp.dto.InstrumentoEvaluacionResponse;
import com.example.academicapp.dto.NotaFinalUnidadResponse;
import com.example.academicapp.dto.NotaListadoResponse;
import com.example.academicapp.dto.NotaResponse;
import com.example.academicapp.entity.Alumno;
import com.example.academicapp.entity.InstrumentoEvaluacion;
import com.example.academicapp.entity.Nota;
import com.example.academicapp.entity.UnidadDidactica;
import com.example.academicapp.repository.AlumnoRepository;
import com.example.academicapp.repository.InstrumentoEvaluacionRepository;
import com.example.academicapp.repository.NotaRepository;
import com.example.academicapp.repository.UnidadDidacticaRepository;
import com.example.academicapp.service.NotaService;

@Service
public class NotaServiceImpl implements NotaService {

    private final AlumnoRepository alumnoRepository;
    private final UnidadDidacticaRepository unidadDidacticaRepository;
    private final InstrumentoEvaluacionRepository instrumentoEvaluacionRepository;
    private final NotaRepository notaRepository;

    public NotaServiceImpl(AlumnoRepository alumnoRepository,
                           UnidadDidacticaRepository unidadDidacticaRepository,
                           InstrumentoEvaluacionRepository instrumentoEvaluacionRepository,
                           NotaRepository notaRepository) {
        this.alumnoRepository = alumnoRepository;
        this.unidadDidacticaRepository = unidadDidacticaRepository;
        this.instrumentoEvaluacionRepository = instrumentoEvaluacionRepository;
        this.notaRepository = notaRepository;
    }

    private Alumno obtenerAlumno(String correoEducativo) {
        return alumnoRepository.findByCorreoEducativo(correoEducativo)
                .orElseThrow(() -> new RuntimeException("Alumno no encontrado"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<InstrumentoEvaluacionResponse> getInstrumentosEvaluacion() {
        List<InstrumentoEvaluacion> instrumentos =
                instrumentoEvaluacionRepository.findAllByOrderByIdInstrumentoAsc();

        return instrumentos.stream()
                .map(instrumento -> new InstrumentoEvaluacionResponse(
                        instrumento.getIdInstrumento(),
                        instrumento.getNombreInstrumento()
                ))
                .toList();
    }

    @Override
    @Transactional
    public NotaResponse crearNota(String correoEducativo, CrearNotaRequest request) {
        Alumno alumno = obtenerAlumno(correoEducativo);

        UnidadDidactica unidad = unidadDidacticaRepository.findById(request.getIdUnidad())
                .orElseThrow(() -> new RuntimeException("La unidad didáctica no existe"));

        InstrumentoEvaluacion instrumento = instrumentoEvaluacionRepository.findById(request.getIdInstrumento())
                .orElseThrow(() -> new RuntimeException("El instrumento de evaluación no existe"));

        boolean unidadPerteneceAlCurso = alumno.getCurso().getAsignaturas()
                .stream()
                .anyMatch(asignatura -> asignatura.getIdAsignatura().equals(unidad.getAsignatura().getIdAsignatura()));

        if (!unidadPerteneceAlCurso) {
            throw new RuntimeException("La unidad didáctica no pertenece al curso del alumno");
        }

        notaRepository.findByAlumno_IdAlumnoAndUnidadDidactica_IdUnidadAndInstrumentoEvaluacion_IdInstrumento(
                alumno.getIdAlumno(),
                unidad.getIdUnidad(),
                instrumento.getIdInstrumento()
        ).ifPresent(n -> {
            throw new RuntimeException("Ya existe una nota para ese alumno, unidad e instrumento");
        });

        List<Nota> notasExistentes = notaRepository.findByAlumnoAndUnidadDidactica(alumno, unidad);

        if (notasExistentes.size() >= 3) {
            throw new RuntimeException("Ya existen las tres notas de instrumentos para esta unidad");
        }

        BigDecimal sumaActual = notasExistentes.stream()
                .map(Nota::getPonderacion)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal ponderacionFinal;

        if (notasExistentes.size() == 2) {
            BigDecimal ponderacionRestante = new BigDecimal("100.00").subtract(sumaActual);

            if (ponderacionRestante.compareTo(BigDecimal.ZERO) <= 0) {
                throw new RuntimeException("No queda ponderación disponible para esta unidad");
            }

            ponderacionFinal = ponderacionRestante;
        } else {
            ponderacionFinal = request.getPonderacion();

            BigDecimal nuevaSuma = sumaActual.add(ponderacionFinal);

            if (nuevaSuma.compareTo(new BigDecimal("100.00")) > 0) {
                throw new RuntimeException(
                        "La suma de ponderaciones supera 100. Actualmente tienes "
                                + sumaActual + " y estás intentando añadir "
                                + ponderacionFinal
                );
            }
        }

        Nota nota = new Nota();
        nota.setAlumno(alumno);
        nota.setUnidadDidactica(unidad);
        nota.setInstrumentoEvaluacion(instrumento);
        nota.setValorCalificacion(request.getValorCalificacion());
        nota.setPonderacion(ponderacionFinal);
        nota.setFechaRegistro(LocalDateTime.now(ZoneOffset.UTC));

        Nota notaGuardada = notaRepository.save(nota);

        return new NotaResponse(
                notaGuardada.getIdNota(),
                notaGuardada.getUnidadDidactica().getIdUnidad(),
                notaGuardada.getInstrumentoEvaluacion().getIdInstrumento(),
                notaGuardada.getValorCalificacion(),
                notaGuardada.getPonderacion(),
                notaGuardada.getFechaRegistro()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotaListadoResponse> getNotas(String correoEducativo) {
        Alumno alumno = obtenerAlumno(correoEducativo);

        List<Nota> notas = notaRepository.findByAlumno_IdAlumnoOrderByFechaRegistroDesc(alumno.getIdAlumno());

        return notas.stream()
                .map(nota -> new NotaListadoResponse(
                        nota.getIdNota(),
                        nota.getUnidadDidactica().getAsignatura().getIdAsignatura(),
                        nota.getUnidadDidactica().getAsignatura().getNombreAsignatura(),
                        nota.getUnidadDidactica().getIdUnidad(),
                        nota.getUnidadDidactica().getNombreUnidad(),
                        nota.getUnidadDidactica().getOrdenUnidad(),
                        nota.getUnidadDidactica().getEvaluacion().getNumeroEvaluacion(),
                        nota.getInstrumentoEvaluacion().getIdInstrumento(),
                        nota.getInstrumentoEvaluacion().getNombreInstrumento(),
                        nota.getValorCalificacion(),
                        nota.getPonderacion(),
                        nota.getFechaRegistro()
                ))
                .toList();
    }

    @Override
    @Transactional
    public void eliminarNota(String correoEducativo, Long idNota) {
        Alumno alumno = obtenerAlumno(correoEducativo);

        Nota nota = notaRepository.findById(idNota)
                .orElseThrow(() -> new RuntimeException("La nota no existe"));

        if (!nota.getAlumno().getIdAlumno().equals(alumno.getIdAlumno())) {
            throw new RuntimeException("No tienes permiso para eliminar esta nota");
        }

        notaRepository.delete(nota);
    }

    @Override
    @Transactional
    public NotaResponse actualizarNota(String correoEducativo, Long idNota, ActualizarNotaRequest request) {
        Alumno alumno = obtenerAlumno(correoEducativo);

        Nota nota = notaRepository.findById(idNota)
                .orElseThrow(() -> new RuntimeException("La nota no existe"));

        if (!nota.getAlumno().getIdAlumno().equals(alumno.getIdAlumno())) {
            throw new RuntimeException("No tienes permiso para editar esta nota");
        }

        List<Nota> notasDeLaUnidad = notaRepository.findByAlumnoAndUnidadDidactica(
                alumno,
                nota.getUnidadDidactica()
        );

        BigDecimal sumaOtrasPonderaciones = notasDeLaUnidad.stream()
                .filter(n -> !n.getIdNota().equals(nota.getIdNota()))
                .map(Nota::getPonderacion)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal nuevaSuma = sumaOtrasPonderaciones.add(request.getPonderacion());

        if (notasDeLaUnidad.size() == 3) {
            if (nuevaSuma.compareTo(new BigDecimal("100.00")) != 0) {
                throw new RuntimeException(
                        "En una unidad con 3 instrumentos, la suma de ponderaciones debe ser exactamente 100. "
                                + "Sin contar esta nota tienes " + sumaOtrasPonderaciones
                                + " y estás intentando dejar esta en " + request.getPonderacion()
                );
            }
        } else {
            if (nuevaSuma.compareTo(new BigDecimal("100.00")) > 0) {
                throw new RuntimeException(
                        "La suma de ponderaciones supera 100. Sin contar esta nota ya tienes "
                                + sumaOtrasPonderaciones + " y estás intentando dejar esta en "
                                + request.getPonderacion()
                );
            }
        }

        nota.setValorCalificacion(request.getValorCalificacion());
        nota.setPonderacion(request.getPonderacion());

        Nota notaActualizada = notaRepository.save(nota);

        return new NotaResponse(
                notaActualizada.getIdNota(),
                notaActualizada.getUnidadDidactica().getIdUnidad(),
                notaActualizada.getInstrumentoEvaluacion().getIdInstrumento(),
                notaActualizada.getValorCalificacion(),
                notaActualizada.getPonderacion(),
                notaActualizada.getFechaRegistro()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public NotaFinalUnidadResponse getNotaFinalUnidad(String correoEducativo, Long idUnidad) {
        Alumno alumno = obtenerAlumno(correoEducativo);

        UnidadDidactica unidad = unidadDidacticaRepository.findById(idUnidad)
                .orElseThrow(() -> new RuntimeException("La unidad didáctica no existe"));

        boolean pertenece = alumno.getCurso().getAsignaturas()
                .stream()
                .anyMatch(a -> a.getIdAsignatura().equals(unidad.getAsignatura().getIdAsignatura()));

        if (!pertenece) {
            throw new RuntimeException("La unidad no pertenece al curso del alumno");
        }

        List<Nota> notas = notaRepository.findByAlumnoAndUnidadDidactica(alumno, unidad);

        if (notas.isEmpty()) {
            return new NotaFinalUnidadResponse(
                    unidad.getIdUnidad(),
                    unidad.getNombreUnidad(),
                    BigDecimal.ZERO
            );
        }

        BigDecimal resultado = notas.stream()
                .map(n -> n.getValorCalificacion()
                        .multiply(n.getPonderacion())
                        .divide(new BigDecimal("100")))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new NotaFinalUnidadResponse(
                unidad.getIdUnidad(),
                unidad.getNombreUnidad(),
                resultado.setScale(2, RoundingMode.HALF_UP)
        );
    }
}
