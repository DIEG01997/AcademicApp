package com.example.academicapp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.academicapp.entity.InstrumentoEvaluacion;

/**
 * Repositorio Spring Data JPA que encapsula el acceso a datos de esta entidad.
 */
public interface InstrumentoEvaluacionRepository extends JpaRepository<InstrumentoEvaluacion, Long> {

    List<InstrumentoEvaluacion> findAllByOrderByIdInstrumentoAsc();
}