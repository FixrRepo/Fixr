package com.inspaya.fixr.dtos;

import com.inspaya.fixr.enums.MediaType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MediaDto(
        @NotBlank(message = "Media to support report is required") String mediaUrl,
        @NotNull(message = "Media type must be specified") MediaType mediaType
) {
}
