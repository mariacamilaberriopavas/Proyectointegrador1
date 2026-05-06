package com.nominet.gestion_empresas.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "colilla_pago")
@Data
public class ColillaPago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_colilla")
    private Integer idColilla;

    @Column(name = "periodo_inicio")
    private LocalDate periodoInicio;

    @Column(name = "periodo_fin")
    private LocalDate periodoFin;

    @Column(name = "salario_base", precision = 10, scale = 2)
    private BigDecimal salarioBase;

    @Column(name = "horas_extras", precision = 5, scale = 2)
    private BigDecimal horasExtras;

    @Column(name = "valor_horas_extras", precision = 10, scale = 2)
    private BigDecimal valorHorasExtras;

    @Column(name = "deducciones", precision = 10, scale = 2)
    private BigDecimal deducciones;

    @Column(name = "total_pagar", precision = 10, scale = 2)
    private BigDecimal totalPagar;

    // FK empleado
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idempleados", nullable = false)
    private Empleado empleado;
}