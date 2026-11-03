package com.inspaya.fixr.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EmailVerificationDto(
        @NotBlank(message = "Otp code cannot be empty") String otpCode,
        @NotBlank @Email(message = "Invalid email format") String email
) {
}
