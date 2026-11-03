package com.inspaya.fixr.dtos;

import java.time.ZonedDateTime;

public record OtpTokenResponse (String otpToken, ZonedDateTime expiresAt){
}
