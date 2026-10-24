package com.inspaya.fixr.dtos;

import com.inspaya.fixr.enums.VotingAbusePattern;

import java.time.Instant;
import java.util.UUID;

public record VotingAbuseFlagDto(
        UUID flagId,
        UUID reportId,
        UUID flaggedUserId,
        String flaggedUsername,
        VotingAbusePattern pattern,
        String details,
        Instant createdAt
) {
}
