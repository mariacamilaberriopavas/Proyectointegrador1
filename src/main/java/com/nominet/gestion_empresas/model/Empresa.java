package com.nominet.gestion_empresas.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;

@Entity
@Table(name = "empresa")
@Data
public class Empresa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idempresa")
    private Integer idempresa;

    @NotBlank(message = "El NIT es obligatorio")
    @Column(name = "nit", length = 100, unique = true)
    private String nit;

    @NotBlank(message = "El nombre es obligatorio")
    @Column(name = "nombre", length = 100)
    private String nombre;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "Correo inválido")
    @Column(name = "correo", length = 45, unique = true)
    private String correo;

    @Column(name = "celular", length = 20)
    private String celular;

    @Column(name = "password", length = 255)
    private String password;
}