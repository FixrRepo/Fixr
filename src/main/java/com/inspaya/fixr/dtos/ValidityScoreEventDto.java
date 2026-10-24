package com.inspaya.fixr.dtos;

import com.inspaya.fixr.enums.ValidityScoreReason;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ValidityScoreEventDto(
        UUID eventId,
        UUID reportId,
        BigDecimal scoreDelta,
        BigDecimal scoreAfter,
        ValidityScoreReason reason,
        String scoringVersion,
        int evaluationEligibleVoters,
        BigDecimal evaluationSupportRatio,
        Instant createdAt
) {
}
