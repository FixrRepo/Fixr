package com.inspaya.fixr.dtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

import java.util.List;

public record CreateReportDto(
        String description,
        @NotBlank(message = "Report category must be specified") String category,
        Boolean isAnonymous,
        List<@Valid MediaDto> mediaList,
        @NotNull LocationDto locationDto,
        @NotBlank String idempotencyKey
        ) {
}
