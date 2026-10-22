package com.inspaya.fixr.dtos;

import com.inspaya.fixr.enums.ReportVoteType;

public record ReportVoteResponseDto(
        ReportVoteType viewerVote,
        long upvoteCount,
        long downvoteCount
) {
}
