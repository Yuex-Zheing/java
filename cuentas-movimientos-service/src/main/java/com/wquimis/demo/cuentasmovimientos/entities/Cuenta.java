package com.wquimis.demo.cuentasmovimientos.entities;

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

    @Column(nullable = false)
    private Long idcliente;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TipoCuenta tipocuenta;

    @Column(precision = 10, scale = 4, nullable = false)
    private BigDecimal saldoinicial;

    @Column(precision = 10, scale = 4)
    private BigDecimal saldodisponible;

    @Column(nullable = false)
    @ColumnDefault("true")
    private Boolean estado;

    @Column(nullable = false)
    private LocalDateTime fechacreacion;

    @Column
    private LocalDateTime fechacierre;

    @OneToMany(mappedBy = "cuenta", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Movimiento> movimientos;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.fechacreacion = now;
        
        if (this.estado == null) {
            this.estado = true;
        }
        
        if (this.saldodisponible == null) {
            this.saldodisponible = this.saldoinicial;
        }
    }

    @PreUpdate
    public void preUpdate() {
        // No actualizamos fechacreacion en updates
    }

    // Métodos de negocio
    public void debitar(BigDecimal monto) {
        if (this.saldodisponible == null) {
            this.saldodisponible = this.saldoinicial;
        }
        this.saldodisponible = this.saldodisponible.subtract(monto);
    }

    public void acreditar(BigDecimal monto) {
        if (this.saldodisponible == null) {
            this.saldodisponible = this.saldoinicial;
        }
        this.saldodisponible = this.saldodisponible.add(monto);
    }

    public boolean tieneSaldoSuficiente(BigDecimal monto) {
        BigDecimal saldoActual = this.saldodisponible != null ? this.saldodisponible : this.saldoinicial;
        return saldoActual.compareTo(monto) >= 0;
    }

    public BigDecimal getSaldodisponible() {
        return this.saldodisponible != null ? this.saldodisponible : this.saldoinicial;
    }
}
