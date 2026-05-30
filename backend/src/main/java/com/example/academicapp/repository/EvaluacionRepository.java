package com.example.academicapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.academicapp.entity.Evaluacion;

public interface EvaluacionRepository extends JpaRepository<Evaluacion, Long> {
	
}

