package com.inspaya.fixr.dtos;

import java.math.BigDecimal;
import java.util.List;

public record UserValidityResponseDto(
        BigDecimal validityScore,
        int scoredAssessments,
        List<ValidityScoreEventDto> history
) {
}
