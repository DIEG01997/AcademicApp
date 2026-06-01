package com.example.academicapp.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.academicapp.entity.Alumno;
import com.example.academicapp.entity.Nota;
import com.example.academicapp.entity.UnidadDidactica;

/**
 * Repositorio Spring Data JPA que encapsula el acceso a datos de esta entidad.
 */
public interface NotaRepository extends JpaRepository<Nota, Long> {

    Optional<Nota> findByAlumno_IdAlumnoAndUnidadDidactica_IdUnidadAndInstrumentoEvaluacion_IdInstrumento(
            Long idAlumno, Long idUnidad, Long idInstrumento);

    @Query("""
        SELECT COALESCE(SUM(n.ponderacion), 0)
        FROM Nota n
        WHERE n.alumno.idAlumno = :idAlumno
          AND n.unidadDidactica.idUnidad = :idUnidad
    """)
    BigDecimal sumaPonderacionesPorAlumnoYUnidad(@Param("idAlumno") Long idAlumno,
                                                 @Param("idUnidad") Long idUnidad);

    List<Nota> findByAlumnoAndUnidadDidactica(Alumno alumno, UnidadDidactica unidad);

    List<Nota> findByAlumno_IdAlumnoOrderByFechaRegistroDesc(Long idAlumno);

    void deleteByAlumno_IdAlumno(Long idAlumno);

    @Query("""
        SELECT n
        FROM Nota n
        JOIN FETCH n.unidadDidactica ud
        JOIN FETCH ud.asignatura
        WHERE n.alumno = :alumno
    """)
    List<Nota> findAllByAlumnoWithUnidadYAsignatura(@Param("alumno") Alumno alumno);

    @Query("""
        SELECT n
        FROM Nota n
        JOIN FETCH n.alumno
        JOIN FETCH n.unidadDidactica ud
        JOIN FETCH ud.asignatura
        WHERE n.alumno IN :alumnos
    """)
    List<Nota> findAllByAlumnoInWithUnidadYAsignatura(@Param("alumnos") List<Alumno> alumnos);
}