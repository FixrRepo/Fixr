package com.inspaya.fixr.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record OtpRequestDto(@NotBlank @Email String email) {
}
