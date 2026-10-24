package com.inspaya.fixr.dtos;

import com.inspaya.fixr.enums.AbuseReviewOutcome;
import jakarta.validation.constraints.NotNull;

public record ReviewVotingAbuseFlagRequest(
        @NotNull AbuseReviewOutcome outcome,
        String notes
) {
}
