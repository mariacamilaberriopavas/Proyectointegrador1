package com.nominet.gestion_empresas.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;

@Entity
@Table(name = "empleados")
@Data
public class Empleado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idempleados")
    private Integer idempleados;

    @NotNull(message = "La cédula es obligatoria")
    @Column(name = "cedula")
    private Integer cedula;

    @NotBlank(message = "El nombre es obligatorio")
    @Column(name = "nombre", length = 100)
    private String nombre;

    @Column(name = "celular", length = 20)
    private String celular;

    @Column(name = "correo", length = 100)
    private String correo;

    @Column(name = "estado", length = 20)
    private String estado;   // "activo" / "inactivo"

    // Relación con Empresa (FK idempresa)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idempresa", nullable = false)
    private Empresa empresa;

    @OneToOne(mappedBy = "empleado", cascade = CascadeType.ALL)
    private Usuario usuario;
}