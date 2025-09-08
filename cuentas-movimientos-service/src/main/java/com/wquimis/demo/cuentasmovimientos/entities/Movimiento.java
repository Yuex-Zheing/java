package com.wquimis.demo.cuentasmovimientos.entities;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import org.hibernate.annotations.ColumnDefault;

@Data
@Entity
@Table(name = "movimientos")
public class Movimiento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idmovimiento;

    @ManyToOne
    @JoinColumn(name = "numerocuenta", nullable = false)
    private Cuenta cuenta;

    @Column(nullable = false)
    @ColumnDefault("true")
    private Boolean estado;

    @Column(nullable = false)
    private LocalDate fechamovimiento;

    @Column(nullable = false)
    private LocalTime horamovimiento;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TipoMovimiento tipomovimiento;

    @Column(precision = 10, scale = 4, nullable = false)
    private BigDecimal montomovimiento;

    @Column(precision = 10, scale = 4, nullable = false)
    private BigDecimal saldodisponible;

    @Column(length = 300)
    private String movimientodescripcion;

    public enum TipoMovimiento {
        RETIRO, DEPOSITO
    }

    @PrePersist
    public void prePersist() {
        if (estado == null) {
            estado = true;
        }
        if (fechamovimiento == null) {
            fechamovimiento = LocalDate.now();
        }
        if (horamovimiento == null) {
            horamovimiento = LocalTime.now();
        }
    }
}
