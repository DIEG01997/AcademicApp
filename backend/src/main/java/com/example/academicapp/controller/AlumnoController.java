package com.example.academicapp.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.example.academicapp.dto.ActualizarNotaRequest;
import com.example.academicapp.dto.ActualizarPerfilRequest;
import com.example.academicapp.dto.AlumnoCursoResponse;
import com.example.academicapp.dto.AsignaturaResponse;
import com.example.academicapp.dto.CrearNotaRequest;
import com.example.academicapp.dto.DashboardResponse;
import com.example.academicapp.dto.MediaAsignaturaResponse;
import com.example.academicapp.dto.MediaGlobalResponse;
import com.example.academicapp.dto.MejorAsignaturaResponse;
import com.example.academicapp.dto.MejorUnidadResponse;
import com.example.academicapp.dto.NotaFinalUnidadResponse;
import com.example.academicapp.dto.NotaListadoResponse;
import com.example.academicapp.dto.NotaResponse;
import com.example.academicapp.dto.PeorAsignaturaResponse;
import com.example.academicapp.dto.PeorUnidadResponse;
import com.example.academicapp.dto.PerfilAlumnoResponse;
import com.example.academicapp.dto.PercentilAsignaturaResponse;
import com.example.academicapp.dto.PercentilGlobalResponse;
import com.example.academicapp.dto.PorcentajeSuspensosAsignaturaResponse;
import com.example.academicapp.dto.PorcentajeSuspensosAsignaturasResponse;
import com.example.academicapp.dto.UnidadDidacticaResponse;
import com.example.academicapp.service.AlumnoService;
import com.example.academicapp.service.EstadisticaService;
import com.example.academicapp.service.NotaService;

@RestController
@RequestMapping("/api/alumnos")
/**
 * Controlador REST que expone operaciones HTTP del backend para la app movil.
 */
public class AlumnoController {

    private final AlumnoService alumnoService;
    private final NotaService notaService;
    private final EstadisticaService estadisticaService;

    public AlumnoController(AlumnoService alumnoService,
                            NotaService notaService,
                            EstadisticaService estadisticaService) {
        this.alumnoService = alumnoService;
        this.notaService = notaService;
        this.estadisticaService = estadisticaService;
    }

    @GetMapping("/perfil")
    public PerfilAlumnoResponse getPerfil(Authentication authentication) {
        String correoEducativo = authentication.getName();
        return alumnoService.getPerfil(correoEducativo);
    }

    @PatchMapping("/perfil")
    public PerfilAlumnoResponse actualizarPerfil(
            Authentication authentication,
            @RequestBody ActualizarPerfilRequest request) {

        String correoEducativo = authentication.getName();
        return alumnoService.actualizarPerfil(correoEducativo, request);
    }

    @PatchMapping("/tutorial/completado")
    public void marcarTutorialCompletado(Authentication authentication) {
        String correoEducativo = authentication.getName();
        alumnoService.marcarTutorialCompletado(correoEducativo);
    }

    @DeleteMapping("/perfil")
    public void eliminarCuenta(Authentication authentication) {
        String correoEducativo = authentication.getName();
        alumnoService.eliminarCuenta(correoEducativo);
    }

    @GetMapping("/curso/companeros")
    public List<AlumnoCursoResponse> getCompanerosCurso(Authentication authentication) {
        String correoEducativo = authentication.getName();
        return alumnoService.getCompanerosCurso(correoEducativo);
    }

    @GetMapping("/asignaturas")
    public List<AsignaturaResponse> getAsignaturas(Authentication authentication) {
        String correoEducativo = authentication.getName();
        return alumnoService.getAsignaturas(correoEducativo);
    }

    @GetMapping("/asignaturas/{idAsignatura}/unidades")
    public List<UnidadDidacticaResponse> getUnidadesDidacticas(
            Authentication authentication,
            @PathVariable Long idAsignatura) {

        String correoEducativo = authentication.getName();
        return alumnoService.getUnidadesDidacticas(correoEducativo, idAsignatura);
    }

    @PostMapping("/notas")
    public NotaResponse crearNota(Authentication authentication, @RequestBody CrearNotaRequest request) {
        String correoEducativo = authentication.getName();
        return notaService.crearNota(correoEducativo, request);
    }

