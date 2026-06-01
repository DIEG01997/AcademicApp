package com.example.academicapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.academicapp.entity.Evaluacion;

/**
 * Repositorio Spring Data JPA que encapsula el acceso a datos de esta entidad.
 */
public interface EvaluacionRepository extends JpaRepository<Evaluacion, Long> {
	
}

