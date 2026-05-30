package com.example.academicapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.academicapp.entity.Asignatura;

public interface AsignaturaRepository extends JpaRepository<Asignatura, Long> {
}