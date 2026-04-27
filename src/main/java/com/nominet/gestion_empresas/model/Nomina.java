package com.nominet.gestion_empresas.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Table(name = "nomina")
@Data
public class Nomina {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idNomina")
    private Integer idNomina;

    @Column(name = "fecha_inicio")
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDate fechaFin;

    @Column(name = "fecha_generacion")
    private LocalDate fechaGeneracion;

    @Column(name = "estado", length = 20)
    private String estado;   // "pendiente", "procesada", "pagada"

    // FK empresa
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idempresa", nullable = false)
    private Empresa empresa;
}