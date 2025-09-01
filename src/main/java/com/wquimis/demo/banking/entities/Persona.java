package com.wquimis.demo.banking.entities;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "personas")
public class Persona {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idpersona;

    @Column(length = 10, unique = true, nullable = false)
    private String identificacionpersona;

    @Column(length = 150, nullable = false)
    private String nombres;

    @Column(length = 1, nullable = false)
    private String genero;

    @Column(nullable = false)
    private Integer edad;

    @Column(length = 300)
    private String direccion;

    @Column(length = 15)
    private String telefono;

    private Boolean estado;

    @OneToOne(mappedBy = "persona")
    private Cliente cliente;
}
