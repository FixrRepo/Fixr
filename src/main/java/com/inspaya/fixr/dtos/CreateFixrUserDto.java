package com.inspaya.fixr.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

public record CreateFixrUserDto(
        @NotBlank(message = "Firstname is required for account creation") String firstname,
        @NotBlank(message = "Lastname is required for account creation") String lastname,
        String middleName,
        @Email(message = "Email is required for account creation") String email,
        @Length(min = 11, max = 13, message = "Phone number should be of valid lenght 11 or 13 digits") String phoneNumber,
        @NotBlank(message = "Password is required for account creation")
        @Length(min = 8, message = "Password is weak") String password,
        @NotNull Boolean isInNigeria,
        @NotNull Boolean agreedToTerms,
        @NotBlank String state,
        @NotBlank String lga,
        String city
) {
}
