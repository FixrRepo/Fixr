package com.inspaya.fixr.dtos;

import java.util.UUID;

public record DuplicateCandidateResponseDto(
        UUID reportId,
        String description,
        int rank,
        double similarityScore,
        Double distanceMeters
) {
}
