package com.inspaya.fixr.services.email;

import com.inspaya.fixr.entities.token.OtpToken;

public interface EmailService {
    void sendVerificationCode(OtpToken token);

}
