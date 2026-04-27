package com.nominet.gestion_empresas.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "contrato")
@Data
public class Contrato {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idcontrato")
    private Integer idcontrato;

    @Column(name = "tipocontrato", length = 50)
    private String tipocontrato;   // "indefinido", "fijo", "prestacion"

    @Column(name = "fecha_inicio")
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDate fechaFin;

    @Column(name = "cargo", length = 50)
    private String cargo;

    @Column(name = "salario", precision = 10, scale = 2)
    private BigDecimal salario;

    // FK empleados
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idempleados", nullable = false)
    private Empleado empleado;
}