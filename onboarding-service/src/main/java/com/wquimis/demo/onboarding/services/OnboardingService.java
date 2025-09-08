package com.wquimis.demo.onboarding.services;

import com.wquimis.demo.onboarding.dto.OnboardingRequestDTO;
import com.wquimis.demo.onboarding.dto.OnboardingResponseDTO;

public interface OnboardingService {
    OnboardingResponseDTO procesarOnboarding(OnboardingRequestDTO request);
}
