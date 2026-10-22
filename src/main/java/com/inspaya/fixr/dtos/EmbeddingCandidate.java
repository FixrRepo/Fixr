package com.inspaya.fixr.dtos;

import java.util.UUID;

public record EmbeddingCandidate(
        UUID reportId,
        int rank,
        double visualScore,
        Double distanceMeters,
        Double distanceScore,
        Double similarityScore
) {
}
