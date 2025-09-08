package com.wquimis.demo.onboarding.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OnboardingAsyncResponseDTO {
    private String transactionId;
    private String status; // PROCESSING, SUCCESS, FAILED
    private String message;
}
