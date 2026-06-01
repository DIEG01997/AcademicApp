package com.example.academicapp.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "ciclo")
/**
 * Entidad JPA que representa una tabla del modelo de datos academico.
 */
public class Ciclo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ciclo")
    private Long idCiclo;

    @Column(name = "nombre_ciclo", nullable = false, length = 100)
    private String nombreCiclo;

    @Column(name = "siglas", nullable = false, unique = true, length = 10)
    private String siglas;

    public Ciclo() {
    }

    public Ciclo(Long idCiclo, String nombreCiclo, String siglas) {
        this.idCiclo = idCiclo;
        this.nombreCiclo = nombreCiclo;
        this.siglas = siglas;
    }

    public Long getIdCiclo() {
        return idCiclo;
    }

    public void setIdCiclo(Long idCiclo) {
        this.idCiclo = idCiclo;
    }

    public String getNombreCiclo() {
        return nombreCiclo;
    }

    public void setNombreCiclo(String nombreCiclo) {
        this.nombreCiclo = nombreCiclo;
    }

    public String getSiglas() {
        return siglas;
    }

    public void setSiglas(String siglas) {
        this.siglas = siglas;
    }
}