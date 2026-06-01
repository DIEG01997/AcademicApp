package com.example.academicapp.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.academicapp.entity.Alumno;

/**
 * Repositorio Spring Data JPA que encapsula el acceso a datos de esta entidad.
 */
public interface AlumnoRepository extends JpaRepository<Alumno, Long> {

    Optional<Alumno> findByCorreoEducativo(String correoEducativo);

    boolean existsByCorreoEducativo(String correoEducativo);

    List<Alumno> findByCurso_IdCurso(Long idCurso);

    List<Alumno> findByCurso_IdCursoOrderByApellidosAscNombreAsc(Long idCurso);
}
