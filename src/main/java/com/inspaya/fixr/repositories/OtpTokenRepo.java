package com.inspaya.fixr.repositories;

import com.inspaya.fixr.entities.token.OtpToken;
import com.inspaya.fixr.enums.OtpTokenPurpose;
import jakarta.validation.constraints.Email;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OtpTokenRepo extends JpaRepository<OtpToken, String> {
    Optional<OtpToken> findByEmailAndPurpose(@Email(message = "Invalid email format") String email, OtpTokenPurpose otpTokenPurpose);

    Optional<OtpToken> findByPhoneAndPurpose(String phone, OtpTokenPurpose purpose);
}
