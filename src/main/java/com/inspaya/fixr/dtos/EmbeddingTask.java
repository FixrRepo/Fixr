package com.inspaya.fixr.dtos;

import com.inspaya.fixr.enums.EmbeddingTaskEvents;
import com.inspaya.fixr.enums.MediaType;

import java.util.UUID;

public record EmbeddingTask(
        EmbeddingTaskEvents event,
        UUID jobId,
        UUID reportId,
        EmbeddingTaskMedia media,
        String description
) {
}
