package com.wquimis.demo.banking.entities;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

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

    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL)
    private List<Cuenta> cuentas;
}
