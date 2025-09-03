package com.wquimis.demo.banking.dto;

import jakarta.validation.constraints.NotNull;

public class UpdateCuentaDTO {
    @NotNull(message = "El estado es requerido")
    private Boolean estado;

    public Boolean getEstado() {
        return estado;
    }

    public void setEstado(Boolean estado) {
        this.estado = estado;
    }
}
