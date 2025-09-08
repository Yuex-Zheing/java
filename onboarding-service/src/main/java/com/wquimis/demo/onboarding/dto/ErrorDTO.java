package com.wquimis.demo.onboarding.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorDTO {
    
    private String codigo;
    private String mensaje;
    private String detalle;
    private LocalDateTime timestamp;
    
    public static ErrorDTO of(String codigo, String mensaje, String detalle) {
        return new ErrorDTO(codigo, mensaje, detalle, LocalDateTime.now());
    }
}
