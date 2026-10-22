package com.inspaya.fixr.dtos;

import com.inspaya.fixr.enums.AuthHeaderType;

public record JwtTokenResponseDto(AuthHeaderType type, String accessToken, String refreshToken) {
}
