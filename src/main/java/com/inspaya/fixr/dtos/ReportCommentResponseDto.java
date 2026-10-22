package com.inspaya.fixr.dtos;

import java.time.ZonedDateTime;
import java.util.UUID;

public record ReportCommentResponseDto(
        UUID commentId,
        String content,
        String username,
        ZonedDateTime createdAt,
        ZonedDateTime updatedAt
) {
}
