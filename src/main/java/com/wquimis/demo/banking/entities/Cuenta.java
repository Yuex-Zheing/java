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
    public enum TipoCuenta {
        AHORROS, CORRIENTE
    }

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

    @Column(precision = 10, scale = 4, nullable = false)
    private BigDecimal saldodisponible;

    // Proteger el saldo inicial
    public void setSaldoinicial(BigDecimal saldoinicial) {
        if (this.saldoinicial != null && !this.saldoinicial.equals(saldoinicial)) {
            throw new IllegalStateException("El saldo inicial no puede ser modificado una vez establecido");
        }
        this.saldoinicial = saldoinicial;
        if (this.saldodisponible == null) {
            this.saldodisponible = saldoinicial;
        }
    }

    // Actualizar solo el saldo disponible
    public void actualizarSaldoDisponible(BigDecimal monto) {
        this.saldodisponible = this.saldodisponible.add(monto);
    }

    // Inicialización de cuenta
    @PrePersist
    protected void inicializarCuenta() {
        if (this.fechacreacion == null) {
            this.fechacreacion = LocalDateTime.now();
        }
        if (this.estado == null) {
            this.estado = true;
        }
        if (this.saldoinicial == null) {
            this.saldoinicial = BigDecimal.ZERO;
        }
        if (this.saldodisponible == null) {
            this.saldodisponible = this.saldoinicial;
        }
    }

    // Manejo del cierre de cuenta
    @PreUpdate
    protected void verificarCierre() {
        if (estado != null && !estado && fechacierre == null) {
            fechacierre = LocalDateTime.now();
        }
    }
}
