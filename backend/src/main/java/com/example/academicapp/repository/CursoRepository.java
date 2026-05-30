package com.example.academicapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.academicapp.entity.Curso;

public interface CursoRepository extends JpaRepository<Curso, Long> {
}