package com.inspaya.fixr.dtos;

public record MediaUploadResponse(
        String signature,
        long timestamp,
        long expiresAt,
        String apiKey,
        String cloudName,
        String folder
) {
}
