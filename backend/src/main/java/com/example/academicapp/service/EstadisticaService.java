package com.example.academicapp.service;

import com.example.academicapp.dto.DashboardResponse;
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

/**
 * Interfaz de servicio que define las operaciones de negocio disponibles para los controladores.
 */
public interface EstadisticaService {

    MediaAsignaturaResponse getMediaAsignatura(String correoEducativo, Long idAsignatura);

    MediaGlobalResponse getMediaGlobal(String correoEducativo);

    MejorUnidadResponse getMejorUnidad(String correoEducativo);

    PeorUnidadResponse getPeorUnidad(String correoEducativo);

    MejorAsignaturaResponse getMejorAsignatura(String correoEducativo);

    PeorAsignaturaResponse getPeorAsignatura(String correoEducativo);

    PorcentajeSuspensosAsignaturaResponse getPorcentajeSuspensosAsignatura(String correoEducativo, Long idAsignatura);

    PorcentajeSuspensosAsignaturasResponse getPorcentajeSuspensosAsignaturas(String correoEducativo);

    DashboardResponse getDashboard(String correoEducativo);

    PercentilGlobalResponse getPercentilGlobal(String correoEducativo);

    PercentilAsignaturaResponse getPercentilAsignatura(String correoEducativo, Long idAsignatura);
}