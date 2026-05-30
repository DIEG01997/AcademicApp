package com.example.academicapp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.academicapp.entity.Asignatura;
import com.example.academicapp.entity.UnidadDidactica;

@Repository
public interface UnidadDidacticaRepository extends JpaRepository<UnidadDidactica, Long> {

    List<UnidadDidactica> findByAsignatura_IdAsignaturaOrderByOrdenUnidadAsc(Long idAsignatura);

    @Query("""
        SELECT ud
        FROM UnidadDidactica ud
        JOIN FETCH ud.asignatura a
        JOIN FETCH ud.evaluacion
        WHERE a IN :asignaturas
        ORDER BY a.idAsignatura ASC, ud.ordenUnidad ASC
    """)
    List<UnidadDidactica> findByAsignaturaInOrderByAsignatura_IdAsignaturaAscOrdenUnidadAsc(List<Asignatura> asignaturas);
}
