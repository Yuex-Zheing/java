package com.wquimis.demo.banking.entities;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.hibernate.annotations.ColumnDefault;

@Data
@Entity
@Table(name = "cuentas")
public class Cuenta {
    @Id
    private Integer numerocuenta;

    @ManyToOne
    @JoinColumn(name = "idcliente", nullable = false)
    private Cliente cliente;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TipoCuenta tipocuenta;

    @Column(precision = 10, scale = 4, nullable = false)
    private BigDecimal saldoinicial;

    @Column(nullable = false)
    @ColumnDefault("true")
    private Boolean estado;

    @Column(nullable = false)
    private LocalDateTime fechacreacion;

    private LocalDateTime fechacierre;

    @OneToMany(mappedBy = "cuenta", cascade = CascadeType.ALL)
    private List<Movimiento> movimientos;

    @Column(precision = 10, scale = 4)
    private BigDecimal saldodisponible;

    public BigDecimal getSaldodisponible() {
        return saldodisponible != null ? saldodisponible : saldoinicial;
    }

    public void setSaldodisponible(BigDecimal saldodisponible) {
        this.saldodisponible = saldodisponible;
    }

    public enum TipoCuenta {
        AHORROS, CORRIENTE
    }

    @PrePersist
    public void prePersist() {
        if (estado == null) {
            estado = true;
        }
        if (fechacreacion == null) {
            fechacreacion = LocalDateTime.now();
        }
        if (saldoinicial == null) {
            saldoinicial = BigDecimal.ZERO;
        }
        if (saldodisponible == null) {
            saldodisponible = saldoinicial;
        }
    }

    @PreUpdate
    public void preUpdate() {
        if (!estado && fechacierre == null) {
            fechacierre = LocalDateTime.now();
        }
    }
}
