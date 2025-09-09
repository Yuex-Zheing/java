package com.wquimis.demo.cuentasmovimientos.dto.kafka;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CompletedEventDTO {
    private String transactionId;
    private String status;
    private LocalDateTime timestamp;
}