    @GetMapping("/notas")
    public List<NotaListadoResponse> getNotas(Authentication authentication) {
        String correoEducativo = authentication.getName();
        return notaService.getNotas(correoEducativo);
    }

    @DeleteMapping("/notas/{idNota}")
    public void eliminarNota(Authentication authentication, @PathVariable Long idNota) {
        String correoEducativo = authentication.getName();
        notaService.eliminarNota(correoEducativo, idNota);
    }

    @PutMapping("/notas/{idNota}")
    public NotaResponse actualizarNota(
            Authentication authentication,
            @PathVariable Long idNota,
            @RequestBody ActualizarNotaRequest request) {

        String correoEducativo = authentication.getName();
        return notaService.actualizarNota(correoEducativo, idNota, request);
    }

    @GetMapping("/unidades/{idUnidad}/nota-final")
    public NotaFinalUnidadResponse getNotaFinalUnidad(
            Authentication authentication,
            @PathVariable Long idUnidad) {

        String correoEducativo = authentication.getName();
        return notaService.getNotaFinalUnidad(correoEducativo, idUnidad);
    }

    @GetMapping("/asignaturas/{idAsignatura}/media")
    public MediaAsignaturaResponse getMediaAsignatura(
            Authentication authentication,
            @PathVariable Long idAsignatura) {

        String correoEducativo = authentication.getName();
        return estadisticaService.getMediaAsignatura(correoEducativo, idAsignatura);
    }

    @GetMapping("/media-global")
    public MediaGlobalResponse getMediaGlobal(Authentication authentication) {
        String correoEducativo = authentication.getName();
        return estadisticaService.getMediaGlobal(correoEducativo);
    }

    @GetMapping("/mejor-unidad")
    public MejorUnidadResponse getMejorUnidad(Authentication authentication) {
        String correoEducativo = authentication.getName();
        return estadisticaService.getMejorUnidad(correoEducativo);
    }

    @GetMapping("/peor-unidad")
    public PeorUnidadResponse getPeorUnidad(Authentication authentication) {
        String correoEducativo = authentication.getName();
        return estadisticaService.getPeorUnidad(correoEducativo);
    }

    @GetMapping("/mejor-asignatura")
    public MejorAsignaturaResponse getMejorAsignatura(Authentication authentication) {
        String correoEducativo = authentication.getName();
        return estadisticaService.getMejorAsignatura(correoEducativo);
    }

    @GetMapping("/peor-asignatura")
    public PeorAsignaturaResponse getPeorAsignatura(Authentication authentication) {
        String correoEducativo = authentication.getName();
        return estadisticaService.getPeorAsignatura(correoEducativo);
    }

    @GetMapping("/asignaturas/{idAsignatura}/porcentaje-suspensos")
    public PorcentajeSuspensosAsignaturaResponse getPorcentajeSuspensosAsignatura(
            Authentication authentication,
            @PathVariable Long idAsignatura) {

        String correoEducativo = authentication.getName();
        return estadisticaService.getPorcentajeSuspensosAsignatura(correoEducativo, idAsignatura);
    }

    @GetMapping("/porcentaje-suspensos-asignaturas")
    public PorcentajeSuspensosAsignaturasResponse getPorcentajeSuspensosAsignaturas(
            Authentication authentication) {

        String correoEducativo = authentication.getName();
        return estadisticaService.getPorcentajeSuspensosAsignaturas(correoEducativo);
    }

    @GetMapping("/dashboard")
    public DashboardResponse getDashboard(Authentication authentication) {
        String correoEducativo = authentication.getName();
        return estadisticaService.getDashboard(correoEducativo);
    }

    @GetMapping("/percentil/global")
    public PercentilGlobalResponse getPercentilGlobal(Authentication authentication) {
        String correoEducativo = authentication.getName();
        return estadisticaService.getPercentilGlobal(correoEducativo);
    }

    @GetMapping("/asignaturas/{idAsignatura}/percentil")
    public PercentilAsignaturaResponse getPercentilAsignatura(
            Authentication authentication,
            @PathVariable Long idAsignatura) {

        String correoEducativo = authentication.getName();
        return estadisticaService.getPercentilAsignatura(correoEducativo, idAsignatura);
    }
}
