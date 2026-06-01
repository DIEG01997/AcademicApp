package com.example.academicapp.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "chat_curso_lectura")
/**
 * Entidad JPA que representa una tabla del modelo de datos academico.
 */
public class ChatCursoLectura {

    @Id
    @Column(name = "id_alumno")
    private Long idAlumno;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "id_alumno")
    private Alumno alumno;

    @Column(name = "ultimo_mensaje_visto_id", nullable = false)
    private Long ultimoMensajeVistoId;

    public Long getIdAlumno() {
        return idAlumno;
    }

    public void setIdAlumno(Long idAlumno) {
        this.idAlumno = idAlumno;
    }

    public Alumno getAlumno() {
        return alumno;
    }

    public void setAlumno(Alumno alumno) {
        this.alumno = alumno;
    }

    public Long getUltimoMensajeVistoId() {
        return ultimoMensajeVistoId;
    }

    public void setUltimoMensajeVistoId(Long ultimoMensajeVistoId) {
        this.ultimoMensajeVistoId = ultimoMensajeVistoId;
    }
}
