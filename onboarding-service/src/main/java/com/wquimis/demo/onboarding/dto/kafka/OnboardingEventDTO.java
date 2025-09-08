package com.wquimis.demo.onboarding.dto.kafka;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OnboardingEventDTO {
    private String transactionId;
    private String eventType;
    private LocalDateTime timestamp;
    private Object payload;
    private String status; // PENDING, SUCCESS, FAILED
    private String errorMessage;
    private int retryCount;

    public OnboardingEventDTO(String eventType, Object payload) {
        this.transactionId = UUID.randomUUID().toString();
        this.eventType = eventType;
        this.payload = payload;
        this.timestamp = LocalDateTime.now();
        this.status = "PENDING";
        this.retryCount = 0;
    }
}
