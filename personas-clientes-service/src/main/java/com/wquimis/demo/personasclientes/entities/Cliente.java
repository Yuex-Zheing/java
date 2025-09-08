package com.wquimis.demo.personasclientes.entities;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "clientes")
public class Cliente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idcliente;

    @OneToOne
    @JoinColumn(name = "idpersona", nullable = false)
    private Persona persona;

    @Column(length = 50, unique = true, nullable = false)
    private String nombreusuario;

    @Column(length = 100, nullable = false)
    private String contrasena;

    private Boolean estado;

    // Eliminamos la relación con cuentas ya que estará en otro microservicio
}
