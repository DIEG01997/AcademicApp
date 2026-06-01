package com.example.academicapp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.academicapp.entity.Curso;
import com.example.academicapp.entity.MensajeChatCurso;

/**
 * Repositorio Spring Data JPA que encapsula el acceso a datos de esta entidad.
 */
public interface MensajeChatCursoRepository extends JpaRepository<MensajeChatCurso, Long> {

    @Query("""
        SELECT m
        FROM MensajeChatCurso m
        JOIN FETCH m.alumno
        WHERE m.curso = :curso
          AND m.fechaEnvio >= :fechaRegistroAlumno
        ORDER BY m.fechaEnvio ASC
    """)
    List<MensajeChatCurso> findByCursoAndFechaEnvioAfterRegistrationOrderByFechaEnvioAsc(
            @Param("curso") Curso curso,
            @Param("fechaRegistroAlumno") java.time.LocalDateTime fechaRegistroAlumno
    );

    @Query("""
        SELECT COALESCE(MAX(m.idMensaje), 0)
        FROM MensajeChatCurso m
        WHERE m.curso = :curso
          AND m.fechaEnvio >= :fechaRegistroAlumno
    """)
    Long findMaxIdVisibleForAlumno(
            @Param("curso") Curso curso,
            @Param("fechaRegistroAlumno") java.time.LocalDateTime fechaRegistroAlumno
    );

    @Query("""
        SELECT COUNT(m)
        FROM MensajeChatCurso m
        WHERE m.curso = :curso
          AND m.fechaEnvio >= :fechaRegistroAlumno
          AND m.idMensaje > :ultimoMensajeVistoId
          AND m.alumno <> :alumno
    """)
    long countUnreadVisibleForAlumno(
            @Param("curso") Curso curso,
            @Param("fechaRegistroAlumno") java.time.LocalDateTime fechaRegistroAlumno,
            @Param("ultimoMensajeVistoId") Long ultimoMensajeVistoId,
            @Param("alumno") com.example.academicapp.entity.Alumno alumno
    );
}
