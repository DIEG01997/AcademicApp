package com.example.academicapp.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.academicapp.dto.DashboardAsignaturaResponse;
import com.example.academicapp.dto.DashboardResponse;
import com.example.academicapp.dto.DashboardTrimestreResponse;
import com.example.academicapp.dto.DashboardUnidadResponse;
import com.example.academicapp.dto.MediaAsignaturaResponse;
import com.example.academicapp.dto.MediaGlobalResponse;
import com.example.academicapp.dto.MejorAsignaturaResponse;
import com.example.academicapp.dto.MejorUnidadResponse;
import com.example.academicapp.dto.PeorAsignaturaResponse;
import com.example.academicapp.dto.PeorUnidadResponse;
import com.example.academicapp.dto.PercentilAsignaturaResponse;
import com.example.academicapp.dto.PercentilGlobalResponse;
import com.example.academicapp.dto.PorcentajeSuspensosAsignaturaResponse;
import com.example.academicapp.dto.PorcentajeSuspensosAsignaturasResponse;
import com.example.academicapp.entity.Alumno;
import com.example.academicapp.entity.Asignatura;
import com.example.academicapp.entity.Nota;
import com.example.academicapp.entity.UnidadDidactica;
import com.example.academicapp.repository.AlumnoRepository;
import com.example.academicapp.repository.NotaRepository;
import com.example.academicapp.repository.UnidadDidacticaRepository;
import com.example.academicapp.service.EstadisticaService;

@Service
public class EstadisticaServiceImpl implements EstadisticaService {

    private static final BigDecimal CERO = BigDecimal.ZERO;
    private static final BigDecimal CIEN = new BigDecimal("100");
    private static final BigDecimal NOTA_APROBADO = new BigDecimal("5.00");
    private static final int MINIMO_ALUMNOS_PERCENTIL = 10;

    private final AlumnoRepository alumnoRepository;
    private final UnidadDidacticaRepository unidadDidacticaRepository;
    private final NotaRepository notaRepository;

    public EstadisticaServiceImpl(AlumnoRepository alumnoRepository,
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

    private Asignatura obtenerAsignaturaDelCurso(Alumno alumno, Long idAsignatura) {
        return alumno.getCurso().getAsignaturas()
                .stream()
                .filter(a -> a.getIdAsignatura().equals(idAsignatura))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("La asignatura no pertenece al curso del alumno"));
    }

