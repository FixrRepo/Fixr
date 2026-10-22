package com.inspaya.fixr.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LocationDto(
        @NotNull CoordinateDto coordinates,
        @NotBlank(message = "State is required") String state,
        @NotBlank(message = "Lga is required") String lga,
        String  city
){}