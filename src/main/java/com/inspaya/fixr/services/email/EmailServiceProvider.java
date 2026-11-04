package com.inspaya.fixr.services.email;

import com.inspaya.fixr.entities.token.OtpToken;
import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailServiceProvider implements EmailService{
    private final EmailTemplateEngine engine;
    private final Resend provider;

    @Override
    @Async
    public void sendVerificationCode(OtpToken token) {
        String otpTemplate = engine.otpEmailTemplate(token.getToken(),
                Duration.between(ZonedDateTime.now(ZoneId.of("Africa/Lagos")), token.getExpiresAt()));
        sendEmail(otpTemplate, EmailTitles.OTP.title, token.getEmail());
    }

    private void sendEmail(String html, String title, String recipient){
        CreateEmailOptions emailOptions = CreateEmailOptions.builder()
                        .html(html)
                        .to(recipient)
                        .from("Fixr notification <notifications.fixr.ng>")
                        .subject(title)
                        .build();

        try {
            provider.emails().send(emailOptions);

        } catch (ResendException e) {
            log.error("Failed to send email notification to {} because {}", recipient, e.getLocalizedMessage());
        }
    }

}
