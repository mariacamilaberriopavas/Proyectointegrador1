package com.nominet.gestion_empresas.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "horas_extras")
@Data
public class HorasExtras {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_horas_extras")
    private Integer idHorasExtras;

    @Column(name = "fecha")
    private LocalDate fecha;

    // Tipo: DIURNA, NOCTURNA, DOMINICAL_DIURNA, DOMINICAL_NOCTURNA
    @Column(name = "tipo", length = 30)
    private String tipo;

    @Column(name = "horas", precision = 5, scale = 2)
    private BigDecimal horas;

    // Valor hora ordinaria base (salario/240)
    @Column(name = "valor_por_hora", precision = 10, scale = 2)
    private BigDecimal valorPorHora;

    // Recargo aplicado (0.25, 0.75, 1.50...)
    @Column(name = "recargo", precision = 5, scale = 2)
    private BigDecimal recargo;

    // Total = horas * valorPorHora * (1 + recargo)
    @Column(name = "total", precision = 10, scale = 2)
    private BigDecimal total;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idempleados", nullable = false)
    private Empleado empleado;
}