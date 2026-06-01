package com.example.academicapp.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "nota")
/**
 * Entidad JPA que representa una tabla del modelo de datos academico.
 */
public class Nota {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_nota")
    private Long idNota;

    @Column(name = "valor_calificacion", nullable = false, precision = 4, scale = 2)
    private BigDecimal valorCalificacion;

    @Column(name = "ponderacion", nullable = false, precision = 5, scale = 2)
    private BigDecimal ponderacion;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_alumno", nullable = false)
    private Alumno alumno;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_unidad", nullable = false)
    private UnidadDidactica unidadDidactica;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_instrumento", nullable = false)
    private InstrumentoEvaluacion instrumentoEvaluacion;

    public Nota() {
    }

    public Nota(Long idNota, BigDecimal valorCalificacion, BigDecimal ponderacion, LocalDateTime fechaRegistro,
                Alumno alumno, UnidadDidactica unidadDidactica, InstrumentoEvaluacion instrumentoEvaluacion) {
        this.idNota = idNota;
        this.valorCalificacion = valorCalificacion;
        this.ponderacion = ponderacion;
        this.fechaRegistro = fechaRegistro;
        this.alumno = alumno;
        this.unidadDidactica = unidadDidactica;
        this.instrumentoEvaluacion = instrumentoEvaluacion;
    }

    public Long getIdNota() {
        return idNota;
    }

    public void setIdNota(Long idNota) {
        this.idNota = idNota;
    }

    public BigDecimal getValorCalificacion() {
        return valorCalificacion;
    }

    public void setValorCalificacion(BigDecimal valorCalificacion) {
        this.valorCalificacion = valorCalificacion;
    }

    public BigDecimal getPonderacion() {
        return ponderacion;
    }

    public void setPonderacion(BigDecimal ponderacion) {
        this.ponderacion = ponderacion;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public Alumno getAlumno() {
        return alumno;
    }

    public void setAlumno(Alumno alumno) {
        this.alumno = alumno;
    }

    public UnidadDidactica getUnidadDidactica() {
        return unidadDidactica;
    }

    public void setUnidadDidactica(UnidadDidactica unidadDidactica) {
        this.unidadDidactica = unidadDidactica;
    }

    public InstrumentoEvaluacion getInstrumentoEvaluacion() {
        return instrumentoEvaluacion;
    }

    public void setInstrumentoEvaluacion(InstrumentoEvaluacion instrumentoEvaluacion) {
        this.instrumentoEvaluacion = instrumentoEvaluacion;
    }
}