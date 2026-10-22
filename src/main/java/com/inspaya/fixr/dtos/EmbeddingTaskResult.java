package com.inspaya.fixr.dtos;

import com.inspaya.fixr.enums.EmbeddingTaskEvents;

import java.util.List;
import java.util.UUID;

public record EmbeddingTaskResult(
        EmbeddingTaskEvents event,
        UUID jobId,
        UUID reportId,
        String status,
        List<EmbeddingCandidate> candidates,
        String error
) {
}
