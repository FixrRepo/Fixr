package com.inspaya.fixr.dtos;

import com.inspaya.fixr.enums.ReportValidationStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record ReportValidationResponseDto(
        UUID reportId,
        ReportValidationStatus validationStatus,
        int eligibleVoteCount,
        BigDecimal upvoteWeight,
        BigDecimal downvoteWeight,
        BigDecimal weightedSupportRatio,
        boolean provisional
) {
}
