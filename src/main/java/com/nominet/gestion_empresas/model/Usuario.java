package com.nominet.gestion_empresas.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;

@Entity
@Table(name = "usuario")
@Data
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idusuario")
    private Integer idusuario;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "Formato de correo inválido")
    @Column(name = "correo", length = 100, unique = true)
    private String correo;

    @Column(name = "password", length = 255)
    private String password;

    // "ADMIN", "EMPRESA", "EMPLEADO"
    @Column(name = "rol", length = 45)
    private String rol;

    @Column(name = "usuariocl", length = 45)
    private String usuariocl;

    // Relación con Empleados (FK idempleados)
    @OneToOne
    @JoinColumn(name = "idempleados")
    private Empleado empleado;
}