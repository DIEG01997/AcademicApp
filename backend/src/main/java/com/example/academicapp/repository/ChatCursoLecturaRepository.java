package com.example.academicapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.academicapp.entity.ChatCursoLectura;

/**
 * Repositorio Spring Data JPA que encapsula el acceso a datos de esta entidad.
 */
public interface ChatCursoLecturaRepository extends JpaRepository<ChatCursoLectura, Long> {
}
