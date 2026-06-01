package com.example.academicapp.service;

import java.util.List;

import com.example.academicapp.dto.ActualizarNotaRequest;
import com.example.academicapp.dto.CrearNotaRequest;
import com.example.academicapp.dto.InstrumentoEvaluacionResponse;
import com.example.academicapp.dto.NotaFinalUnidadResponse;
import com.example.academicapp.dto.NotaListadoResponse;
import com.example.academicapp.dto.NotaResponse;

/**
 * Interfaz de servicio que define las operaciones de negocio disponibles para los controladores.
 */
public interface NotaService {
    List<InstrumentoEvaluacionResponse> getInstrumentosEvaluacion();
    NotaResponse crearNota(String correoEducativo, CrearNotaRequest request);
    List<NotaListadoResponse> getNotas(String correoEducativo);
    void eliminarNota(String correoEducativo, Long idNota);
    NotaResponse actualizarNota(String correoEducativo, Long idNota, ActualizarNotaRequest request);
    NotaFinalUnidadResponse getNotaFinalUnidad(String correoEducativo, Long idUnidad);
}