    private BigDecimal calcularNotaFinalUnidad(List<Nota> notasUnidad) {
        if (notasUnidad == null || notasUnidad.isEmpty()) {
            return null;
        }

        return notasUnidad.stream()
                .map(n -> n.getValorCalificacion()
                        .multiply(n.getPonderacion())
                        .divide(CIEN))
                .reduce(CERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calcularPorcentaje(int parte, int total) {
        return total == 0
                ? CERO
                : new BigDecimal(parte)
                        .multiply(CIEN)
                        .divide(new BigDecimal(total), 2, RoundingMode.HALF_UP);
    }

    private boolean esSuspenso(BigDecimal nota) {
        return nota.compareTo(NOTA_APROBADO) < 0;
    }

    private BigDecimal calcularPercentil(BigDecimal valorAlumno, List<BigDecimal> valoresComparacion) {
        long menores = valoresComparacion.stream()
                .filter(valor -> valor.compareTo(valorAlumno) < 0)
                .count();

        long iguales = valoresComparacion.stream()
                .filter(valor -> valor.compareTo(valorAlumno) == 0)
                .count();

        long mayores = valoresComparacion.stream()
                .filter(valor -> valor.compareTo(valorAlumno) > 0)
                .count();

        if (mayores == 0) {
            return new BigDecimal("100.00");
        }

        if (menores == 0) {
            return new BigDecimal("0.00");
        }

        BigDecimal numerador = new BigDecimal(menores)
                .add(new BigDecimal(iguales).multiply(new BigDecimal("0.5")));

        return numerador.multiply(CIEN)
                .divide(new BigDecimal(valoresComparacion.size()), 2, RoundingMode.HALF_UP);
    }

    private List<UnidadDidactica> obtenerUnidadesCurso(List<Asignatura> asignaturasCurso) {
        return asignaturasCurso.isEmpty()
                ? List.of()
                : unidadDidacticaRepository.findByAsignaturaInOrderByAsignatura_IdAsignaturaAscOrdenUnidadAsc(asignaturasCurso);
    }

    private BigDecimal calcularMediaAsignaturaDesdeNotas(
            Asignatura asignatura,
            List<UnidadDidactica> unidadesCurso,
            Map<Long, List<Nota>> notasPorUnidadId) {

        BigDecimal sumaNotasFinales = CERO;
        int unidadesComputadas = 0;

        for (UnidadDidactica unidad : unidadesCurso) {
            if (!unidad.getAsignatura().getIdAsignatura().equals(asignatura.getIdAsignatura())) {
                continue;
            }

            BigDecimal notaFinalUnidad = calcularNotaFinalUnidad(notasPorUnidadId.get(unidad.getIdUnidad()));
            if (notaFinalUnidad != null) {
                sumaNotasFinales = sumaNotasFinales.add(notaFinalUnidad);
                unidadesComputadas++;
            }
        }

        if (unidadesComputadas == 0) {
            return null;
        }

        return sumaNotasFinales.divide(new BigDecimal(unidadesComputadas), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal calcularMediaGlobalDesdeNotas(
            List<Asignatura> asignaturasCurso,
            List<UnidadDidactica> unidadesCurso,
            Map<Long, List<Nota>> notasPorUnidadId) {

        BigDecimal sumaMediasAsignaturas = CERO;
        int asignaturasComputadas = 0;

        for (Asignatura asignatura : asignaturasCurso) {
            BigDecimal mediaAsignatura = calcularMediaAsignaturaDesdeNotas(asignatura, unidadesCurso, notasPorUnidadId);
            if (mediaAsignatura != null) {
                sumaMediasAsignaturas = sumaMediasAsignaturas.add(mediaAsignatura);
                asignaturasComputadas++;
            }
        }

        if (asignaturasComputadas == 0) {
            return null;
        }

        return sumaMediasAsignaturas.divide(new BigDecimal(asignaturasComputadas), 2, RoundingMode.HALF_UP);
    }

    private Map<Long, Map<Long, List<Nota>>> agruparNotasPorAlumnoYUnidad(List<Nota> notas) {
        return notas.stream()
                .filter(nota -> nota.getAlumno() != null && nota.getUnidadDidactica() != null)
                .collect(Collectors.groupingBy(
                        nota -> nota.getAlumno().getIdAlumno(),
                        Collectors.groupingBy(nota -> nota.getUnidadDidactica().getIdUnidad())
                ));
    }

    private Map<Long, BigDecimal> calcularMediasGlobalesCurso(
            List<Alumno> alumnosCurso,
            List<Asignatura> asignaturasCurso,
            List<UnidadDidactica> unidadesCurso) {

        if (alumnosCurso.isEmpty()) {
            return Map.of();
        }

        Map<Long, Map<Long, List<Nota>>> notasPorAlumnoYUnidad = agruparNotasPorAlumnoYUnidad(
                notaRepository.findAllByAlumnoInWithUnidadYAsignatura(alumnosCurso)
        );

        Map<Long, BigDecimal> mediasPorAlumnoId = new LinkedHashMap<>();
        for (Alumno alumno : alumnosCurso) {
            Map<Long, List<Nota>> notasPorUnidadId = notasPorAlumnoYUnidad.getOrDefault(alumno.getIdAlumno(), Map.of());
            mediasPorAlumnoId.put(
                    alumno.getIdAlumno(),
                    calcularMediaGlobalDesdeNotas(asignaturasCurso, unidadesCurso, notasPorUnidadId)
            );
        }

        return mediasPorAlumnoId;
    }

    private Map<Long, BigDecimal> calcularMediasAsignaturaCurso(
            List<Alumno> alumnosCurso,
            Asignatura asignatura,
            List<UnidadDidactica> unidadesCurso) {

        if (alumnosCurso.isEmpty()) {
            return Map.of();
        }

        Map<Long, Map<Long, List<Nota>>> notasPorAlumnoYUnidad = agruparNotasPorAlumnoYUnidad(
                notaRepository.findAllByAlumnoInWithUnidadYAsignatura(alumnosCurso)
        );

        Map<Long, BigDecimal> mediasPorAlumnoId = new LinkedHashMap<>();
        for (Alumno alumno : alumnosCurso) {
            Map<Long, List<Nota>> notasPorUnidadId = notasPorAlumnoYUnidad.getOrDefault(alumno.getIdAlumno(), Map.of());
            mediasPorAlumnoId.put(
                    alumno.getIdAlumno(),
                    calcularMediaAsignaturaDesdeNotas(asignatura, unidadesCurso, notasPorUnidadId)
            );
        }

        return mediasPorAlumnoId;
    }
    private ContextoEstadisticas construirContexto(Alumno alumno) {
        List<Asignatura> asignaturasCurso = new ArrayList<>(alumno.getCurso().getAsignaturas());

        Map<Long, AcumuladorAsignatura> acumuladoresPorAsignatura = new LinkedHashMap<>();
        for (Asignatura asignatura : asignaturasCurso) {
            acumuladoresPorAsignatura.put(
                    asignatura.getIdAsignatura(),
                    new AcumuladorAsignatura(asignatura)
            );
        }

        List<UnidadDidactica> unidades = asignaturasCurso.isEmpty()
                ? List.of()
                : unidadDidacticaRepository.findByAsignaturaInOrderByAsignatura_IdAsignaturaAscOrdenUnidadAsc(asignaturasCurso);

        List<Nota> notasAlumno = notaRepository.findAllByAlumnoWithUnidadYAsignatura(alumno);

        Map<Long, List<Nota>> notasPorUnidadId = notasAlumno.stream()
                .filter(nota -> nota.getUnidadDidactica() != null)
                .collect(Collectors.groupingBy(nota -> nota.getUnidadDidactica().getIdUnidad()));

        ResultadoUnidad mejorUnidad = null;
        ResultadoUnidad peorUnidad = null;
        Map<Long, List<UnidadEstadistica>> unidadesPorAsignaturaId = new LinkedHashMap<>();

        for (UnidadDidactica unidad : unidades) {
            List<Nota> notasUnidad = notasPorUnidadId.get(unidad.getIdUnidad());
            BigDecimal notaFinalUnidad = calcularNotaFinalUnidad(notasUnidad);
            Long idAsignatura = unidad.getAsignatura().getIdAsignatura();

            unidadesPorAsignaturaId
                    .computeIfAbsent(idAsignatura, key -> new ArrayList<>())
                    .add(new UnidadEstadistica(unidad, notaFinalUnidad));

            if (notaFinalUnidad == null) {
                continue;
            }

            AcumuladorAsignatura acumulador = acumuladoresPorAsignatura.get(idAsignatura);

            if (acumulador != null) {
                acumulador.sumaNotasFinales = acumulador.sumaNotasFinales.add(notaFinalUnidad);
                acumulador.unidadesComputadas++;
                acumulador.unidadesEvaluadas++;

                if (esSuspenso(notaFinalUnidad)) {
                    acumulador.unidadesSuspensas++;
                }
            }

            ResultadoUnidad candidata = new ResultadoUnidad(unidad, unidad.getAsignatura(), notaFinalUnidad);

            if (mejorUnidad == null || notaFinalUnidad.compareTo(mejorUnidad.nota()) > 0) {
                mejorUnidad = candidata;
            }

            if (peorUnidad == null || notaFinalUnidad.compareTo(peorUnidad.nota()) < 0) {
                peorUnidad = candidata;
            }
        }

        List<EstadisticasAsignatura> estadisticasAsignaturas = new ArrayList<>();
        Map<Long, EstadisticasAsignatura> estadisticasPorAsignaturaId = new LinkedHashMap<>();

        for (Asignatura asignatura : asignaturasCurso) {
            AcumuladorAsignatura acumulador = acumuladoresPorAsignatura.get(asignatura.getIdAsignatura());

            BigDecimal media = acumulador.unidadesComputadas == 0
                    ? CERO
                    : acumulador.sumaNotasFinales.divide(
                            new BigDecimal(acumulador.unidadesComputadas),
                            2,
                            RoundingMode.HALF_UP
                    );

            BigDecimal porcentajeSuspensos = calcularPorcentaje(
                    acumulador.unidadesSuspensas,
                    acumulador.unidadesEvaluadas
            );

            EstadisticasAsignatura estadisticas = new EstadisticasAsignatura(
                    acumulador.asignatura,
                    media,
                    acumulador.unidadesComputadas,
                    acumulador.unidadesEvaluadas,
                    acumulador.unidadesSuspensas,
                    porcentajeSuspensos
            );

            estadisticasAsignaturas.add(estadisticas);
            estadisticasPorAsignaturaId.put(asignatura.getIdAsignatura(), estadisticas);
        }

        return new ContextoEstadisticas(
                estadisticasAsignaturas,
                estadisticasPorAsignaturaId,
                unidadesPorAsignaturaId,
                mejorUnidad,
                peorUnidad
        );
    }

    private MediaGlobalResponse construirMediaGlobal(ContextoEstadisticas contexto) {
        BigDecimal sumaMediasAsignaturas = CERO;
        int asignaturasComputadas = 0;

        for (EstadisticasAsignatura estadisticas : contexto.estadisticasAsignaturas()) {
            if (estadisticas.unidadesComputadas() > 0) {
                sumaMediasAsignaturas = sumaMediasAsignaturas.add(estadisticas.media());
                asignaturasComputadas++;
            }
        }

        BigDecimal mediaGlobal = asignaturasComputadas == 0
                ? CERO
                : sumaMediasAsignaturas.divide(new BigDecimal(asignaturasComputadas), 2, RoundingMode.HALF_UP);

        return new MediaGlobalResponse(mediaGlobal, asignaturasComputadas);
    }

    private MejorAsignaturaResponse construirMejorAsignatura(ContextoEstadisticas contexto) {
        EstadisticasAsignatura mejor = null;

        for (EstadisticasAsignatura estadisticas : contexto.estadisticasAsignaturas()) {
            if (estadisticas.unidadesComputadas() == 0) {
                continue;
            }

            if (mejor == null || estadisticas.media().compareTo(mejor.media()) > 0) {
                mejor = estadisticas;
            }
        }

        if (mejor == null) {
            return null;
        }

        return new MejorAsignaturaResponse(
                mejor.asignatura().getIdAsignatura(),
                mejor.asignatura().getNombreAsignatura(),
                mejor.media(),
                mejor.unidadesComputadas()
        );
    }

    private PeorAsignaturaResponse construirPeorAsignatura(ContextoEstadisticas contexto) {
        EstadisticasAsignatura peor = null;

        for (EstadisticasAsignatura estadisticas : contexto.estadisticasAsignaturas()) {
            if (estadisticas.unidadesComputadas() == 0) {
                continue;
            }

            if (peor == null || estadisticas.media().compareTo(peor.media()) < 0) {
                peor = estadisticas;
            }
        }

        if (peor == null) {
            return null;
        }

        return new PeorAsignaturaResponse(
                peor.asignatura().getIdAsignatura(),
                peor.asignatura().getNombreAsignatura(),
                peor.media(),
                peor.unidadesComputadas()
        );
    }

    private MejorUnidadResponse construirMejorUnidad(ContextoEstadisticas contexto) {
        ResultadoUnidad mejorUnidad = contexto.mejorUnidad();

        if (mejorUnidad == null) {
            return null;
        }

        return new MejorUnidadResponse(
                mejorUnidad.unidad().getIdUnidad(),
                mejorUnidad.unidad().getNombreUnidad(),
                mejorUnidad.asignatura().getIdAsignatura(),
                mejorUnidad.asignatura().getNombreAsignatura(),
                mejorUnidad.nota()
        );
    }

    private PeorUnidadResponse construirPeorUnidad(ContextoEstadisticas contexto) {
        ResultadoUnidad peorUnidad = contexto.peorUnidad();

        if (peorUnidad == null) {
            return null;
        }

        return new PeorUnidadResponse(
                peorUnidad.unidad().getIdUnidad(),
                peorUnidad.unidad().getNombreUnidad(),
                peorUnidad.asignatura().getIdAsignatura(),
                peorUnidad.asignatura().getNombreAsignatura(),
                peorUnidad.nota()
        );
    }

    private PorcentajeSuspensosAsignaturasResponse construirPorcentajeSuspensosAsignaturas(ContextoEstadisticas contexto) {
        int asignaturasEvaluadas = 0;
        int asignaturasSuspensas = 0;

        for (EstadisticasAsignatura estadisticas : contexto.estadisticasAsignaturas()) {
            if (estadisticas.unidadesComputadas() > 0) {
                asignaturasEvaluadas++;

                if (esSuspenso(estadisticas.media())) {
                    asignaturasSuspensas++;
                }
            }
        }

        BigDecimal porcentajeSuspensos = calcularPorcentaje(asignaturasSuspensas, asignaturasEvaluadas);

        return new PorcentajeSuspensosAsignaturasResponse(
                asignaturasEvaluadas,
                asignaturasSuspensas,
                porcentajeSuspensos
        );
    }

    private List<DashboardAsignaturaResponse> construirResumenAsignaturas(ContextoEstadisticas contexto) {
        return contexto.estadisticasAsignaturas().stream()
                .map(estadisticas -> new DashboardAsignaturaResponse(
                        estadisticas.asignatura().getIdAsignatura(),
                        estadisticas.asignatura().getNombreAsignatura(),
                        estadisticas.media(),
                        estadisticas.unidadesComputadas(),
                        estadisticas.unidadesEvaluadas(),
                        estadisticas.unidadesSuspensas(),
                        estadisticas.porcentajeSuspensos(),
                        construirResumenUnidades(contexto, estadisticas.asignatura().getIdAsignatura())
                ))
                .toList();
    }

    private List<DashboardUnidadResponse> construirResumenUnidades(ContextoEstadisticas contexto, Long idAsignatura) {
        return contexto.unidadesPorAsignaturaId()
                .getOrDefault(idAsignatura, List.of())
                .stream()
                .map(unidad -> new DashboardUnidadResponse(
                        unidad.unidad().getIdUnidad(),
                        unidad.unidad().getNombreUnidad(),
                        unidad.unidad().getOrdenUnidad(),
                        unidad.unidad().getEvaluacion() == null ? null : unidad.unidad().getEvaluacion().getNumeroEvaluacion(),
                        unidad.nota()
                ))
                .toList();
    }

    private List<DashboardTrimestreResponse> construirEvolucionTrimestres(ContextoEstadisticas contexto) {
        Map<Integer, Map<Long, AcumuladorTrimestreAsignatura>> acumuladores = new LinkedHashMap<>();
        Map<Integer, String> nombresEvaluacion = new LinkedHashMap<>();
        Map<Integer, java.time.LocalDate> fechasInicioEvaluacion = new LinkedHashMap<>();
        Map<Integer, java.time.LocalDate> fechasFinEvaluacion = new LinkedHashMap<>();

        contexto.unidadesPorAsignaturaId().forEach((idAsignatura, unidadesAsignatura) -> {
            for (UnidadEstadistica unidad : unidadesAsignatura) {
                if (unidad.unidad().getEvaluacion() == null) {
                    continue;
                }

                Integer numeroEvaluacion = unidad.unidad().getEvaluacion().getNumeroEvaluacion();
                nombresEvaluacion.putIfAbsent(numeroEvaluacion, unidad.unidad().getEvaluacion().getNombreEvaluacion());
                fechasInicioEvaluacion.putIfAbsent(numeroEvaluacion, unidad.unidad().getEvaluacion().getFechaInicio());
                fechasFinEvaluacion.putIfAbsent(numeroEvaluacion, unidad.unidad().getEvaluacion().getFechaFin());

                if (unidad.nota() == null) {
                    continue;
                }

                acumuladores
                        .computeIfAbsent(numeroEvaluacion, key -> new LinkedHashMap<>())
                        .computeIfAbsent(idAsignatura, key -> new AcumuladorTrimestreAsignatura())
                        .add(unidad.nota());
            }
        });

        return nombresEvaluacion.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> {
                    Map<Long, AcumuladorTrimestreAsignatura> asignaturas = acumuladores.getOrDefault(entry.getKey(), Map.of());
                    BigDecimal sumaMedias = CERO;
                    int asignaturasComputadas = 0;

                    for (AcumuladorTrimestreAsignatura acumulador : asignaturas.values()) {
                        if (acumulador.unidadesComputadas == 0) {
                            continue;
                        }

                        sumaMedias = sumaMedias.add(acumulador.media());
                        asignaturasComputadas++;
                    }

                    BigDecimal media = asignaturasComputadas == 0
                            ? null
                            : sumaMedias.divide(new BigDecimal(asignaturasComputadas), 2, RoundingMode.HALF_UP);

                    return new DashboardTrimestreResponse(
                            entry.getKey(),
                            entry.getValue(),
                            fechasInicioEvaluacion.get(entry.getKey()),
                            fechasFinEvaluacion.get(entry.getKey()),
                            media,
                            asignaturasComputadas
                    );
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public MediaAsignaturaResponse getMediaAsignatura(String correoEducativo, Long idAsignatura) {
        Alumno alumno = obtenerAlumno(correoEducativo);
        Asignatura asignatura = obtenerAsignaturaDelCurso(alumno, idAsignatura);

        ContextoEstadisticas contexto = construirContexto(alumno);
        EstadisticasAsignatura estadisticas = contexto.estadisticasPorAsignaturaId().get(asignatura.getIdAsignatura());

        return new MediaAsignaturaResponse(
                asignatura.getIdAsignatura(),
                asignatura.getNombreAsignatura(),
                estadisticas.media(),
                estadisticas.unidadesComputadas()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public MediaGlobalResponse getMediaGlobal(String correoEducativo) {
        Alumno alumno = obtenerAlumno(correoEducativo);
        ContextoEstadisticas contexto = construirContexto(alumno);
        return construirMediaGlobal(contexto);
    }

    @Override
    @Transactional(readOnly = true)
    public MejorUnidadResponse getMejorUnidad(String correoEducativo) {
        Alumno alumno = obtenerAlumno(correoEducativo);
        ContextoEstadisticas contexto = construirContexto(alumno);
        return construirMejorUnidad(contexto);
    }

    @Override
    @Transactional(readOnly = true)
    public PeorUnidadResponse getPeorUnidad(String correoEducativo) {
        Alumno alumno = obtenerAlumno(correoEducativo);
        ContextoEstadisticas contexto = construirContexto(alumno);
        return construirPeorUnidad(contexto);
    }

    @Override
    @Transactional(readOnly = true)
    public MejorAsignaturaResponse getMejorAsignatura(String correoEducativo) {
        Alumno alumno = obtenerAlumno(correoEducativo);
        ContextoEstadisticas contexto = construirContexto(alumno);
        return construirMejorAsignatura(contexto);
    }

    @Override
    @Transactional(readOnly = true)
    public PeorAsignaturaResponse getPeorAsignatura(String correoEducativo) {
        Alumno alumno = obtenerAlumno(correoEducativo);
        ContextoEstadisticas contexto = construirContexto(alumno);
        return construirPeorAsignatura(contexto);
    }

    @Override
    @Transactional(readOnly = true)
    public PorcentajeSuspensosAsignaturaResponse getPorcentajeSuspensosAsignatura(String correoEducativo, Long idAsignatura) {
        Alumno alumno = obtenerAlumno(correoEducativo);
        Asignatura asignatura = obtenerAsignaturaDelCurso(alumno, idAsignatura);

        ContextoEstadisticas contexto = construirContexto(alumno);
        EstadisticasAsignatura estadisticas = contexto.estadisticasPorAsignaturaId().get(asignatura.getIdAsignatura());

        return new PorcentajeSuspensosAsignaturaResponse(
                asignatura.getIdAsignatura(),
                asignatura.getNombreAsignatura(),
                estadisticas.unidadesEvaluadas(),
                estadisticas.unidadesSuspensas(),
                estadisticas.porcentajeSuspensos()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PorcentajeSuspensosAsignaturasResponse getPorcentajeSuspensosAsignaturas(String correoEducativo) {
        Alumno alumno = obtenerAlumno(correoEducativo);
        ContextoEstadisticas contexto = construirContexto(alumno);
        return construirPorcentajeSuspensosAsignaturas(contexto);
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardResponse getDashboard(String correoEducativo) {
        Alumno alumno = obtenerAlumno(correoEducativo);
        ContextoEstadisticas contexto = construirContexto(alumno);

        return new DashboardResponse(
                construirMediaGlobal(contexto),
                construirMejorAsignatura(contexto),
                construirPeorAsignatura(contexto),
                construirMejorUnidad(contexto),
                construirPeorUnidad(contexto),
                construirPorcentajeSuspensosAsignaturas(contexto),
                construirResumenAsignaturas(contexto),
                construirEvolucionTrimestres(contexto)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PercentilGlobalResponse getPercentilGlobal(String correoEducativo) {
        Alumno alumnoActual = obtenerAlumno(correoEducativo);
        List<Asignatura> asignaturasCurso = new ArrayList<>(alumnoActual.getCurso().getAsignaturas());
        List<UnidadDidactica> unidadesCurso = obtenerUnidadesCurso(asignaturasCurso);
        List<Alumno> alumnosCurso = alumnoRepository.findByCurso_IdCurso(alumnoActual.getCurso().getIdCurso());

        Map<Long, BigDecimal> mediasPorAlumnoId = calcularMediasGlobalesCurso(
                alumnosCurso,
                asignaturasCurso,
                unidadesCurso
        );

        BigDecimal mediaAlumnoActual = mediasPorAlumnoId.get(alumnoActual.getIdAlumno());
        if (mediaAlumnoActual == null) {
            return new PercentilGlobalResponse(
                    false,
                    "El alumno no tiene datos suficientes para calcular su media global",
                    null,
                    0,
                    MINIMO_ALUMNOS_PERCENTIL
            );
        }

        List<BigDecimal> mediasValidas = mediasPorAlumnoId.values()
                .stream()
                .filter(media -> media != null)
                .toList();

        if (mediasValidas.size() < MINIMO_ALUMNOS_PERCENTIL) {
            return new PercentilGlobalResponse(
                    false,
                    "No hay suficientes alumnos con datos validos en el curso para calcular el percentil global",
                    null,
                    mediasValidas.size(),
                    MINIMO_ALUMNOS_PERCENTIL
            );
        }

        BigDecimal percentil = calcularPercentil(mediaAlumnoActual, mediasValidas);

        return new PercentilGlobalResponse(
                true,
                "Percentil global calculado correctamente",
                percentil,
                mediasValidas.size(),
                MINIMO_ALUMNOS_PERCENTIL
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PercentilAsignaturaResponse getPercentilAsignatura(String correoEducativo, Long idAsignatura) {
        Alumno alumnoActual = obtenerAlumno(correoEducativo);
        Asignatura asignatura = obtenerAsignaturaDelCurso(alumnoActual, idAsignatura);
        List<UnidadDidactica> unidadesCurso = unidadDidacticaRepository
                .findByAsignatura_IdAsignaturaOrderByOrdenUnidadAsc(asignatura.getIdAsignatura());
        List<Alumno> alumnosCurso = alumnoRepository.findByCurso_IdCurso(alumnoActual.getCurso().getIdCurso());

        Map<Long, BigDecimal> mediasPorAlumnoId = calcularMediasAsignaturaCurso(
                alumnosCurso,
                asignatura,
                unidadesCurso
        );

        BigDecimal mediaAlumnoActual = mediasPorAlumnoId.get(alumnoActual.getIdAlumno());
        if (mediaAlumnoActual == null) {
            return new PercentilAsignaturaResponse(
                    false,
                    "El alumno no tiene datos suficientes para calcular su percentil en esta asignatura",
                    asignatura.getIdAsignatura(),
                    asignatura.getNombreAsignatura(),
                    null,
                    0,
                    MINIMO_ALUMNOS_PERCENTIL
            );
        }

        List<BigDecimal> mediasValidas = mediasPorAlumnoId.values()
                .stream()
                .filter(media -> media != null)
                .toList();

        if (mediasValidas.size() < MINIMO_ALUMNOS_PERCENTIL) {
            return new PercentilAsignaturaResponse(
                    false,
                    "No hay suficientes alumnos con datos validos en el curso para calcular el percentil de esta asignatura",
                    asignatura.getIdAsignatura(),
                    asignatura.getNombreAsignatura(),
                    null,
                    mediasValidas.size(),
                    MINIMO_ALUMNOS_PERCENTIL
            );
        }

        BigDecimal percentil = calcularPercentil(mediaAlumnoActual, mediasValidas);

        return new PercentilAsignaturaResponse(
                true,
                "Percentil de asignatura calculado correctamente",
                asignatura.getIdAsignatura(),
                asignatura.getNombreAsignatura(),
                percentil,
                mediasValidas.size(),
                MINIMO_ALUMNOS_PERCENTIL
        );
    }
    private static class AcumuladorAsignatura {
        private final Asignatura asignatura;
        private BigDecimal sumaNotasFinales = CERO;
        private int unidadesComputadas = 0;
        private int unidadesEvaluadas = 0;
        private int unidadesSuspensas = 0;

        private AcumuladorAsignatura(Asignatura asignatura) {
            this.asignatura = asignatura;
        }
    }

    private static class AcumuladorTrimestreAsignatura {
        private BigDecimal sumaNotas = CERO;
        private int unidadesComputadas = 0;

        private void add(BigDecimal nota) {
            sumaNotas = sumaNotas.add(nota);
            unidadesComputadas++;
        }

        private BigDecimal media() {
            return sumaNotas.divide(new BigDecimal(unidadesComputadas), 2, RoundingMode.HALF_UP);
        }
    }

    private record EstadisticasAsignatura(
            Asignatura asignatura,
            BigDecimal media,
            int unidadesComputadas,
            int unidadesEvaluadas,
            int unidadesSuspensas,
            BigDecimal porcentajeSuspensos
    ) {
    }

    private record ResultadoUnidad(
            UnidadDidactica unidad,
            Asignatura asignatura,
            BigDecimal nota
    ) {
    }

    private record UnidadEstadistica(
            UnidadDidactica unidad,
            BigDecimal nota
    ) {
    }

    private record ContextoEstadisticas(
            List<EstadisticasAsignatura> estadisticasAsignaturas,
            Map<Long, EstadisticasAsignatura> estadisticasPorAsignaturaId,
            Map<Long, List<UnidadEstadistica>> unidadesPorAsignaturaId,
            ResultadoUnidad mejorUnidad,
            ResultadoUnidad peorUnidad
    ) {
    }
}
