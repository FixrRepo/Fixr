package com.inspaya.fixr.services;

import com.inspaya.fixr.configurations.properties.FixrUserSecurityProperties;
import com.inspaya.fixr.dtos.EmailVerificationDto;
import com.inspaya.fixr.dtos.PasswordResetDto;
import com.inspaya.fixr.entities.token.OtpToken;
import com.inspaya.fixr.enums.DeliveryMedium;
import com.inspaya.fixr.enums.OtpTokenPurpose;
import com.inspaya.fixr.repositories.OtpTokenRepo;
import com.inspaya.fixr.services.email.EmailServiceProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VerificationService {
    private final OtpTokenRepo repo;
    private final EmailServiceProvider provider;
    private final FixrUserSecurityProperties securityProperties;

    public boolean verifyEmailOtp(EmailVerificationDto dto){
        return verifyToken(dto.email(), dto.otpCode(), OtpTokenPurpose.EmailVerification);
    }

    public boolean verifyPasswordResetToken(PasswordResetDto dto){
        return verifyToken(dto.email(), dto.otpToken(), OtpTokenPurpose.PasswordReset);
    }

    private boolean verifyToken(String email, String otpToken, OtpTokenPurpose purpose){
        Optional<OtpToken> optionalToken = repo.findByEmailAndPurpose(email, purpose);
        if(optionalToken.isEmpty())
            return false;
        OtpToken token = optionalToken.get();
        boolean isMatch = generateOTPHash(otpToken.toUpperCase()).equals(token.getTokenHash());

        var isValid = token.isValid() && isMatch;
        if(isValid){
            token.setIsUsed(true);
            repo.save(token);
        }
        return isValid;
    }

    @Async
    public void generateAndSendEmailVerification(String emailAddress){
        generateAndSendToken(OtpTokenPurpose.EmailVerification, DeliveryMedium.Email, emailAddress);
    }
    @Async
    public void generateAndSendPasswordResetToken(String emailAddress){
        generateAndSendToken(OtpTokenPurpose.PasswordReset, DeliveryMedium.Email, emailAddress);
    }

    private void generateAndSendToken(OtpTokenPurpose purpose, DeliveryMedium medium, String deliveredTo){
        OtpToken token = generateToken(purpose, medium, deliveredTo);
        provider.sendVerificationCode(token);
    }

    private OtpToken generateToken(OtpTokenPurpose purpose, DeliveryMedium medium, String deliveredTo){
        OtpToken token;
        if(medium.equals(DeliveryMedium.Email))
            token = generateEmailOTP(purpose, deliveredTo);
        else
           token = generatePhoneOTP(medium, purpose, deliveredTo);
        return repo.save(token);
    }

    private OtpToken generateEmailOTP(OtpTokenPurpose purpose, String email){
        Optional<OtpToken> existing = repo.findByEmailAndPurpose(email, purpose);

        existing.ifPresent(repo::delete);
        OtpToken token = new OtpToken();
        token.setPurpose(purpose);
        token.setDeliveryMedium(DeliveryMedium.Email);
        token.setIsUsed(false);

        String stringToken = generateRandomToken();
        // Transient property, it is lost at the end of a request
        token.setToken(stringToken);
        token.setTokenHash(generateOTPHash(stringToken));
        token.setEmail(email);
        return token;
    }

    private OtpToken generatePhoneOTP(DeliveryMedium method, OtpTokenPurpose purpose, String phone){
        Optional<OtpToken> existing = repo.findByPhoneAndPurpose(phone, purpose);

        if(existing.isPresent() && existing.get().getExpiresAt().isAfter(ZonedDateTime.now(ZoneId.of("Africa/Lagos"))) && !existing.get().getIsUsed())
            return existing.get();
        existing.ifPresent(repo::delete);
        OtpToken token = new OtpToken();
        token.setPurpose(purpose);
        token.setDeliveryMedium(method);
        token.setIsUsed(false);

        String stringToken = generateRandomToken();
        // Transient property, it is lost at the end of a request
        token.setToken(stringToken);
        token.setTokenHash(generateOTPHash(stringToken));
        token.setPhone(phone);

        return token;
    }

    private String generateRandomToken(){
        String rawToken = UUID.randomUUID().toString().replaceAll("-", "");
        return rawToken.substring(0, securityProperties.tokenLenght()).toUpperCase(Locale.ROOT);
    }

    private String generateOTPHash(String token){
        return DigestUtils.md5DigestAsHex((token + securityProperties.otpSecret()).getBytes(StandardCharsets.UTF_8));
    }
}
