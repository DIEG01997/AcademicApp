package com.example.academicapp.service;

import java.util.List;

import com.example.academicapp.dto.ActualizarPerfilRequest;
import com.example.academicapp.dto.AlumnoCursoResponse;
import com.example.academicapp.dto.AsignaturaResponse;
import com.example.academicapp.dto.PerfilAlumnoResponse;
import com.example.academicapp.dto.UnidadDidacticaResponse;

public interface AlumnoService {

    PerfilAlumnoResponse getPerfil(String correoEducativo);

    PerfilAlumnoResponse actualizarPerfil(String correoEducativo, ActualizarPerfilRequest request);

    void marcarTutorialCompletado(String correoEducativo);

    void eliminarCuenta(String correoEducativo);

    List<AsignaturaResponse> getAsignaturas(String correoEducativo);

    List<UnidadDidacticaResponse> getUnidadesDidacticas(String correoEducativo, Long idAsignatura);

    List<AlumnoCursoResponse> getCompanerosCurso(String correoEducativo);
}
