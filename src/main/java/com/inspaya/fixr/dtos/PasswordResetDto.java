package com.inspaya.fixr.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PasswordResetDto(
        @NotBlank String otpToken,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8, message = "Password is too short") String password,
        @NotBlank @Size(min = 8, message = "Password is too short") String rePassword
) {
}
