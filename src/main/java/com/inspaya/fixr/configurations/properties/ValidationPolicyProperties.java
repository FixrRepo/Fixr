package com.inspaya.fixr.configurations.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;

@ConfigurationProperties(prefix = "fixr.validation")
public record ValidationPolicyProperties(
        int minimumVoters,
        BigDecimal supportThreshold,
        BigDecimal disputeThreshold,
        BigDecimal scoreAdjustment,
        String scoringVersion,
        int burstVoteCount,
        int burstWindowMinutes,
        int repeatedTargetReportCount,
        int repeatedTargetWindowDays,
        int reciprocalReportsPerDirection,
        int reciprocalWindowDays
) {
}
