package com.inspaya.fixr.dtos;

import jakarta.validation.constraints.NotNull;

public record CoordinateDto(
        @NotNull(message = "Specify longitude") Double longitude,
        @NotNull(message = "Specify latitude") Double latitude
) {
}
