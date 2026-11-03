package com.inspaya.fixr.services.email;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.Thymeleaf;
import org.thymeleaf.context.Context;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class EmailTemplateEngine {
    private final TemplateEngine engine;

    public String otpEmailTemplate(String otp, Duration duration){
        Context context = new Context();
        context.setVariable("otp", otp);
        context.setVariable("expiryMinutes", duration.toMinutes());
        return engine.process("emails/otp", context);
    }
}
