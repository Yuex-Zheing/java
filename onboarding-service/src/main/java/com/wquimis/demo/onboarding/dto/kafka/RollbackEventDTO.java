package com.wquimis.demo.onboarding.dto.kafka;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para eventos de rollback
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RollbackEventDTO {
    private String transactionId;
    private String failedStep;
    private String errorMessage;
    private LocalDateTime timestamp;
    private String rollbackReason;
}
