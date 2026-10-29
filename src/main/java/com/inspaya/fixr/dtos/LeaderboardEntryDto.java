package com.inspaya.fixr.dtos;

import java.math.BigDecimal;

public record LeaderboardEntryDto(
        long rank,
        String username,
        BigDecimal credibilityScore,
        long reportCount,
        long supportedReportCount,
        long disputedReportCount,
        int scoredAssessments
) {
}
